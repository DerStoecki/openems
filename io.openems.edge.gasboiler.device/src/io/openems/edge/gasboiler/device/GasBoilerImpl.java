package io.openems.edge.gasboiler.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC2ReadInputsTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.gasboiler.device.api.GasBoiler;
import io.openems.edge.gasboiler.device.api.GasBoilerData;
import io.openems.edge.heater.api.Heater;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "GasBoiler",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class GasBoilerImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, GasBoilerData, GasBoiler, Heater {

    //in kW
    private int thermicalOutput = 66;

    @Reference
    protected ConfigurationAdmin cm;

    public GasBoilerImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GasBoilerData.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbusBridgeId());
        if (config.maxThermicalOutput() != 0) {
            this.thermicalOutput = config.maxThermicalOutput();
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC2ReadInputsTask(1, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_MODULATION_VALUE, new UnsignedWordElement(1),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(2, 2),
                        m(GasBoilerData.ChannelId.WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE, new UnsignedWordElement(3),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(4),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(8, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_ACTUAL, new UnsignedWordElement(8),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(11, Priority.LOW,
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE_EFFECTIVE, new UnsignedWordElement(11),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        // 1% in Channel == 0.5% in real
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_PERFORMANCE_EFFECTIVE, new UnsignedWordElement(12),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(17, Priority.LOW,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(17),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_PREPARATION, new UnsignedWordElement(17),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC2ReadInputsTask(27, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_BOILER_TEMPERATURE, new UnsignedWordElement(27),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_A, new UnsignedWordElement(28),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_B, new UnsignedWordElement(29),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_EXHAUST_TEMPERATURE, new UnsignedWordElement(30),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.REWIND_TEMPERATURE_17A, new UnsignedWordElement(31),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.REWIND_TEMPERATURE_17B, new UnsignedWordElement(32),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.SENSOR_9, new UnsignedWordElement(33),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.SETPOINT_EA_1, new UnsignedWordElement(34),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(37, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_1, new UnsignedWordElement(37),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_2, new UnsignedWordElement(38),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_START_COUNTER, new UnsignedWordElement(39),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_MODE, new UnsignedWordElement(40),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE, new UnsignedWordElement(41),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OPERATING_HOURS_COMBUSTION_ENGINE_TIER_1_EXPANDED, new UnsignedWordElement(42),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(47, Priority.LOW,
                        m(GasBoilerData.ChannelId.AMBIENT_TEMPERATURE, new SignedWordElement(47),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_SIGNAL_PM_1, new UnsignedWordElement(48),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.VOLUME_FLOW_SET_POINT_PM_1, new SignedWordElement(49),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_1, new SignedWordElement(50),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_2, new SignedWordElement(51),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_3, new SignedWordElement(52),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_4, new SignedWordElement(53),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_1_PM_1_STATUS, new SignedWordElement(54),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_2_PM_1_STATUS, new SignedWordElement(55),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        ),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_3_PM_1_STATUS, new SignedWordElement(56),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        ),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_4_PM_1_STATUS, new SignedWordElement(57),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        )),
                new FC4ReadInputRegistersTask(1, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_STATUS, new UnsignedWordElement(1))),
                new FC4ReadInputRegistersTask(4, Priority.LOW,
                        m(GasBoilerData.ChannelId.TRIBUTARY_PUMP, new UnsignedWordElement(4),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_20, new UnsignedWordElement(5),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_29, new UnsignedWordElement(6),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_EA_1, new UnsignedWordElement(7),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_AM_1_1, new UnsignedWordElement(8),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_AM_1_2, new UnsignedWordElement(9),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_1, new UnsignedWordElement(10),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_2, new UnsignedWordElement(11),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_3, new UnsignedWordElement(12),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_CHARGE_PUMP, new UnsignedWordElement(13),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_CIRCULATION_PUMP, new UnsignedWordElement(14),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC4ReadInputRegistersTask(16, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_ON_OFF, new UnsignedWordElement(16),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.GRID_VOLTAGE_BEHAVIOUR_PM_1, new UnsignedWordElement(17),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.FLOATING_ELECTRICAL_CONTACT_PM_1, new UnsignedWordElement(18),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.DISTURBANCE_INPUT_PM_1, new UnsignedWordElement(19),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(1, Priority.HIGH,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT, new UnsignedWordElement(1),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE, new UnsignedWordElement(2),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(5, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_OPERATION_MODE, new UnsignedWordElement(5),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(7, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS, new UnsignedWordElement(7),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE, new UnsignedWordElement(8),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_OPERATION_MODE, new UnsignedWordElement(9),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(10, Priority.HIGH,
                        m(GasBoilerData.ChannelId.FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE, new UnsignedWordElement(10),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(13, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT, new UnsignedWordElement(13),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(15, Priority.HIGH,
                        m(GasBoilerData.ChannelId.BOILER_MAX_REACHED_EXHAUST_TEMPERATURE, new UnsignedWordElement(15),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OPERATING_MODE_A1_M1, new UnsignedWordElement(16),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC6WriteRegisterTask(1,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT, new UnsignedWordElement(1))
                ),
                new FC6WriteRegisterTask(2,
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE, new UnsignedWordElement(2))),
                new FC6WriteRegisterTask(5,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_OPERATION_MODE, new UnsignedWordElement(5))
                ),
                new FC6WriteRegisterTask(7,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS, new UnsignedWordElement(7))
                ),
                new FC6WriteRegisterTask(8,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE, new UnsignedWordElement(8))),
                new FC6WriteRegisterTask(9,
                        m(GasBoilerData.ChannelId.WARM_WATER_OPERATION_MODE, new UnsignedWordElement(9))),
                new FC6WriteRegisterTask(10,
                        m(GasBoilerData.ChannelId.FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE, new UnsignedWordElement(10))),
                new FC6WriteRegisterTask(13,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT, new UnsignedWordElement(13))),
                new FC6WriteRegisterTask(15,
                        m(GasBoilerData.ChannelId.BOILER_MAX_REACHED_EXHAUST_TEMPERATURE, new UnsignedWordElement(15))),
                new FC6WriteRegisterTask(16,
                        m(GasBoilerData.ChannelId.OPERATING_MODE_A1_M1, new UnsignedWordElement(16)))
        );
    }

    @Override
    public int calculateProvidedPower(int demand, float bufferValue) throws OpenemsError.OpenemsNamedException {
        int providedPower = Math.round(demand * bufferValue);
        if (providedPower >= thermicalOutput) {
            //for boiler 1 == 0.5%
            getHeatBoilerPerformanceSetPointValue().setNextWriteValue(200);
            getHeatBoilerPerformanceSetPointValuePercent().setNextValue(100);
            return thermicalOutput;

        } else {
            //for boiler
            getHeatBoilerPerformanceSetPointValue().setNextWriteValue((providedPower / thermicalOutput) * 200);
            //for user
            getHeatBoilerPerformanceSetPointValuePercent().setNextValue((providedPower / thermicalOutput) * 100);
            return providedPower;
        }


    }

    @Override
    public int getMaximumThermicalOutput() {
        return 0;
    }

    @Override
    public void setOffline() throws OpenemsError.OpenemsNamedException {
        getHeatBoilerPerformanceSetPointValue().setNextWriteValue(0);
    }
}
