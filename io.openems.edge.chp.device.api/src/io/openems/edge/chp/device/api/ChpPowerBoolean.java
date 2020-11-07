package io.openems.edge.chp.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface ChpPowerBoolean extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * How much Percent of the Chp Power will be used.
         *
         * <ul>
         * <li>Interface: PowerLevel
         * <li>Type: Integer
         * <li>Unit: Percent
         * </ul>
         */

        POWER_LEVEL(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)); //
        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    default WriteChannel<Boolean> setPower() {
        return this.channel(ChannelId.POWER_LEVEL);
    }



}
