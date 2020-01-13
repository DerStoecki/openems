package io.openems.edge.chp.device;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.chp.device.task.ChpTaskImpl;
import io.openems.edge.chp.module.api.ChpModule;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.i2c.mcp.api.Mcp;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Chp",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class ChpImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, PowerLevel {
    private Mcp mcp;
    private ChpType chpType;
    @Reference
    protected ComponentManager cpm;

    public ChpImpl() {
        super(OpenemsComponent.ChannelId.values(), PowerLevel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());

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

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.mcp.removeTask(super.id());
    }


    @Override
    public String debugLog() {
        String chpInformations = getInformations();
        if (this.getPowerLevelChannel().getNextValue().get() != null) {
            if (chpType != null) {
                return "Chp: " + this.chpType.getName() + "is at " + this.getPowerLevelChannel().getNextValue().get();
            } else {
                return "Chp is at " + this.getPowerLevelChannel().getNextValue().get();
            }
        }
        return "Percentage Level at 0";
    }

    private String getInformations() {
        return null;
    }

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        MODUS(Doc.of(OpenemsType.INTEGER)),
        STATUS(Doc.of(OpenemsType.INTEGER)),
        MODE(Doc.of(OpenemsType.INTEGER)),
        SETPOINT_OPERATION_MODE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
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
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        OIL_PRESSURE(Doc.of(OpenemsType.INTEGER).unit(Unit.BAR)),
        LAMBDA_PROBE_Voltage(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        ROTATION_PER_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.ROTATION_PER_MINUTE)),
        TEMPERATURE_CONTROLLER(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURE_CLEARANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
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
        ENGINE_PERFORMANCE(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT)),
        SUPPLY_FREQUENCY(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ)),
        GENERATOR_FREQUENCY(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ)),
        ACTIVE_POWER_FACTOR(Doc.of(OpenemsType.INTEGER)),
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
                new FC16WriteRegistersTask(0xFA0))
    }


}
