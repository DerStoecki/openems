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
         * OnOFF.
         *
         * <ul>
         * <li>Interface: PassingChannel
         * <li>Type: Boolean
         * <li>
         * </ul>
         */
        ON_OFF(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * IsCloser.
         *
         * <ul>
         * <li>Type: Boolean
         * </ul>
         */

        IS_CLOSER(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY)),

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

        BUSY(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE));

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
     * Gets the Temperature in [degree celsius].
     *
     * @return the Channel
     */
    default Channel<Boolean> getOnOff() {
        return this.channel(ChannelId.ON_OFF);
    }

    default Channel<Boolean> getIsCloser() {
        return this.channel(ChannelId.IS_CLOSER);
    }

    default Channel<Float> getPowerLevel() {
        return this.channel(ChannelId.POWER_LEVEL);
    }

    default Channel<Float> getLastPowerLevel() {
        return this.channel(ChannelId.LAST_POWER_LEVEL);
    }

    default Channel<Boolean> getIsBusy() {
        return this.channel(ChannelId.BUSY);
    }
}
