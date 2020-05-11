package io.openems.edge.rest.communicator.api;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface RestCommunicator extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * IsMaster Enabled via Config. If the Communication device is Master Device.
         *
         * <ul>
         * <li>Interface: RestCommunicator
         * <li>Type: Boolean
         * </ul>
         */
        IS_MASTER(Doc.of(OpenemsType.BOOLEAN));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    /**
     * Gets the Channel and returns if the Communicator is a Master or not.
     *
     * @return the Channel
     */
    default Channel<Boolean> isMaster() {
        return this.channel(ChannelId.IS_MASTER);
    }


}
