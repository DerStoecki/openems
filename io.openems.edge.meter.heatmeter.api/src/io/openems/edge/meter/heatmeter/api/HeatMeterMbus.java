package io.openems.edge.meter.heatmeter.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface HeatMeterMbus extends OpenemsComponent {


    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        /**
         * Power.
         *
         * <ul>
         * <li>Interface: HeatMeterMbus
         * <li>Type: Integer
         * <li>Unit: Kilowatt
         * </ul>
         */
        POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT)),
        /**
         * Percolation.
         *
         * <ul>
         * <li>Interface: HeatMeterMbus
         * <li>Type: Integer
         * <li>Unit: m³/s CubicmeterPerSecond
         * </ul>
         */
        PERCOLATION(Doc.of(OpenemsType.DOUBLE).unit(Unit.CUBICMETER_PER_HOUR)),
        /**
         * Total Consumed Energy.
         *
         * <ul>
         * <li>Interface: HeatMeterMbus
         * <li>Type: Integer
         * <li>Unit: WattHours
         * </ul>
         */
        TOTAL_CONSUMED_ENERGY(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT_HOURS)),
        /**
         * FlowTemp.
         *
         * <ul>
         * <li>Interface: HeatMeterMbus
         * <li>Type: Float
         * <li>Unit: DegreeCelsius
         * </ul>
         */
        FLOW_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),
        /**
         * Return Temp.
         *
         * <ul>
         * <li>Interface: HeatMeterMbus
         * <li>Type: Float
         * <li>Unit: DegreeCelsius
         * </ul>
         */
        RETURN_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),

        /**
         * Timestamp of Measurement
         */
        TIMESTAMP(Doc.of(OpenemsType.LONG).unit(Unit.SECONDS)),

        AVERAGE_CONSUMPTION_PER_HOUR(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }


    }

    default Channel<Integer> getAverageHourConsumption() {
        return this.channel(ChannelId.AVERAGE_CONSUMPTION_PER_HOUR);
    }

    default Channel<Integer> getPower() {
        return this.channel(ChannelId.POWER);
    }

    default Channel<Integer> getPercolation() {
        return this.channel(ChannelId.PERCOLATION);
    }

    default Channel<Integer> getTotalConsumedEnergy() {
        return this.channel(ChannelId.TOTAL_CONSUMED_ENERGY);
    }

    default Channel<Float> getFlowTemp() {
        return this.channel(ChannelId.FLOW_TEMP);
    }

    default Channel<Float> getReturnTemp() {
        return this.channel(ChannelId.RETURN_TEMP);
    }

    default Channel<Float> getTimestamp() {
        return this.channel(ChannelId.TIMESTAMP);
    }
}

