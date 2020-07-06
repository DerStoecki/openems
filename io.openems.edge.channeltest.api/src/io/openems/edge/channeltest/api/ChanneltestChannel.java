package io.openems.edge.channeltest.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.*;
import io.openems.edge.common.component.OpenemsComponent;

public interface ChanneltestChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /**
         * Write integer channel.
         * <li>
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        WRITE_INTEGER(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),

        /**
         * Write boolean channel.
         * <li>
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        WRITE_BOOLEAN(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)
                .onInit(channel -> { //
            // on each Write to the channel -> set the value
            ((BooleanWriteChannel) channel).onSetNextWrite(value -> {
                channel.setNextValue(value);
            });
        })),

        /**
         * Is Error.
         * <ul>
         * <li> If an Error occurred within this Controller
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        NO_ERROR(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }


    /**
     * Write integer channel.
     *
     * @return the Channel
     */

    default WriteChannel<Integer> writeInteger() {
        return this.channel(ChannelId.WRITE_INTEGER);
    }

    /**
     * Write boolean channel.
     *
     * @return the Channel
     */

    default WriteChannel<Boolean> writeBoolean() {
        return this.channel(ChannelId.WRITE_BOOLEAN);
    }


    /**
     * Has an Error occurred or is everything's fine.
     *
     * @return the Channel
     */

    default Channel<Boolean> noError() {
        return this.channel(ChannelId.NO_ERROR);
    }

}
