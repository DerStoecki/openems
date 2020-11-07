package io.openems.edge.relays.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface ActuatorRelaysChannel extends OpenemsComponent {
    /**
     * Is active or not.
     *
     * <ul>
     * <li>Interface: ActuatorRelays
     * <li>Type: boolean
     * <li>Unit: ON_OFF
     * </ul>
     */
    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        ON_OFF(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        //
        IS_CLOSER(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY));
        private final Doc doc;


        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    /**
     * Gets the On or Off Value as Boolean.
     *
     * @return the Channel
     */
    default BooleanWriteChannel getRelaysChannel() {
        return this.channel(ChannelId.ON_OFF);
    }

    default Channel<Boolean> isCloser() {
        return this.channel(ChannelId.IS_CLOSER);
    }


}
