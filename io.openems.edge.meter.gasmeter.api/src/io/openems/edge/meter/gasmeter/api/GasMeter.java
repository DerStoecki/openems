package io.openems.edge.meter.gasmeter.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface GasMeter extends OpenemsComponent {


    enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT)),
        PERCOLATION(Doc.of(OpenemsType.INTEGER).unit(Unit.CUBICMETER_PER_SECOND)),
        TOTAL_CONSUMED_ENERGY(Doc.of(OpenemsType.INTEGER).unit(Unit.CUBIC_METER)),
        FLOW_TEMP(Doc.of(OpenemsType.FLOAT).unit(Unit.DEGREE_CELSIUS)),
        RETURN_TEMP(Doc.of(OpenemsType.FLOAT).unit(Unit.DEGREE_CELSIUS));

        // AVERAGE_CONSUMPTION_PER_HOUR(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }


    }

    //    default Channel<Integer> getAverageHourConsumption() {
    //        return this.channel(ChannelId.AVERAGE_CONSUMPTION_PER_HOUR);
    //    }

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
}