package io.openems.edge.rest.remote.device.general.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface RestRemoteChannel extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        VALUE_READ(Doc.of(OpenemsType.STRING)),
        VALUE_WRITE(Doc.of(OpenemsType.STRING).accessMode(AccessMode.READ_WRITE)),
        WHAT_TYPE_SET(Doc.of(OpenemsType.STRING)),
        ALLOW_REQUEST(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        IS_INVERSE(Doc.of(OpenemsType.BOOLEAN));


        private final Doc doc;

        ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    default Channel<String> getReadValue() {
        return this.channel(ChannelId.VALUE_READ);
    }

    default WriteChannel<String> getWriteValue() {
        return this.channel(ChannelId.VALUE_WRITE);
    }

    default Channel<String> getTypeSet() {
        return this.channel(ChannelId.WHAT_TYPE_SET);
    }

    default WriteChannel<Boolean> getAllowRequest() {
        return this.channel(ChannelId.ALLOW_REQUEST);
    }

    default Channel<Boolean> getIsInverse() {
        return this.channel(ChannelId.IS_INVERSE);
    }
}