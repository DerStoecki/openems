package io.openems.edge.chp.device.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface ChpInformationChannel extends OpenemsComponent {
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
        RESERVE(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT_HOURS)),
        ERROR_CHANNEL(Doc.of(OpenemsType.STRING));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    default Channel<Integer> getErrorOne() {
        return this.channel(ChannelId.ERROR_BITS_1);
    }

    default Channel<Integer> getErrorTwo() {
        return this.channel(ChannelId.ERROR_BITS_2);
    }

    default Channel<Integer> getErrorThree() {
        return this.channel(ChannelId.ERROR_BITS_3);
    }

    default Channel<Integer> getErrorFour() {
        return this.channel(ChannelId.ERROR_BITS_4);
    }

    default Channel<Integer> getErrorFive() {
        return this.channel(ChannelId.ERROR_BITS_5);
    }

    default Channel<Integer> getErrorSix() {
        return this.channel(ChannelId.ERROR_BITS_6);
    }

    default Channel<Integer> getErrorSeven() {
        return this.channel(ChannelId.ERROR_BITS_7);
    }

    default Channel<Integer> getErrorEight() {
        return this.channel(ChannelId.ERROR_BITS_8);
    }

    default Channel<String> getErrorChannel() {
        return this.channel(ChannelId.ERROR_CHANNEL);
    }
}