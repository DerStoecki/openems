package io.openems.edge.chp.device;


import io.openems.common.exceptions.OpenemsError;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;

import io.openems.edge.bridge.modbus.api.element.*;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.chp.device.api.ChpInformationChannel;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.chp.device.task.ChpTaskImpl;
import io.openems.edge.chp.module.api.ChpModule;

import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.i2c.mcp.api.Mcp;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;


import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.List;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Chp",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE)
public class ChpImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, PowerLevel, ChpInformationChannel, EventHandler {
    private Mcp mcp;
    private ChpType chpType;
    private String accessMode;
    private final ChpErrorWorker chpErrorWorker = new ChpErrorWorker();
    private String[] errorPossibilities = {
            "Lüfter	gestört		",
            "Kühlwasserpumpe	gestört		",
            "Abgasgegendruck	max.		",
            "Einspeiseschalter			",
            "Externe	Störung		",
            "Überdrehzahl			",
            "Kühlwassertemperatur			",
            "Abgastemperatur	max.		",
            "			",
            "Not-Stopp			",
            "Ölstand	min.		",
            "Kühlwasserdruck	min.		",
            "Gasdruck	min.		",
            "Sicherheitstemperatur			",
            "Generatortemperatur			",
            "Schallhaubentemperatur			",
            "Zuschaltung	gestört		",
            "Synchronisierung	gestört		",
            "Drehzahl	<	50	/",
            "Ölstand	max.		",
            "Temperatur	Pt100_2	max.	",
            "Temperatur	Pt100_3	max.	",
            "Leistung	max.		",
            "Rückleistung			",
            "Abgastemperatur	min.		",
            "Öldruck	min.		",
            "Gasdruck	max.		",
            "Heizwasserpumpe	gestört		",
            "Anlassdrehzahl	<	50	Upm",
            "Zünddrehzahl			",
            "Drehzahlfenster			",
            "Drehzahl	<	1200	Upm",
            "Schaltuhr	Abwahl	Meldung	",
            "Schaltuhr	Freigabe	Meldung	",
            "Netzstörung	F	<	nicht",
            "Netzstörung	F	>	nicht",
            "Netzstörung	U+F	<>	nicht",
            "Klopfen	Leistung	Min.	",
            "Klopfen	Leistung	Max.	",
            "Netzkuppelschalter			",
            "Leistungsregler	gestört		",
            "Lambdaregler	gestört		",
            "Generatorschütz	gestört		",
            "Zündung gestört			",
            "Öldruck	gestört		",
            "Lambda	Startposition		",
            "Klopfen	EIN		",
            "Klopfen	AUS		",
            "Batterie	Unterspannung		",
            "Generator	Unterspannung		",
            "Generator	Überspannung		",
            "Generator	Überstrom		",
            "Generator	Schieflast		",
            "Dichttest	gestört		",
            "Netzschutz	gestört		",
            "Sensoren	gestört		",
            "Klopfen	Störung		",
            "Netz	o.k.	Meldung	",
            "Netzstörung	Warnung		",
            "Temperatur	Abwahl	Meldung	",
            "Temperatur	Freigabe	Meldung	",
            "Wartung	überschritten		",
            "Sicherheitsabschaltung			",
            "Motor	steht	nicht	",
            "Abgastemp. Differenz A/B			",
            "Reserve			",
            "Temp.	Rücklauf	max	PT100/2",
            "Temp.	Heizwasser	max	PT100/3",
            "Temp.	Motoröl	max	",
            "Temp.	Gasgemisch	max	",
            "Temp.	Gemischkühlwasser	max	",
            "Reserve			",
            "Abgastemperatur	A	max	",
            "Abgastemperatur	A	min	",
            "Abgastemperatur	B	max	",
            "Abgastemperatur	B	min	",
            "Abgastemperatur	C	max	",
            "Abgastemperatur	C	min	",
            "Abgastemperatur	D	max	",
            "Abgastemperatur	D	min	"};


    @Reference
    protected ConfigurationAdmin cm;

