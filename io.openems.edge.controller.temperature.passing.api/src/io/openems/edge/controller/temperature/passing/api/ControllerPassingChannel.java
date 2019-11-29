package io.openems.edge.controller.temperature.passing.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface ControllerPassingChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /** ActivateOrNot.
         * <ul>
         * <li>Interface: ControllerPassingChannel
         * <li>Type: boolean
         * <li>Unit: ON_OFF
         * </ul>
         */


        ON_OFF(Doc.of(OpenemsType.BOOLEAN).unit(Unit.ON_OFF).accessMode(AccessMode.READ_WRITE)), //

        /**
         * Min Temperature.
         * <ul>
         * <li> Min Temperature that has to be reached
         * <li>Type: Integer
         * <li>Unit: Dezidegree Celsius
         * </ul>
         */

        MIN_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE)),

        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    /**
     * Activate/Deactivate the Passing Station.
     *
     * @return the Channel
     */
    default WriteChannel<Boolean> getOnOff_PassingController() {
        return this.channel(ChannelId.ON_OFF);
    }

    /**
     * Min Temperature you want to reach / check if it can be reached.
     *
     * @return the Channel
     */
    default Channel<Integer> getMinTemperature() {
        return this.channel(ChannelId.MIN_TEMPERATURE);
    }


}
