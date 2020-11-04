package io.openems.edge.chp.device;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;

import io.openems.edge.bridge.modbus.api.element.*;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.chp.device.api.ChpInformationChannel;
import io.openems.edge.chp.device.api.ChpPowerPercentage;
import io.openems.edge.chp.device.task.ChpTaskImpl;
import io.openems.edge.chp.module.api.ChpModule;

import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.heater.api.Heater;
import io.openems.edge.i2c.mcp.api.Mcp;

import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;


import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Chp",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class ChpImplViessmann extends AbstractOpenemsModbusComponent implements OpenemsComponent, ChpInformationChannel, EventHandler, Heater {

    private final Logger log = LoggerFactory.getLogger(ChpImplViessmann.class);
    private Mcp mcp;
    private ChpType chpType;
    private int thermicalOutput;
    private int electricalOutput;
    private ActuatorRelaysChannel relay;
    private boolean useRelay;
    private AccessChp accessChp;

    private Config config;

    private String[] errorPossibilities = ErrorPossibilities.STANDARD_ERRORS.getErrorList();

    @Reference
    protected ConfigurationAdmin cm;

    @Reference
    protected ComponentManager cpm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public ChpImplViessmann() {
        super(OpenemsComponent.ChannelId.values(),
                ChpPowerPercentage.ChannelId.values(),
                ChpInformationChannel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbusBridgeId());
        this.config = config;
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
        this.accessChp = AccessChp.READ;
        this.useRelay = config.useRelay();
        if (config.accesMode().equals("rw")) {
            this.accessChp = AccessChp.READWRITE;
            if (cpm.getComponent(config.chpModuleId()) instanceof ChpModule) {
                ChpModule chpModule = cpm.getComponent(config.chpModuleId());
                mcp = chpModule.getMcp();
                mcp.addTask(super.id(), new ChpTaskImpl(super.id(),
                        config.position(), config.minLimit(), config.maxLimit(),
                        config.percentageRange(), 4096.f, this.getPowerLevelChannel()));
            }
            if (this.useRelay == true) {
                if (cpm.getComponent(config.relayId()) instanceof ActuatorRelaysChannel) {
                    this.relay = cpm.getComponent(config.relayId());
                    this.relay.getRelaysChannel().setNextWriteValue(false);
                }

                if (config.startOnActivation()) {
                    this.getPowerLevelChannel().setNextValue(config.startPercentage());
                    if (this.useRelay) {
                        this.relay.getRelaysChannel().setNextWriteValue(true);
                    }
                }

            }
        }
        this.thermicalOutput = Math.round(this.chpType.getThermalOutput());
        this.electricalOutput = Math.round(this.chpType.getElectricalOutput());
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        if (this.accessChp == AccessChp.READWRITE) {
            this.mcp.removeTask(super.id());
        }
        if (this.useRelay) {
            try {
                this.relay.getRelaysChannel().setNextWriteValue(false);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String debugLog() {
        //getInformations();
        if (this.accessChp == AccessChp.READWRITE) {
            if (this.getPowerLevelChannel().getNextValue().get() != null) {
                if (chpType != null) {
                    return "Chp: " + this.chpType.getName() + "is at " + this.getPowerLevelChannel().getNextValue().get()
                            + "\nErrors in Chp: "
                            + this.getErrorChannel().getNextValue().toString() + "\n";
                } else {
                    return "Chp is at " + this.getPowerLevelChannel().getNextValue().get() + "\nErrors in Chp: "
                            + this.getErrorChannel().getNextValue().toString() + "\n";
                }
            }
            return "Percentage Level at 0\n";
        } else {
            return this.getErrorChannel().getNextValue().get() + "\n";
        }
    }

    /*private void getInformations() {
        List<Channel<?>> all = new ArrayList<>();
        System.out.println("-----------------------------" + super.id() + "-----------------------------");
        Arrays.stream(ChpInformationChannel.ChannelId.values()).forEach(consumer -> {
            if (!consumer.id().contains("ErrorBits")) {
                all.add(this.channel(consumer));
            }
        });
        all.forEach(consumer -> System.out.println(consumer.channelId().id() + " value: " + (consumer.value().isDefined() ? consumer.value().get() : "UNDEFINED ") + (consumer.channelDoc().getUnit().getSymbol())));
        System.out.println("----------------------------------------------------------");
    }*/


    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC3ReadRegistersTask(0x4000, Priority.LOW,
                        new DummyRegisterElement(0x4000, 0x4000),
                        m(ChpInformationChannel.ChannelId.MODE, new UnsignedWordElement(0x4001),
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
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_B, new SignedWordElement(0x4015),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_C, new SignedWordElement(0x4016),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.EXHAUST_D, new SignedWordElement(0x4017),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_1, new SignedWordElement(0x4018),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_2, new SignedWordElement(0x4019),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_3, new SignedWordElement(0x401A),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_4, new SignedWordElement(0x401B),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_5, new SignedWordElement(0x401C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.PT_100_6, new SignedWordElement(0x401D),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.BATTERY_VOLTAGE, new SignedWordElement(0x401E),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.OIL_PRESSURE, new SignedWordElement(0x401F),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.LAMBDA_PROBE_VOLTAGE, new SignedWordElement(0x4020),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(0x4025, Priority.LOW,
                        m(ChpInformationChannel.ChannelId.ROTATION_PER_MIN, new UnsignedWordElement(0x4025),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.TEMPERATURE_CONTROLLER, new SignedWordElement(0x4026),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.TEMPERATURE_CLEARANCE, new SignedWordElement(0x4027),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
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
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChpInformationChannel.ChannelId.RESERVE, new UnsignedDoublewordElement(0x403C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(0x403E, 0x403E)));

    }

    @Override
    public int calculateProvidedPower(int demand, float bufferValue) throws OpenemsError.OpenemsNamedException {
        //percent
        if (this.accessChp.equals(AccessChp.READWRITE)) {
            if (this.isErrorOccured().value().isDefined() && this.isErrorOccured().value().get()) {
                return 0;
            }
            int providedPower = Math.round(((demand * bufferValue) * 100) / thermicalOutput);
            if (this.useRelay == true) {
                this.relay.getRelaysChannel().setNextWriteValue(true);
            }

            if (providedPower >= 100) {

                getPowerLevelChannel().setNextWriteValue(100);
                return thermicalOutput;

            } else {
                getPowerLevelChannel().setNextWriteValue(providedPower);
                providedPower = providedPower < this.config.startPercentage() ? config.startPercentage() : providedPower;
                return (providedPower * thermicalOutput) / 100;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getMaximumThermicalOutput() {
        return thermicalOutput;
    }

    @Override
    public void setOffline() throws OpenemsError.OpenemsNamedException {
        if (this.useRelay == true) {
            this.relay.getRelaysChannel().setNextWriteValue(true);
        }
        getPowerLevelChannel().setNextWriteValue(0);
    }

    @Override
    public int runFullPower() {
        if (this.isErrorOccured().value().isDefined() && this.isErrorOccured().value().get()) {
            return 0;
        }
        try {

            if (this.useRelay == true) {
                this.relay.getRelaysChannel().setNextWriteValue(true);
            }
            this.getPowerLevelChannel().setNextWriteValue(100);
        } catch (OpenemsError.OpenemsNamedException e) {
            log.warn("Couldn't Write into Channel! " + e.getMessage());
            return 0;
        }
        return this.getMaximumThermicalOutput();
    }


    private void forever() {
        List<String> errorSummary = new ArrayList<>();

        char[] allErrorsAsChar = generateErrorAsCharArray();

        int errorMax = 80;
        //int errorBitLength = 16;
        for (int i = 0, errorListPosition = 0; i < errorMax; i++) {
            if (allErrorsAsChar[i] == '1') {
                if (errorPossibilities[i].toLowerCase().contains("reserve")) {
                    errorListPosition++;
                } else {
                    errorSummary.add(errorListPosition, errorPossibilities[i]);
                    errorListPosition++;
                }
            }
        }
        //All occuring errors in openemsChannel.

        if ((errorSummary.size() > 0)) {
            getErrorChannel().setNextValue(errorSummary.toString());
            isErrorOccured().setNextValue(true);
        } else {
            getErrorChannel().setNextValue("No Errors found.");
            isErrorOccured().setNextValue(false);
        }

    }

    private char[] generateErrorAsCharArray() {

        String errorBitsAsString = "";
        String dummyString = "0000000000000000";
        if (getErrorOne().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorOne().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorTwo().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorTwo().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorThree().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorThree().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorFour().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorFour().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorFive().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorFive().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorSix().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorSix().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorSeven().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorSeven().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }
        if (getErrorEight().getNextValue().isDefined()) {
            errorBitsAsString += String.format("%16s", Integer.toBinaryString(getErrorEight().getNextValue().get())).replace(' ', '0');
        } else {
            errorBitsAsString += dummyString;
        }

        return errorBitsAsString.toCharArray();
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.forever();
        }
    }
}