    @Reference
    protected ComponentManager cpm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public ChpImpl() {
        super(OpenemsComponent.ChannelId.values(),
                PowerLevel.ChannelId.values(),
                ChpInformationChannel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbusBridgeId());

        switch (config.chpType()) {
            case "EM_6_15":
                this.chpType = ChpType.Vito_EM_6_15;
                break;
            case "EM_9_20":
                this.chpType = ChpType.Vito_EM_9_20;
                break;
            case "EM_20_39":
                this.chpType = ChpType.Vito_EM_20_39;
                break;
            case "EM_20_39_70":
                this.chpType = ChpType.Vito_EM_20_39_RL_70;
                break;
            case "EM_50_81":
                this.chpType = ChpType.Vito_EM_50_81;
                break;
            case "EM_70_115":
                this.chpType = ChpType.Vito_EM_70_115;
                break;
            case "EM_100_167":
                this.chpType = ChpType.Vito_EM_100_167;
                break;
            case "EM_140_207":
                this.chpType = ChpType.Vito_EM_140_207;
                break;
            case "EM_199_263":
                this.chpType = ChpType.Vito_EM_199_263;
                break;
            case "EM_199_293":
                this.chpType = ChpType.Vito_EM_199_293;
                break;
            case "EM_238_363":
                this.chpType = ChpType.Vito_EM_238_363;
                break;
            case "EM_363_498":
                this.chpType = ChpType.Vito_EM_363_498;
                break;
            case "EM_401_549":
                this.chpType = ChpType.Vito_EM_401_549;
                break;
            case "EM_530_660":
                this.chpType = ChpType.Vito_EM_530_660;
                break;
            case "BM_36_66":
                this.chpType = ChpType.Vito_BM_36_66;
                break;
            case "BM_55_88":
                this.chpType = ChpType.Vito_BM_55_88;
                break;
            case "BM_190_238":
                this.chpType = ChpType.Vito_BM_190_238;
                break;
            case "BM_366_437":
                this.chpType = ChpType.Vito_BM_366_437;
                break;

            default:
                break;

        }
        if (config.accesMode().equals("rw")) {
            if (cpm.getComponent(config.chpModuleId()) instanceof ChpModule) {
                ChpModule chpModule = cpm.getComponent(config.chpModuleId());
                mcp = chpModule.getMcp();
                mcp.addTask(super.id(), new ChpTaskImpl(super.id(),
                        config.position(), config.minLimit(), config.maxLimit(),
                        config.percentageRange(), 4096.f, this.getPowerLevelChannel()));
            }
        }
        this.chpErrorWorker.activate(config.id());
        this.accessMode = config.accesMode();
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        if (this.accessMode.equals("rw")) {
            this.mcp.removeTask(super.id());
        }
    }


