package io.openems.edge.heatpump.device.api;


import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface HeatPump extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        PRESSURE(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE).unit(Unit.BAR));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }


    default WriteChannel<Double> getPressure() {
        return this.channel(ChannelId.PRESSURE);
    }

}



