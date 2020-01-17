package io.openems.edge.heatmeter.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface HeatMeterMbus extends OpenemsComponent {


    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        AVERAGE_CONSUMPTION(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }


    }

    default Channel<Integer> getAverageConsumption() {
        return this.channel(ChannelId.AVERAGE_CONSUMPTION);
    }

}

