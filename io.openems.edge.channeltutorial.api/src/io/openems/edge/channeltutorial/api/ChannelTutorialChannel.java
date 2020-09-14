package io.openems.edge.channeltutorial.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.*;
import io.openems.edge.common.component.OpenemsComponent;

public interface ChannelTutorialChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        /**
         * Example write channel 1 to demonstrate how to use a write channel.
         * <li>
         * <li>Type: Integer
         * <li>
         * </ul>
         */

        WRITE1(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)),

        /**
         * Example write channel 2 to demonstrate how to use a write channel.
         * <li>
         * <li>Type: Integer
         * <li>
         * </ul>
         */

        WRITE2(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)),

        /**
         * Example write channel 3 to demonstrate how to use a write channel. This channel automatically copies anything
         * that is written into [write] into [next].
         * <li>
         * <li>Type: Integer
         * <li>
         * </ul>
         */

        WRITE3(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
                .onInit(channel -> { //
                    // on each Write to the channel -> set the value
                    ((IntegerWriteChannel) channel).onSetNextWrite(value -> {
                        channel.setNextValue(value);
                    });
                })),

        /**
         * Example write channel 4 to demonstrate how to use a write channel. This channel automatically copies anything
         * that is written into [write] into [next].
         * <li>
         * <li>Type: Integer
         * <li>
         * </ul>
         */

        WRITE4(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
                .onInit(channel -> { //
                    // on each Write to the channel -> set the value
                    ((IntegerWriteChannel) channel).onSetNextWrite(value -> {
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
     * Example write channel 1 to demonstrate how to use a write channel. Type is integer.
     *
     * @return the Channel
     */

    default WriteChannel<Integer> exampleWriteChannel1() { return this.channel(ChannelId.WRITE1); }

    /**
     * Example write channel 2 to demonstrate how to use a write channel. Type is integer.
     *
     * @return the Channel
     */

    default WriteChannel<Integer> exampleWriteChannel2() {
        return this.channel(ChannelId.WRITE2);
    }

    /**
     * Example write channel 3 to demonstrate how to use a write channel. Type is integer.
     * This channel automatically copies anything that is written into [write] into [next].
     *
     * @return the Channel
     */

    default WriteChannel<Integer> exampleWriteChannel3() {
        return this.channel(ChannelId.WRITE3);
    }

    /**
     * Example write channel 3 to demonstrate how to use a write channel. Type is integer.
     * This channel automatically copies anything that is written into [write] into [next].
     *
     * @return the Channel
     */

    default WriteChannel<Integer> exampleWriteChannel4() {
        return this.channel(ChannelId.WRITE4);
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
