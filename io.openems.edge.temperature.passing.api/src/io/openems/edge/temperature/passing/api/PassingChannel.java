package io.openems.edge.temperature.passing.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface PassingChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        // TODO: Why is this a write channel? It does not seem it needs to be. Can it be changed to a read channel if that is sufficient?
        /**
         * PowerLevel.
         *
         * <ul>
         * <li>Interface: PassingChannel
         * <li>Type: Double
         * <li> Unit: Percentage
         * </ul>
         */

        POWER_LEVEL(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT)),

        /**
         * LastPowerLevel.
         *
         * <ul>
         * <li>
         * <li>Type: Double
         * <li>Unit: Percentage
         * </ul>
         */

        LAST_POWER_LEVEL(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_ONLY).unit(Unit.PERCENT)),
        /**
         * Tells if the Device is Busy or not.
         *
         * <ul>
         * <li>Type: Boolean
         * </ul>
         */

        BUSY(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * Set Power Level of e.g. Valve.
         * Handled before Controllers.
         * <ul>
         *     <li> Type: Integer
         * </ul>
         */
        SET_POWER_LEVEL(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(channel ->
                ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue))),


        /**
         * How Long does the Device need to do something(e.g. Valve Opening/Closing time)
         *
         * <ul>
         * <li>Type: Double
         * </ul>
         */


        TIME(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_ONLY).unit(Unit.SECONDS));

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
     * <li> Unit: Double
     * </ul>
     *
     * @return the Channel
     */

    default WriteChannel<Double> getPowerLevel() {
        return this.channel(ChannelId.POWER_LEVEL);
    }

    /**
     * <ul>
     * Same as above, but LastPowerLevel; For calculation purposes and for checking.
     *
     * <li> Type: Double
     * </ul>
     *
     * @return the Channel
     */

    default Channel<Double> getLastPowerLevel() {
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


    default WriteChannel<Integer> setPowerLevelPercent() {
        return this.channel(ChannelId.SET_POWER_LEVEL);
    }


    /**
     * Tells how much time is needed for e.g. Valve to Open or Close 100%.
     * <li> Type: Double
     *
     * @return the Channel
     */


    default Channel<Double> getTimeNeeded() {
        return this.channel(ChannelId.TIME);
    }
}
