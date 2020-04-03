package io.openems.edge.controller.pid.passing.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface PidForPassingNature extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /**
         * Min Temperature.
         * <ul>
         * <li> Min Temperature that has to be reached
         * <li>Type: Integer
         * <li>Unit: Decimal degrees Celsius
         * </ul>
         */

        MIN_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_WRITE));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }


    /**
     * Min Temperature you want to reach / check if it can be reached.
     *
     * @return the Channel
     */
    default WriteChannel<Integer> setMinTemperature() {
        return this.channel(ChannelId.MIN_TEMPERATURE);
    }



}