package io.openems.edge.gpio.device.api;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface GpioDevice extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * Temperature.
         *
         * <ul>
         * <li>Interface: Thermometer
         * <li>Type: Integer
         * <li>Unit: degree celsius
         * </ul>
         */
        ON_OFF(Doc.of(OpenemsType.BOOLEAN));

        private final Doc doc;

        ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    default Channel<Boolean> getOnOff(){
        return this.channel(ChannelId.ON_OFF);
    }


}
