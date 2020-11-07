package io.openems.edge.powerplant.analog.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.*;
import io.openems.edge.common.component.OpenemsComponent;

public interface PowerPlant extends OpenemsComponent {


    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        /**
         * PowerLevel.
         *
         * <ul>
         * <li>Interface: PassingChannel
         * <li>Type: Double
         * <li> Unit: Percentage
         * </ul>
         */

        POWER_LEVEL_PERCENT(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                }
        )),

        /**
         * PowerLevelKW.
         *
         * <ul>
         * <li>
         * <li>Type: Double
         * <li>Unit: Kw
         * </ul>
         */

        POWER_LEVEL_KW(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.KILOWATT).onInit(
                ch -> {
                    ((IntegerWriteChannel) ch).onSetNextWrite(ch::setNextValue);
                })),

        MAXIMUM_KW(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_ONLY).unit(Unit.KILOWATT)),

        ERROR_OCCURED(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY));

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

    default WriteChannel<Integer> getPowerLevelPercent() {
        return this.channel(ChannelId.POWER_LEVEL_PERCENT);
    }

    default WriteChannel<Integer> getPowerLevelKiloWatt() {
        return this.channel(ChannelId.POWER_LEVEL_KW);
    }

    default Channel<Boolean> getErrorOccured() {
        return this.channel(ChannelId.ERROR_OCCURED);
    }

    default Channel<Integer> getMaximumKw() {
        return this.channel(ChannelId.MAXIMUM_KW);
    }
}