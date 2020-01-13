package io.openems.edge.chp.device;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.*;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.chp.device.task.ChpTaskImpl;
import io.openems.edge.chp.module.api.ChpModule;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.i2c.mcp.api.Mcp;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Chp",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class ChpImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, PowerLevel {
    private Mcp mcp;
    private ChpType chpType;
    private static int UnitIdCounter = 0;
    @Reference
    ConfigurationAdmin cm;

    @Reference
    protected ComponentManager cpm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public ChpImpl() {
        super(OpenemsComponent.ChannelId.values(),
                PowerLevel.ChannelId.values(),
                ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled(), UnitIdCounter, this.cm, "Modbus", config.modbusBridgeId());


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

        if (cpm.getComponent(config.chpModuleId()) instanceof ChpModule) {
            ChpModule chpModule = cpm.getComponent(config.chpModuleId());
            mcp = chpModule.getMcp();
            mcp.addTask(super.id(), new ChpTaskImpl(super.id(),
                    config.position(), config.minLimit(), config.maxLimit(),
                    config.percentageRange(), 4096.f, this.getPowerLevelChannel()));
        }
        UnitIdCounter++;
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.mcp.removeTask(super.id());
    }


    @Override
    public String debugLog() {
        String chpInformation = getInformation();
        if (this.getPowerLevelChannel().getNextValue().get() != null) {
            if (chpType != null) {
                return "Chp: " + this.chpType.getName() + "is at " + this.getPowerLevelChannel().getNextValue().get();
            } else {
                return "Chp is at " + this.getPowerLevelChannel().getNextValue().get();
            }
        }
        return "Percentage Level at 0";
    }

    private String getInformation() {

        return null;
    }

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        MODUS(Doc.of(OpenemsType.INTEGER)),
        STATUS(Doc.of(OpenemsType.INTEGER)),
        OPERATING_MODE(Doc.of(OpenemsType.INTEGER)),
        SET_POINT_OPERATION_MODE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        ERROR_BITS_1(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_2(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_3(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_4(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_5(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_6(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_7(Doc.of(OpenemsType.INTEGER)),
        ERROR_BITS_8(Doc.of(OpenemsType.INTEGER)),
        OPERATING_HOURS(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        OPERATING_MINUTES(Doc.of(OpenemsType.INTEGER).unit(Unit.MINUTE)),
        START_COUNTER(Doc.of(OpenemsType.INTEGER)),
        MAINTENANCE_INTERVAL(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        MODULE_LOCK(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        WARNING_TIME(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        NEXT_MAINTENANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        EXHAUST_A(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        EXHAUST_B(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        EXHAUST_C(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        EXHAUST_D(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_2(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_3(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_4(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_5(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        PT_100_6(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        OIL_PRESSURE(Doc.of(OpenemsType.INTEGER).unit(Unit.BAR)),
        LAMBDA_PROBE_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        ROTATION_PER_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.ROTATION_PER_MINUTE)),
        TEMPERATURE_CONTROLLER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),
        TEMPERATURE_CLEARANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),
        SUPPLY_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        SUPPLY_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        SUPPLY_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_VOLTAGE_L1(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_VOLTAGE_L2(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_VOLTAGE_L3(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_ELECTRICITY_L1(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        GENERATOR_ELECTRICITY_L2(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        GENERATOR_ELECTRICITY_L3(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        SUPPLY_VOLTAGE_TOTAL(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_VOLTAGE_TOTAL(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GENERATOR_ELECTRICITY_TOTAL(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        ENGINE_PERFORMANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT)),
        SUPPLY_FREQUENCY(Doc.of(OpenemsType.FLOAT).unit(Unit.HERTZ)),
        GENERATOR_FREQUENCY(Doc.of(OpenemsType.FLOAT).unit(Unit.HERTZ)),
        ACTIVE_POWER_FACTOR(Doc.of(OpenemsType.FLOAT)),
        RESERVE(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT_HOURS));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC3ReadRegistersTask(0x4001, Priority.LOW,
                        m(ChannelId.MODUS, new UnsignedWordElement(0x4001),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.STATUS, new UnsignedWordElement(0x4002),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.OPERATING_MODE, new UnsignedWordElement(0x4003),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SET_POINT_OPERATION_MODE, new SignedWordElement(0x4004),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_1, new UnsignedWordElement(0x4005),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_2, new UnsignedWordElement(0x4006),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_3, new UnsignedWordElement(0x4007),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_4, new UnsignedWordElement(0x4008),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_5, new UnsignedWordElement(0x4009),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_6, new UnsignedWordElement(0x400A),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_7, new UnsignedWordElement(0x400B),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ERROR_BITS_8, new UnsignedWordElement(0x400C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.OPERATING_HOURS, new UnsignedWordElement(0x400D),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.OPERATING_MINUTES, new UnsignedWordElement(0x400E),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.START_COUNTER, new UnsignedWordElement(0x400F),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.MAINTENANCE_INTERVAL, new SignedWordElement(0x4010),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.MODULE_LOCK, new SignedWordElement(0x411),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.WARNING_TIME, new SignedWordElement(0x4012),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.NEXT_MAINTENANCE, new UnsignedWordElement(0x4013),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.EXHAUST_A, new SignedWordElement(0x4014),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.EXHAUST_B, new SignedWordElement(0x4015),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.EXHAUST_C, new SignedWordElement(0x4016),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.EXHAUST_D, new SignedWordElement(0x4017),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_1, new SignedWordElement(0x4018),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_2, new SignedWordElement(0x4019),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_3, new SignedWordElement(0x401A),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_4, new SignedWordElement(0x401B),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_5, new SignedWordElement(0x401C),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.PT_100_6, new SignedWordElement(0x401D),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.BATTERY_VOLTAGE, new SignedWordElement(0x401E),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.OIL_PRESSURE, new SignedWordElement(0x401F),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        m(ChannelId.LAMBDA_PROBE_VOLTAGE, new SignedWordElement(0x4020),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
                        new DummyRegisterElement(0x4021, 0x4024),
                        m(ChannelId.ROTATION_PER_MIN, new UnsignedWordElement(0x4025),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.TEMPERATURE_CONTROLLER, new SignedWordElement(0x4026),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.TEMPERATURE_CLEARANCE, new SignedWordElement(0x4027),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SUPPLY_VOLTAGE_L1, new SignedWordElement(0x4028),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SUPPLY_VOLTAGE_L2, new SignedWordElement(0x4029),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SUPPLY_VOLTAGE_L3, new SignedWordElement(0x402A),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_VOLTAGE_L1, new SignedWordElement(0x402B),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_VOLTAGE_L2, new SignedWordElement(0x402C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_VOLTAGE_L3, new SignedWordElement(0x402D),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_ELECTRICITY_L1, new SignedWordElement(0x402E),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_ELECTRICITY_L2, new SignedWordElement(0x402F),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_ELECTRICITY_L3, new SignedWordElement(0x4030),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SUPPLY_VOLTAGE_TOTAL, new SignedWordElement(0x4031),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_VOLTAGE_TOTAL, new SignedWordElement(0x4032),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.GENERATOR_ELECTRICITY_TOTAL, new SignedWordElement(0x4033),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.ENGINE_PERFORMANCE, new SignedWordElement(0x4034),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(ChannelId.SUPPLY_FREQUENCY, new FloatDoublewordElement(0x4035),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(0x4036, 0x4036),
                        m(ChannelId.GENERATOR_FREQUENCY, new FloatDoublewordElement(0x4037),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(0x4039, 0x403A),
                        m(ChannelId.ACTIVE_POWER_FACTOR, new SignedWordElement(0x403B),
                                ElementToChannelConverter.SCALE_FACTOR_MINUS_3),
                        m(ChannelId.RESERVE, new UnsignedDoublewordElement(0x403C),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(0x403E, 0x403E)));

    }


}
