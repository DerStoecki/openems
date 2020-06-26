package io.openems.edge.controller.passing.valvepumpcontrol.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface ValvePumpControlChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /**
         * Set status for the valve override setting. True means open, false means close.
         * <ul>
         * <li>Open or close
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        VALVE_OVERRIDE_OPEN_CLOSE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)
                .onInit(channel -> { //
                    // on each Write to the channel -> set the value
                    ((BooleanWriteChannel) channel).onSetNextWrite(value -> {
                        channel.setNextValue(value);
                    });
                })),

        /**
         * Tells the controller to activate the valve override.
         * <ul>
         * <li>If the override is active.
         * <li>Type: Boolean
         * <li>
         * </ul>
         */

        ACTIVATE_VALVE_OVERRIDE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)
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
     * Set status for the valve override setting. True means open, false means close.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> setValveOverrideOpenClose() {
        return this.channel(ChannelId.VALVE_OVERRIDE_OPEN_CLOSE);
    }

    /**
     * Activate the valve override.
     *
     * @return the Channel
     */

    default WriteChannel<Boolean> activateValveOverride() {
        return this.channel(ChannelId.ACTIVATE_VALVE_OVERRIDE);
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
