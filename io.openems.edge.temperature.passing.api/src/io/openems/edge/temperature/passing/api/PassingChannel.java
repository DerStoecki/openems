package io.openems.edge.temperature.passing.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface PassingChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {



        /**
         * PowerLevel.
         *
         * <ul>
         * <li>Interface: PassingChannel
         * <li>Type: Float
         * <li> Unit: Percentage
         * </ul>
         */

        POWER_LEVEL(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT)),

        /**
         * LastPowerLevel.
         *
         * <ul>
         * <li>
         * <li>Type: Float
         * <li>Unit: Percentage
         * </ul>
         */

        LAST_POWER_LEVEL(Doc.of(OpenemsType.FLOAT).accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT)),
        /**
         * Busy.
         *
         * <ul>
         * <li>Type: Boolean
         * </ul>
         */

        BUSY(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * How Long does the Device need to do something(e.g. Valve Opening/Closing time)
         *
         * <ul>
         * <li>Type: Boolean
         * </ul>
         */

        TIME(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY));

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
     * .
     * <ul>
     * <li> Tells how much percent of the Device is used aka how much the valve is opened or
     *      how much % of the Pump is on a high / low.
     * <li> Unit: Float
     * </ul>
     *
     * @return the Channel
     */

    default Channel<Float> getPowerLevel() {
        return this.channel(ChannelId.POWER_LEVEL);
    }

    /**
     * <ul>
     * Same as above, but LastPowerLevel; For calculation purposes and for checking.
     *
     * <li> Type: Float
     * </ul>
     *
     * @return the Channel
     */

    default Channel<Float> getLastPowerLevel() {
        return this.channel(ChannelId.LAST_POWER_LEVEL);
    }

    /**
     * Tells if the PassingDevice is busy or not.
     * <li> Type: Boolean
     *
     * @return the Channel
     */

    default Channel<Boolean> getIsBusy() {
        return this.channel(ChannelId.BUSY);
    }

    /**
     * Tells how much time is needed for e.g. Valve to Open or Close 100%.
     * <li> Type: Boolean
     *
     * @return the Channel
     */

    default Channel<Integer> getTimeNeeded() {
        return this.channel(ChannelId.TIME);
    }
}
