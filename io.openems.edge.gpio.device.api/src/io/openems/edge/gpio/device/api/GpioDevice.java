package io.openems.edge.gpio.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface GpioDevice extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * Reads Gpio if an ErrorFlag was set or not. (Same goes for OnOff.
         *
         * <ul>
         * <li>Interface: GpioDevice
         * <li>Type: Boolean
         * </ul>
         */
        READ_ERROR(Doc.of(OpenemsType.BOOLEAN)),

        WRITE_ERROR(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE));

        private final Doc doc;

        ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    default Channel<Boolean> getReadError() {
        return this.channel(ChannelId.READ_ERROR);
    }

    default WriteChannel<Boolean> getWriteError() {
        return this.channel(ChannelId.WRITE_ERROR);
    }


}
