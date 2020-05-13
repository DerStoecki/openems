package io.openems.edge.controller.signalhotwater.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface SignalHotWaterChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /**
         * Low temperature in water tank.
         * <li>
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        TEMP_LOW(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * Controller input
         * <li>
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        HOT_WATER_REMOTE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        /**
         * Controller output
         * <li>
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        NEED_HOT_WATER(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY)),

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
     * Temperature in the water tank is low.
     *
     * @return the Channel
     */

    default Channel<Boolean> temperatureLow() {
        return this.channel(ChannelId.TEMP_LOW);
    }

    /**
     * Controller output, signalling need for hot water.
     *
     * @return the Channel
     */

    default WriteChannel<Boolean> remoteHotWaterSignal() {
        return this.channel(ChannelId.HOT_WATER_REMOTE);
    }

    /**
     * Controller output, signalling need for hot water.
     *
     * @return the Channel
     */

    default Channel<Boolean> needHotWater() {
        return this.channel(ChannelId.NEED_HOT_WATER);
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
