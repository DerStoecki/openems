package io.openems.edge.lucidcontrol.device.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface LucidControlDevice extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        PRESSURE(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR));
        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }

    }

    default Channel<Double> getPressure(){
        return this.channel(ChannelId.PRESSURE);
    }

}
