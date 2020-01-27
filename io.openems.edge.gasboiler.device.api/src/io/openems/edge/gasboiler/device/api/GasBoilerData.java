package io.openems.edge.gasboiler.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface GasBoilerData extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        /*
         * Informations will be got by ModBus.
         * That's why the Percentage Values got 2 Channels. 1 To Receive and Write the Correct Value and 1 for
         * human readable und writable values.
         *
         * */


        /**
         * Output 1 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_AM_1_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Output 2 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_AM_1_2(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Output 20 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_20(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Output 2 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_29(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Ambient Temperature in °C.
         * <li>Type: Integer</li>
         * <li>Unit: Degree Celsius</li>
         */
        AMBIENT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Output EA 1 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_EA_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Input EA 1-3 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = ON
         */
        INPUT_EA_1(Doc.of(OpenemsType.BOOLEAN)),
        INPUT_EA_2(Doc.of(OpenemsType.BOOLEAN)),
        INPUT_EA_3(Doc.of(OpenemsType.BOOLEAN)),

        /**
         * SetPoint Range. 0_10V to 0_120°C.
         * <li>Type: Integer</li>
         * <li>Unit: Degree Celsius</li>
         */
        SETPOINT_EA_1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Pumprotationvalue. 0-100% control signal == 0-10V
         * 1 = 0.5%
         */
        OUTPUT_SIGNAL_PM_1(Doc.of(OpenemsType.INTEGER)),
        /**
         * Shows the Actual Value in Correct %.
         */
        OUTPUT_SIGNAL_PM_1_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)),
        /**
         * GRID_ELECTRICITY_PUMP. Represented by Boolean (On Off)
         */
        GRID_VOLTAGE_BEHAVIOUR_PM_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Potential free Electrical Contact of the Pump. Represented by Boolean (On Off)
         */
        FLOATING_ELECTRICAL_CONTACT_PM_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * <p>Volume Flow Set Point of Pump Pm1.
         * 1 == 0.5%
         * </p>
         */
        VOLUME_FLOW_SET_POINT_PM_1(Doc.of(OpenemsType.INTEGER)),
        /**
         * Actual Percentage Value of Pump.
         */

        VOLUME_FLOW_SET_POINT_PM_1_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)),

        DISTURBANCE_INPUT_PM_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Temperature Sensors 1-4 of Pump.
         * <li>Unit: Degree Celsius</li>
         */
        TEMPERATURESENSOR_PM_1_1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_2(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_3(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_4(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),

        REWIND_TEMPERATURE_17A(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        REWIND_TEMPERATURE_17B(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Additional Temperature Sensor, Appearing in the Datasheet of Vitogate 300.
         */
        SENSOR_9(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * The Signal becomes True if the Heating cycle or the Waterusage of Device
         * sends a Temperature demand to the heat generation.
         */
        TRIBUTARY_PUMP(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * OperatingMode A1 M1.
         * 0: Off (Monitoried by Freezeprotection)
         * 1: Only Heating Water (Running by autotimer programms, Freezeprotection)
         * 2: Heating + Heating Water (Heating of room and above mentioned bulletpoints.
         */
        OPERATING_MODE_A1_M1(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        /**
         * Setpoint of Boiler Temperature 0-127°C.
         */
        BOILER_SET_POINT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Exhaustion Temperature of the Combustion Engine.
         */
        COMBUSTION_ENGINE_EXHAUST_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Combustion_Engine On Off represented by Boolean.
         * 0 = Off
         * 1 = On
         */
        COMBUSTION_ENGINE_ON_OFF(Doc.of(OpenemsType.BOOLEAN)),

        COMBUSTION_ENGINE_OPERATING_HOURS_TIER_1(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        COMBUSTION_ENGINE_OPERATING_HOURS_TIER_2(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        /**
         * Combustion engine Efficiency Actual Value.
         * 1 == 0.5%
         */
        COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Human readable % Value.
         */
        COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)),
        COMBUSTION_ENGINE_START_COUNTER(Doc.of(OpenemsType.INTEGER)),
        /**
         * Operation Modes.
         * 0: Combustion engine off
         * 1: Combustion engine Tier 1 on
         * 2: Combustion Engine Tier 2 on
         * 3: Combustion Engine Tier 1+2 on
         */
        COMBUSTION_ENGINE_OPERATING_MODE(Doc.of(OpenemsType.INTEGER)),

        COMBUSTION_ENGINE_BOILER_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Tells if the Sensor has an Error.
         * 0: Everythings OK
         * 1: Error
         */
        TEMPERATURE_SENSOR_1_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_2_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_3_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_4_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),

        /**
         * Expanded Diagnose Operating Hour Data.
         */
        OPERATING_HOURS_COMBUSTION_ENGINE_TIER_1_EXPANDED(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Operation modes of the heatingboiler.
         * 0: HVAC_AUTO
         * 1: HVAC_HEAT
         * 2: HVAC_MRNG_WRMUP
         * 3: HVAC_COOL
         * 4: HVAC_NIGHT_PURGE
         * 5: HVAC_PRE_COOL
         * 6: HVAC_OFF
         * 7: HVAC_TEST
         * 8: HVAC_EMERG_HEAT
         * 9: HVAC_FAN_ONLY
         * 110: HVAC_SLAVE_ACTIVE
         * 111: HVAC_LOW_FIRE
         * 112: HVAC_HIGH_FIRE
         * 255: HVAC_NUL
         */
        HEAT_BOILER_OPERATION_MODE(Doc.of(OpenemsType.INTEGER)),

        HEAT_BOILER_TEMPERATURE_SET_POINT_EFFECTIVE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Status represented by boolean.
         * 0 = Off
         * 1 = On
         */
        HEAT_BOILER_PERFORMANCE_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Performance set point.
         * 0 = Off
         * 1 = On
         * 255 = Auto
         */
        HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Values 1 == 0.5%.
         */
        HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * For Actual Percentage Value(Easier to read/write.
         */
        HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Heat Boiler Temperature Set point Value between 0-127.
         */
        HEAT_BOILER_TEMPERATURE_SET_POINT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Actual measured Temperature.
         */
        HEAT_BOILER_TEMPERATURE_ACTUAL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Modulation Value between 0-100%.
         * 1 == 0.5%
         */
        HEAT_BOILER_MODULATION_VALUE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Humandreadable and writable % Values.
         */
        HEAT_BOILER_MODULATION_VALUE_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)),
        /**
         * Operation mode of warm water.
         * 0: HVAC_AUTO
         * 1: HVAC_HEAT
         * 3: HVAC_COOL
         * 4: HVAC_NIGHT_PURGE
         * 5: HVAC_PRE_COOL
         * 6: HVAC_OFF
         * 255: HVAC_NUL
         */
        WARM_WATER_OPERATION_MODE(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Reads the effective warm water set point temperature in °C.
         */
        WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),

        /**
         * Sets the warm water Temperature between 0-90°C.
         */
        FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)
                .accessMode(AccessMode.READ_WRITE)),
        /**
         * Boiler Set Point Performance.
         * 1 == 0.5%
         */
        BOILER_SET_POINT_PERFORMANCE_EFFECTIVE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Human readable percentageValue.
         */
        BOILER_SET_POINT_PERFORMANCE_EFFECTIVE_PERCENT(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT)),
        /**
         * Boiler Set Point temperature 0-127°C.
         * Considers Boiler max temp. Boiler protection and freeze protection.
         * <li>Unit: Degree Celsius</li>
         */
        BOILER_SET_POINT_TEMPERATURE_EFFECTIVE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Max Reached Temperature of Boiler. Values between 0-500°C
         * <li>Unit: Degree Celsius</li>
         */
        BOILER_MAX_REACHED_EXHAUST_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),

        /**
         * Status of the Warm Water storage pump.
         * 0: Off
         * 1: On
         */
        WARM_WATER_STORAGE_CHARGE_PUMP(Doc.of(OpenemsType.BOOLEAN)),
        WARM_WATER_STORAGE_TEMPERATURE_5_A(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        WARM_WATER_STORAGE_TEMPERATURE_5_B(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Tells the prep status of the Warm water.
         * 0: Load inactive
         * 1: Engine Start
         * 2: Engine start Pump
         * 3: Load active
         * 4: Trail
         */
        WARM_WATER_PREPARATION(Doc.of(OpenemsType.INTEGER)),
        /**
         * Setpoint of Warmwater; Values between 10-95.
         * <li>Unit: Degree Celsius</li>
         * <p>
         * Attention: Max. allowed potable water temperature.
         * 10-60, with coding 56: 1 it's possible to set temp to 10-90.
         * </p>
         */
        WARM_WATER_TEMPERATURE_SET_POINT(Doc.of(OpenemsType.INTEGER)
                .accessMode(AccessMode.READ_WRITE)),
        WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Ciruculation pump state.
         * 0 = Off
         * 1 = On
         */
        WARM_WATER_CIRCULATION_PUMP(Doc.of(OpenemsType.BOOLEAN));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    default Channel<Boolean> getOutPutAm1_1() {
        return this.channel(ChannelId.OUTPUT_AM_1_1);
    }

    default Channel<Boolean> getOutPutAm1_2() {
        return this.channel(ChannelId.OUTPUT_AM_1_2);
    }

    default Channel<Boolean> getOutPut20() {
        return this.channel(ChannelId.OUTPUT_20);
    }

    default Channel<Boolean> getOutPut29() {
        return this.channel(ChannelId.OUTPUT_29);
    }

    default Channel<Integer> getAmbientTemperature() {
        return this.channel(ChannelId.AMBIENT_TEMPERATURE);
    }

    default Channel<Integer> getOutPutEA1_1() {
        return this.channel(ChannelId.OUTPUT_EA_1);
    }

    default Channel<Integer> getInputEA_1() {
        return this.channel(ChannelId.INPUT_EA_1);
    }

    default Channel<Integer> getInputEA_2() {
        return this.channel(ChannelId.INPUT_EA_2);
    }

    default Channel<Integer> getInputEA_3() {
        return this.channel(ChannelId.INPUT_EA_3);
    }

    default Channel<Integer> getSetPointEA_1() {
        return this.channel(ChannelId.SETPOINT_EA_1);
    }

    default Channel<Integer> getOutputSignalPm_1() {
        return this.channel(ChannelId.OUTPUT_SIGNAL_PM_1);
    }

    default Channel<Boolean> getGridVoltageBehaviourPm_1() {
        return this.channel(ChannelId.GRID_VOLTAGE_BEHAVIOUR_PM_1);
    }

    default Channel<Boolean> getFloatingElectricalContactPm_1() {
        return this.channel(ChannelId.FLOATING_ELECTRICAL_CONTACT_PM_1);
    }

    default Channel<Integer> getVolumeFlowSetpointPm_1() {
        return this.channel(ChannelId.VOLUME_FLOW_SET_POINT_PM_1);
    }

    default Channel<Boolean> getDisturbanceInputPm_1() {
        return this.channel(ChannelId.DISTURBANCE_INPUT_PM_1);
    }

    default Channel<Integer> getTemperatureSensorPm_1_1() {
        return this.channel(ChannelId.TEMPERATURESENSOR_PM_1_1);
    }

    default Channel<Integer> getTemperatureSensorPm_1_2() {
        return this.channel(ChannelId.TEMPERATURESENSOR_PM_1_2);
    }

    default Channel<Integer> getTemperatureSensorPm_1_3() {
        return this.channel(ChannelId.TEMPERATURESENSOR_PM_1_3);
    }

    default Channel<Integer> getTemperatureSensorPm_1_4() {
        return this.channel(ChannelId.TEMPERATURESENSOR_PM_1_4);
    }

    default Channel<Integer> getRewindTemperature17A() {
        return this.channel(ChannelId.REWIND_TEMPERATURE_17A);
    }

    default Channel<Integer> getRewindTemperature17B() {
        return this.channel(ChannelId.REWIND_TEMPERATURE_17B);
    }

    default Channel<Integer> getSensor9() {
        return this.channel(ChannelId.SENSOR_9);
    }

    default Channel<Boolean> getTributaryPump() {
        return this.channel(ChannelId.TRIBUTARY_PUMP);
    }

    default WriteChannel<Integer> getOperatingModeA1M1() {
        return this.channel(ChannelId.OPERATING_MODE_A1_M1);
    }

    default WriteChannel<Integer> getBoilerSetPointTemperature() {
        return this.channel(ChannelId.BOILER_SET_POINT_TEMPERATURE);
    }

    default Channel<Integer> getCombustionEngineExhaustTemperature() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_EXHAUST_TEMPERATURE);
    }

    default Channel<Boolean> getCombustionEngineOnOff() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_ON_OFF);
    }

    default Channel<Integer> getCombustionEngineOperatingHoursTier1() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_1);
    }

    default Channel<Integer> getCombustionEngineOperatingHoursTier2() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_OPERATING_HOURS_TIER_2);
    }

    default Channel<Integer> getCombustionEngineEfficiencyActualValue() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE);
    }

    default Channel<Integer> getCombustionEngineStartCounter() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_START_COUNTER);
    }

    default Channel<Integer> getCombustionEngineOperatingMode() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_OPERATING_MODE);
    }

    default Channel<Integer> getCombustionEngineBoilerTemperature() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_BOILER_TEMPERATURE);
    }

    default Channel<Boolean> getTemperatureSensor_1_Pm1_Status() {
        return this.channel(ChannelId.TEMPERATURE_SENSOR_1_PM_1_STATUS);
    }

    default Channel<Boolean> getTemperatureSensor_2_Pm1_Status() {
        return this.channel(ChannelId.TEMPERATURE_SENSOR_2_PM_1_STATUS);
    }

    default Channel<Boolean> getTemperatureSensor_3_Pm1_Status() {
        return this.channel(ChannelId.TEMPERATURE_SENSOR_3_PM_1_STATUS);
    }

    default Channel<Boolean> getTemperatureSensor_4_Pm1_Status() {
        return this.channel(ChannelId.TEMPERATURE_SENSOR_4_PM_1_STATUS);
    }

    default Channel<Boolean> getOperatingHoursEngineTier_1_Expanded() {
        return this.channel(ChannelId.OPERATING_HOURS_COMBUSTION_ENGINE_TIER_1_EXPANDED);
    }

    default WriteChannel<Integer> getHeatBoilerOperationMode() {
        return this.channel(ChannelId.HEAT_BOILER_OPERATION_MODE);
    }

    default Channel<Integer> getHeatBoilerTemperatureSetPointEffective() {
        return this.channel(ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT_EFFECTIVE);
    }

    default Channel<Boolean> getHeatBoilerPerformanceStatus() {
        return this.channel(ChannelId.HEAT_BOILER_PERFORMANCE_STATUS);
    }

    default WriteChannel<Integer> getHeatBoilerPerformanceSetPointStatus() {
        return this.channel(ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS);
    }

    default WriteChannel<Integer> getHeatBoilerPerformanceSetPointValue() {
        return this.channel(ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE);
    }

    default WriteChannel<Integer> getHeatBoilerTemperatureSetPoint() {
        return this.channel(ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT);
    }

    default Channel<Integer> getHeatBoilerTemperatureActual() {
        return this.channel(ChannelId.HEAT_BOILER_TEMPERATURE_ACTUAL);
    }

    default Channel<Integer> getHeatBoilerModulationValue() {
        return this.channel(ChannelId.HEAT_BOILER_MODULATION_VALUE);
    }

    default WriteChannel<Integer> getWarmWaterOperationMode() {
        return this.channel(ChannelId.WARM_WATER_OPERATION_MODE);
    }

    default Channel<Integer> getWarmWaterEffectiveSetPointTemperature() {
        return this.channel(ChannelId.WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE);
    }

    default WriteChannel<Integer> getWarmWaterSetPointTemperature() {
        return this.channel(ChannelId.FUNCTIONING_WARM_WATER_SET_POINT_TEMPERATURE);
    }

    default Channel<Integer> getBoilerSetPointPerformanceEffetive() {
        return this.channel(ChannelId.BOILER_SET_POINT_PERFORMANCE_EFFECTIVE);
    }

    default Channel<Integer> getBoilerSetPointTemperatureEffective() {
        return this.channel(ChannelId.BOILER_SET_POINT_TEMPERATURE_EFFECTIVE);
    }

    default Channel<Integer> getBoilerMaxReachedExhaustTemperature() {
        return this.channel(ChannelId.BOILER_MAX_REACHED_EXHAUST_TEMPERATURE);
    }

    default Channel<Boolean> getWarmWaterStorageChargePump() {
        return this.channel(ChannelId.WARM_WATER_STORAGE_CHARGE_PUMP);
    }

    default Channel<Integer> getWarmWaterStorageTemperature_5_A() {
        return this.channel(ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_A);
    }

    default Channel<Integer> getWarmWaterStorageTemperature_5_B() {
        return this.channel(ChannelId.WARM_WATER_STORAGE_TEMPERATURE_5_B);
    }

    default Channel<Integer> getWarmWaterPreparation() {
        return this.channel(ChannelId.WARM_WATER_PREPARATION);
    }

    default WriteChannel<Integer> getWarmWaterTemperatureSetPoint() {
        return this.channel(ChannelId.WARM_WATER_TEMPERATURE_SET_POINT);
    }

    default Channel<Integer> getWarmWaterTemperatureSetPointEffective() {
        return this.channel(ChannelId.WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE);
    }

    default Channel<Integer> getWarmWaterCirculationPump() {
        return this.channel(ChannelId.WARM_WATER_CIRCULATION_PUMP);
    }

    /*
     * Following Channels get the Value of member Channels and returns them as an actual Percentage Value
     * */
    default Channel<Float> getOutPutSignalPm1_Percent() {
        return this.channel(ChannelId.OUTPUT_SIGNAL_PM_1_PERCENT);
    }

    default Channel<Float> getVolumeFlowSetPointPm1Percent() {
        return this.channel(ChannelId.VOLUME_FLOW_SET_POINT_PM_1_PERCENT);
    }

    default Channel<Float> getCombustionEngineEfficiencyActualValuePercent() {
        return this.channel(ChannelId.COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE_PERCENT);
    }

    default WriteChannel<Float> getHeatBoilerPerformanceSetPointValuePercent() {
        return this.channel(ChannelId.HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE_PERCENT);
    }

    default Channel<Float> getHeatBoilerModulationValuePercent() {
        return this.channel(ChannelId.HEAT_BOILER_MODULATION_VALUE_PERCENT);
    }

    default Channel<Float> getBoilerSetPointPerformanceEffectivePercent() {
        return this.channel(ChannelId.BOILER_SET_POINT_PERFORMANCE_EFFECTIVE_PERCENT);
    }

}