    @Override
    public String debugLog() {
        if (this.getPowerLevelChannel().getNextValue().get() != null) {
            if (chpType != null) {
                return "Chp: " + this.chpType.getName() + "is at " + this.getPowerLevelChannel().getNextValue().get();
            } else {
                return "Chp is at " + this.getPowerLevelChannel().getNextValue().get();
            }
        }
        return "Percentage Level at 0";
    }


    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC3ReadRegistersTask(0x4000, Priority.LOW,
                        new DummyRegisterElement(0x4000, 0x4000),
                        m(ChpInformationChannel.ChannelId.MODUS, new UnsignedWordElement(0x4001),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.STATUS, new UnsignedWordElement(0x4002),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.OPERATING_MODE, new UnsignedWordElement(0x4003),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SET_POINT_OPERATION_MODE, new SignedWordElement(0x4004),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_1, new UnsignedWordElement(0x4005),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_2, new UnsignedWordElement(0x4006),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_3, new UnsignedWordElement(0x4007),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_4, new UnsignedWordElement(0x4008),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_5, new UnsignedWordElement(0x4009),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_6, new UnsignedWordElement(0x400A),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_7, new UnsignedWordElement(0x400B),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ERROR_BITS_8, new UnsignedWordElement(0x400C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.OPERATING_HOURS, new UnsignedWordElement(0x400D),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.OPERATING_MINUTES, new UnsignedWordElement(0x400E),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.START_COUNTER, new UnsignedWordElement(0x400F),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.MAINTENANCE_INTERVAL, new SignedWordElement(0x4010),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.MODULE_LOCK, new SignedWordElement(0x4011),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.WARNING_TIME, new SignedWordElement(0x4012),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.NEXT_MAINTENANCE, new UnsignedWordElement(0x4013),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_A, new SignedWordElement(0x4014),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_B, new SignedWordElement(0x4015),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_C, new SignedWordElement(0x4016),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_D, new SignedWordElement(0x4017),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_1, new SignedWordElement(0x4018),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_2, new SignedWordElement(0x4019),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_3, new SignedWordElement(0x401A),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_4, new SignedWordElement(0x401B),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_5, new SignedWordElement(0x401C),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.PT_100_6, new SignedWordElement(0x401D),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.BATTERY_VOLTAGE, new SignedWordElement(0x401E),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.OIL_PRESSURE, new SignedWordElement(0x401F),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChpInformationChannel.ChannelId.LAMBDA_PROBE_VOLTAGE, new SignedWordElement(0x4020),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1)),
                        new FC3ReadRegistersTask(0x4025,Priority.LOW,
                        m(ChpInformationChannel.ChannelId.ROTATION_PER_MIN, new UnsignedWordElement(0x4025),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.TEMPERATURE_CONTROLLER, new SignedWordElement(0x4026),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.TEMPERATURE_CLEARANCE, new SignedWordElement(0x4027),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SUPPLY_VOLTAGE_L1, new SignedWordElement(0x4028),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SUPPLY_VOLTAGE_L2, new SignedWordElement(0x4029),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SUPPLY_VOLTAGE_L3, new SignedWordElement(0x402A),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_VOLTAGE_L1, new SignedWordElement(0x402B),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_VOLTAGE_L2, new SignedWordElement(0x402C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_VOLTAGE_L3, new SignedWordElement(0x402D),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_ELECTRICITY_L1, new SignedWordElement(0x402E),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_ELECTRICITY_L2, new SignedWordElement(0x402F),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_ELECTRICITY_L3, new SignedWordElement(0x4030),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SUPPLY_VOLTAGE_TOTAL, new SignedWordElement(0x4031),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_VOLTAGE_TOTAL, new SignedWordElement(0x4032),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.GENERATOR_ELECTRICITY_TOTAL, new SignedWordElement(0x4033),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.ENGINE_PERFORMANCE, new SignedWordElement(0x4034),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.SUPPLY_FREQUENCY, new FloatDoublewordElement(0x4035),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                        new FC3ReadRegistersTask(0x4037, Priority.LOW,
                        m(ChpInformationChannel.ChannelId.GENERATOR_FREQUENCY, new FloatDoublewordElement(0x4037),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                        new FC3ReadRegistersTask(0x403B, Priority.LOW,
                        m(ChpInformationChannel.ChannelId.ACTIVE_POWER_FACTOR, new SignedWordElement(0x403B),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_3),
                        m(ChpInformationChannel.ChannelId.RESERVE, new UnsignedDoublewordElement(0x403C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(0x403E, 0x403E)));

    }


    private class ChpErrorWorker extends AbstractCycleWorker {

        @Override
        public void activate(String name) {
            super.activate(name);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }

        @Override
        protected void forever() throws Throwable {

            char[] errorOne = String.format("%16s", Integer.toBinaryString(getErrorOne().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorTwo = String.format("%16s", Integer.toBinaryString(getErrorTwo().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorThree = String.format("%16s", Integer.toBinaryString(getErrorThree().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorFour = String.format("%16s", Integer.toBinaryString(getErrorFour().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorFive = String.format("%16s", Integer.toBinaryString(getErrorFive().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorSix = String.format("%16s", Integer.toBinaryString(getErrorSix().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorSeven = String.format("%16s", Integer.toBinaryString(getErrorSeven().getNextValue().get())).replace(" ", "0").toCharArray();
            char[] errorEight = String.format("%16s", Integer.toBinaryString(getErrorEight().getNextValue().get())).replace(" ", "0").toCharArray();

            List<String> errorSummary = new ArrayList<>();

            //Zuordnen error one 0-15; error2 16-23; error 3 24-31; error4 32-39; error5 40-47 Rest erstmal egal
            // errorListPosition --> for Actual List of Erroroccurence
            // multiplierForLimit --> important for 8/16/24/32/40 etc --> correct Channels are read
            // %8 for correct position in char array
            boolean errorFound = false;
            int errorMax = 80;
            int errorBitLength = 16;
            for (int i = 0, errorListPosition = 0, multiplierForLimit = 1; i < errorMax; i++) {
                if (i < errorBitLength && errorOne[i] == '1') {

                    errorSummary.add(errorListPosition, errorPossibilities[i % errorBitLength]);
                    errorFound = true;
                } else if (errorBitLength * multiplierForLimit <= i && i < (errorBitLength * (multiplierForLimit + 1)) && errorTwo[i % errorBitLength] == '1') {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorFound = true;
                } else if (errorBitLength * multiplierForLimit <= i && i < (errorBitLength * (multiplierForLimit + 1)) && errorThree[i % errorBitLength] == '1') {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorFound = true;
                } else if (errorBitLength * multiplierForLimit <= i && i < (errorBitLength * (multiplierForLimit + 1)) && errorFour[i % errorBitLength] == '1') {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorFound = true;
                } else if (errorBitLength * multiplierForLimit <= i && i < (errorBitLength * (multiplierForLimit + 1)) && errorFive[i % errorBitLength] == '1') {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorFound = true;
                } else if (errorBitLength * multiplierForLimit <= i && i < (errorBitLength * (multiplierForLimit + 1)) && errorSix[i % errorBitLength] == '1') {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorFound = true;
                }
                //                else if (errorBitLength*j <= i && i<(errorBitLength*(j+1)) && errorSeven[i % errorBitLength] == '1') {
                //                    errorSummary.add(k, errorPossibilities[i]);
                //                    errorFound = true;
                //                }
                //                else if (errorBitLength*j <= i && i<(errorBitLength*(j+1)) && errorEight[i % errorBitLength] == '1') {
                //                    errorSummary.add(k, errorPossibilities[i]);
                //                    errorFound = true;
                //                }
                if (i % (errorBitLength) == 1) {
                    multiplierForLimit++;
                }
                if (errorFound) {
                    errorListPosition++;
                    errorFound = false;
                }
            }
            //All occuring errors in openemsChannel.
            getErrorChannel().setNextValue(errorSummary.toString());

        }


    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE)) {
            this.chpErrorWorker.triggerNextRun();
        }
    }

}
