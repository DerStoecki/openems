package io.openems.edge.rest.remote.device.general.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface RestRemoteChannel extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        //  INTEGER_READ(Doc.of(OpenemsType.INTEGER)),
        //  INTEGER_WRITE(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        //
        //  STRING_READ(Doc.of(OpenemsType.STRING)),
        //  STRING_WRITE(Doc.of(OpenemsType.STRING).accessMode(AccessMode.READ_WRITE)),
        //
        //  BOOLEAN_READ(Doc.of(OpenemsType.BOOLEAN)),
        //  BOOLEAN_WRITE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        //
        //  FLOAT_READ(Doc.of(OpenemsType.FLOAT)),
        //  FLOAT_WRITE(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_WRITE)),

        VALUE_READ(Doc.of(OpenemsType.STRING)),
        VALUE_WRITE(Doc.of(OpenemsType.STRING).accessMode(AccessMode.READ_WRITE)),
        WHAT_TYPE_SET(Doc.of(OpenemsType.STRING)),
        ALLOW_REQUEST(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        IS_INVERSE(Doc.of(OpenemsType.BOOLEAN));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    //    default Channel<Integer> getIntegerRead() {
    //        return this.channel(ChannelId.INTEGER_READ);
    //    }
    //
    //    default WriteChannel<Integer> getIntegerWrite() {
    //        return this.channel(ChannelId.INTEGER_WRITE);
    //    }
    //
    //    default Channel<String> getStringRead() {
    //        return this.channel(ChannelId.STRING_READ);
    //    }
    //
    //    default WriteChannel<String> getStringWrite() {
    //        return this.channel(ChannelId.STRING_WRITE);
    //    }
    //
    //    default Channel<Boolean> getBooleanRead() {
    //        return this.channel(ChannelId.BOOLEAN_READ);
    //    }
    //
    //    default WriteChannel<Boolean> getBooleanWrite() {
    //        return this.channel(ChannelId.BOOLEAN_WRITE);
    //
    //    }
    //
    //    default Channel<Float> getFloatRead() {
    //        return this.channel(ChannelId.FLOAT_READ);
    //    }
    //
    //    default WriteChannel<Float> getFloatWrite() {
    //        return this.channel(ChannelId.FLOAT_WRITE);
    //    }
    default Channel<String> getReadValue() {
        return this.channel(ChannelId.VALUE_READ);
    }

    default WriteChannel<String> getWriteValue() {
        return this.channel(ChannelId.VALUE_WRITE);
    }

    default Channel<String> getTypeSet() {
        return this.channel(ChannelId.WHAT_TYPE_SET);
    }
}