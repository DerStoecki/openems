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

    GasBoilerType gasBoilerType;


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
        allocateGasBoilerType(config.gasBoilerType());
    }

    private void allocateGasBoilerType(String gasBoilerType) {

        switch (gasBoilerType) {

            case "Placeholder":
            case "VITOTRONIC_100":
                this.gasBoilerType = GasBoilerType.VITOTRONIC_100;
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
                        m(GasBoilerData.ChannelId.HEAT_BOILER_MODULATION_VALUE, new UnsignedWordElement(this.gasBoilerType.heatBoilerModulationValueAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(2, 2),
                        m(GasBoilerData.ChannelId.WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.warmWaterEffectiveSetPointTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(this.gasBoilerType.heatBoilerTemperatureSetPointEffectiveAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(8, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_ACTUAL, new UnsignedWordElement(this.gasBoilerType.heatBoilerTemperatureActualAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(11, Priority.LOW,
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE_EFFECTIVE, new UnsignedWordElement(this.gasBoilerType.boilerSetPointTemperatureEffectiveAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        // 1% in Channel == 0.5% in real
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_PERFORMANCE_EFFECTIVE, new UnsignedWordElement(this.gasBoilerType.boilerSetPointPerformanceEffectiveAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(17, Priority.LOW,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(this.gasBoilerType.warmWaterTemperatureSetPointAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_PREPARATION, new UnsignedWordElement(this.gasBoilerType.warmWaterPreparationAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC2ReadInputsTask(27, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_BOILER_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.combustionEngineBoilerTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_A, new UnsignedWordElement(this.gasBoilerType.warmWaterStorageTemperature_5_A_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_B, new UnsignedWordElement(this.gasBoilerType.warmWaterStorageTemperature_5_B_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_EXHAUST_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.combustionEngineExhaustTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.REWIND_TEMPERATURE_17A, new UnsignedWordElement(this.gasBoilerType.rewindTemperature_17_A_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.REWIND_TEMPERATURE_17B, new UnsignedWordElement(this.gasBoilerType.rewindTemperature_17_B_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.SENSOR_9, new UnsignedWordElement(this.gasBoilerType.sensor9address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.SETPOINT_EA_1, new UnsignedWordElement(this.gasBoilerType.setPoint_EA_1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(37, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_1, new UnsignedWordElement(this.gasBoilerType.combustionEngineOperatingHoursTier1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_2, new UnsignedWordElement(this.gasBoilerType.combustionEngineOperatingHoursTier2Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_START_COUNTER, new UnsignedWordElement(this.gasBoilerType.combustionEngineStartCounterAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_OPERATING_MODE, new UnsignedWordElement(this.gasBoilerType.combustionEngineOperatingModeAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE, new UnsignedWordElement(this.gasBoilerType.combustionEngineEfficiencyActualValueAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OPERATING_HOURS_COMBUSTION_ENGINE_TIER_1_EXPANDED, new UnsignedWordElement(this.gasBoilerType.operatingHoursCombustionEngineTier_1_expandedAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC2ReadInputsTask(47, Priority.LOW,
                        m(GasBoilerData.ChannelId.AMBIENT_TEMPERATURE, new SignedWordElement(this.gasBoilerType.ambientTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_SIGNAL_PM_1, new UnsignedWordElement(this.gasBoilerType.outPutSignalPM1_Percent),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.VOLUME_FLOW_SET_POINT_PM_1, new SignedWordElement(this.gasBoilerType.volumeFlowSetPointPm1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_1, new SignedWordElement(this.gasBoilerType.temperatureSensorPM_1_1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_2, new SignedWordElement(this.gasBoilerType.temperatureSensorPM1_2Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_3, new SignedWordElement(this.gasBoilerType.temperatureSensorPM1_3Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURESENSOR_PM_1_4, new SignedWordElement(this.gasBoilerType.temperatureSensorPM1_4Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_1_PM_1_STATUS, new SignedWordElement(this.gasBoilerType.temperatureSensor_1_PM_1_StatusAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_2_PM_1_STATUS, new SignedWordElement(this.gasBoilerType.temperatureSensor_1_PM_2_StatusAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        ),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_3_PM_1_STATUS, new SignedWordElement(this.gasBoilerType.temperatureSensor_1_PM_3_StatusAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        ),
                        m(GasBoilerData.ChannelId.TEMPERATURE_SENSOR_4_PM_1_STATUS, new SignedWordElement(this.gasBoilerType.temperatureSensor_1_PM_4_StatusAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1
                        )),
                new FC4ReadInputRegistersTask(1, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_STATUS, new UnsignedWordElement(this.gasBoilerType.heatBoilerPerformanceStatusAddress))),
                new FC4ReadInputRegistersTask(4, Priority.LOW,
                        m(GasBoilerData.ChannelId.TRIBUTARY_PUMP, new UnsignedWordElement(this.gasBoilerType.tributaryPumpAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_20, new UnsignedWordElement(this.gasBoilerType.output20Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_29, new UnsignedWordElement(this.gasBoilerType.output_29Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_EA_1, new UnsignedWordElement(this.gasBoilerType.outPutEA1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_AM_1_1, new UnsignedWordElement(this.gasBoilerType.outPutAM1_1_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OUTPUT_AM_1_2, new UnsignedWordElement(this.gasBoilerType.outPutAm1_2_Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_1, new UnsignedWordElement(this.gasBoilerType.input_EA_1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_2, new UnsignedWordElement(this.gasBoilerType.input_EA_2Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.INPUT_EA_3, new UnsignedWordElement(this.gasBoilerType.input_EA_3Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_STORAGE_CHARGE_PUMP, new UnsignedWordElement(this.gasBoilerType.warmWaterStorageChargePumpAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_CIRCULATION_PUMP, new UnsignedWordElement(this.gasBoilerType.warmWaterCirculationPumpAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC4ReadInputRegistersTask(16, Priority.LOW,
                        m(GasBoilerData.ChannelId.COMBUSTION_ENGINE_ON_OFF, new UnsignedWordElement(this.gasBoilerType.combustionEngineOnOffAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.GRID_VOLTAGE_BEHAVIOUR_PM_1, new UnsignedWordElement(this.gasBoilerType.gridVoltageBehaviourPM1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.FLOATING_ELECTRICAL_CONTACT_PM_1, new UnsignedWordElement(this.gasBoilerType.floatingElectricalContactPm1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.DISTURBANCE_INPUT_PM_1, new UnsignedWordElement(this.gasBoilerType.disturbanceInputPM1Address),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(1, Priority.HIGH,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT, new UnsignedWordElement(this.gasBoilerType.warmWaterTemperatureSetPointAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.boilerSetPointTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(5, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_OPERATION_MODE, new UnsignedWordElement(this.gasBoilerType.heatBoilerOperationModeAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(7, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS, new UnsignedWordElement(this.gasBoilerType.heatBoilerPerformanceSetPointStatusAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE, new UnsignedWordElement(this.gasBoilerType.heatBoilerPerformanceSetPointValueAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.WARM_WATER_OPERATION_MODE, new UnsignedWordElement(this.gasBoilerType.warmWaterOperationModeAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)
                ),
                new FC3ReadRegistersTask(10, Priority.HIGH,
                        m(GasBoilerData.ChannelId.FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.functioningWarmWaterSetPointTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(13, Priority.HIGH,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT, new UnsignedWordElement(this.gasBoilerType.heatBoilerTemperatureSetPointAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC3ReadRegistersTask(15, Priority.HIGH,
                        m(GasBoilerData.ChannelId.BOILER_MAX_REACHED_EXHAUST_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.boilerMaxReachedExhaustTemperatureAddress),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        m(GasBoilerData.ChannelId.OPERATING_MODE_A1_M1, new UnsignedWordElement(this.gasBoilerType.operatingModeA1_M1),
                                ElementToChannelConverter.DIRECT_1_TO_1)),
                new FC6WriteRegisterTask(1,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT, new UnsignedWordElement(this.gasBoilerType.warmWaterTemperatureSetPointAddress))
                ),
                new FC6WriteRegisterTask(2,
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.boilerSetPointTemperatureAddress))),
                new FC6WriteRegisterTask(5,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_OPERATION_MODE, new UnsignedWordElement(this.gasBoilerType.heatBoilerOperationModeAddress))
                ),
                new FC6WriteRegisterTask(7,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS, new UnsignedWordElement(this.gasBoilerType.heatBoilerPerformanceSetPointStatusAddress))
                ),
                new FC6WriteRegisterTask(8,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE, new UnsignedWordElement(this.gasBoilerType.heatBoilerPerformanceSetPointValueAddress))),
                new FC6WriteRegisterTask(9,
                        m(GasBoilerData.ChannelId.WARM_WATER_OPERATION_MODE, new UnsignedWordElement(this.gasBoilerType.warmWaterOperationModeAddress))),
                new FC6WriteRegisterTask(10,
                        m(GasBoilerData.ChannelId.FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.functioningWarmWaterSetPointTemperatureAddress))),
                new FC6WriteRegisterTask(13,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT, new UnsignedWordElement(this.gasBoilerType.heatBoilerTemperatureSetPointAddress))),
                new FC6WriteRegisterTask(15,
                        m(GasBoilerData.ChannelId.BOILER_MAX_REACHED_EXHAUST_TEMPERATURE, new UnsignedWordElement(this.gasBoilerType.boilerMaxReachedExhaustTemperatureAddress))),
                new FC6WriteRegisterTask(16,
                        m(GasBoilerData.ChannelId.OPERATING_MODE_A1_M1, new UnsignedWordElement(this.gasBoilerType.operatingModeA1_M1)))
        );
    }

    @Override
    public int calculateProvidedPower(int demand, float bufferValue) throws OpenemsError.OpenemsNamedException {
        float providedPower = Math.round(demand * bufferValue);
        if (providedPower >= thermicalOutput) {
            //for boiler 1 == 0.5%
            getHeatBoilerPerformanceSetPointValue().setNextWriteValue(200);
            getHeatBoilerPerformanceSetPointValuePercent().setNextValue(100);
            return thermicalOutput;

        } else {
            //for boiler
            getHeatBoilerPerformanceSetPointValue().setNextWriteValue((int) Math.floor((providedPower / (float) thermicalOutput) * 200));
            //for user
            getHeatBoilerPerformanceSetPointValuePercent().setNextValue((int) Math.floor((providedPower / (float) thermicalOutput) * 100));
            return (int) providedPower;
        }


    }

    @Override
    public int getMaximumThermicalOutput() {
        return this.thermicalOutput;
    }

    @Override
    public void setOffline() throws OpenemsError.OpenemsNamedException {
        getHeatBoilerPerformanceSetPointValue().setNextWriteValue(0);
        getHeatBoilerPerformanceSetPointValuePercent().setNextValue(0);
    }
}
