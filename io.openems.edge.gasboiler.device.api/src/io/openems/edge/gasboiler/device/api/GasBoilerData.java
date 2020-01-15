package io.openems.edge.gasboiler.device.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface GasBoilerData extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * Output 1 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_AM1_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Output 2 represented by Boolean.
         * <li>Type: Boolean</li>
         * 0 = Off
         * 1 = On
         */
        OUTPUT_AM1_2(Doc.of(OpenemsType.BOOLEAN)),
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
         * */
        SETPOINT_EA_1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Pumprotationvalue. 0-100% control signal == 0-10V
         * */
        OUTPUT_SIGNAL_PM_1(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        /**
         * GRID_ELECTRICITY_PUMP. Represented by Boolean (On Off)
         * */
        GRID_VOLTAGE_BEHAVIOUR_PM_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Potential free Electrical Contact of the Pump. Represented by Boolean (On Off)
         * */
        FLOATING_ELECTRICAL_CONTACT_PM_1(Doc.of(OpenemsType.BOOLEAN)),

        VOLUME_FLOW_SETPOINT_PM_1(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),

        DISTURBANCE_INPUT_PM_1(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Temperature Sensors 1-4 of Pump.
         * <li>Unit: Degree Celsius</li>
         * */
        TEMPERATURESENSOR_PM_1_1(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_2(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_3(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        TEMPERATURESENSOR_PM_1_4(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),

        REWIND_TEMPERATURE_17A(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        REWIND_TEMPERATURE_17B(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Additional Temperature Sensor, Appearing in the Datasheet of Vitogate 300.
         * */
        SENSOR_9(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * The Signal becomes True if the Heating cycle or the Waterusage of Device
         * sends a Temperature demand to the heat generation.
         * */
        TRIBUTARY_PUMP(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * OperatingMode A1 M1.
         * 0: Off (Monitoried by Freezeprotection)
         * 1: Only Heating Water (Running by autotimer programms, Freezeprotection)
         * 2: Heating + Heating Water (Heating of room and above mentioned bulletpoints.
         * */
        OPERATING_MODE_A1_M1(Doc.of(OpenemsType.INTEGER)),
        /**
         * Setpoint of Boiler Temperature 0-127°C.
         *
         * */
        BOILER_SETPOINT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Exhaustion Temperature of the Combustion Engine.
         * */
        EXHAUST_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Combustion_Engine On Off represented by Boolean.
         * 0 = Off
         * 1 = On
         * */
        COMBUSTION_ENGINE_ON_OFF(Doc.of(OpenemsType.BOOLEAN)),

        COMBUSTION_ENGINE_OPERATING_HOURS_TIER_1(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
        COMBUSTION_ENGINE_OPERATING_HOURS_TIER_2(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),

        COMBUSTION_ENGINE_EFFICIENCY_ACTUAL_VALUE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        COMBUSTION_ENGINE_START_COUNTER(Doc.of(OpenemsType.INTEGER)),
        /**
         * Operation Modes.
         * 0: Combustion engine off
         * 1: Combustion engine Tier 1 on
         * 2: Combustion Engine Tier 2 on
         * 3: Combustion Engine Tier 1+2 on
         * */
        COMBUSTION_ENGINE_OPERATING_MODE(Doc.of(OpenemsType.INTEGER)),

        GAS_BOILER_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Tells if the Sensor has an Error.
         * 0: Everythings OK
         * 1: Error
         * */
        TEMPERATURE_SENSOR_1_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_2_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_3_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        TEMPERATURE_SENSOR_4_PM_1_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        /**
         * Expanded Diagnose Operating Hour Data.
         * */
        OPERATING_HOURS_COMBUSTION_ENGINE_TIER_1(Doc.of(OpenemsType.INTEGER).unit(Unit.HOUR)),
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
         *
         * */
        HEAT_BOILER_OPERATION_MODE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Status represented by boolean.
         * 0 = Off
         * 1 = On
         * */
        HEAT_BOILER_PERFORMANCE_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        HEAT_BOILER_PERFORMANCE_SET_POINT_STATUS(Doc.of(OpenemsType.BOOLEAN)),

        HEAT_BOILER_PERFORMANCE_SET_POINT_VALUE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        /**
         * Heat Boiler Temperature Set point Value between 0-127.
         * */
        HEAT_BOILER_TEMPERATURE_SET_POINT(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Actual measured Temperature.
         * */
        HEAT_BOILER_TEMPERATURE_ACTUAL(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Modulation Value between 0-100%.
         * */
        HEAT_BOILER_MODULATION_VALUE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        /**
         * Operation mode of warm water.
         * 0: HVAC_AUTO
         * 1: HVAC_HEAT
         * 3: HVAC_COOL
         * 4: HVAC_NIGHT_PURGE
         * 5:HVAC_PRE_COOL
         * 6:HVAC_OFF
         * 255: HVAC_NUL
         * */
        WARM_WATER_OPERATION_MODE(Doc.of(OpenemsType.INTEGER)),

        WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Boiler Set Point Performance.
         * <li>Unit: % </li>
         * */
        BOILER_SET_POINT_PERFORMANCE_EFFECTIVE(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        /**
         * Boiler Set Point temperature 0-127°C.
         * Considers Boiler max temp. Boiler protection and freeze protection.
         * <li>Unit: Degree Celsius</li>
         *
         * */
        BOILER_SET_POINT_TEMPERATURE_EFFECTIVE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        /**
         * Max Reached Temperature of Boiler. Values between 0-500°C
         * <li>Unit: Degree Celsius</li>
         *
         * */
        BOILER_MAX_REACHED_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),

        /**
         * Status of the Warm Water storage pump.
         * 0: Off
         * 1: On
         * */
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
         * */
        WARM_WATER_PREPARATION(Doc.of(OpenemsType.INTEGER)),
        /**
         * Setpoint of Warmwater; Values between 10-95.
         * <li>Unit: Degree Celsius</li>
         * <p>
         *     Attention: Max. allowed potable water temperature.
         *     10-60, with coding 56: 1 it's possible to set temp to 10-90.
         * </p>
         * */
        WARM_WATER_TEMPERATURE_SET_POINT(Doc.of(OpenemsType.INTEGER)),
        WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE(Doc.of(OpenemsType.INTEGER)),
        /**
         * Ciruculation pump state.
         * 0 = Off
         * 1 = On
         * */
        WARM_WATER_CIRCULATION_PUMP(Doc.of(OpenemsType.BOOLEAN));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

}
