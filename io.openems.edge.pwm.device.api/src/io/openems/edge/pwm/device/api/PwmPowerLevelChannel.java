package io.openems.edge.pwm.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.FloatWriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface PwmPowerLevelChannel extends OpenemsComponent {

    /**
     * * How much of the low/highflank the device uses. e.g. 80% power --> like a dimming light and
     * it's "level" of brightness.
     * <ul>
     * <li>Interface: PowerLevel
     * <li>Type: Float
     * <li>Unit: Percent
     * </ul>
     */
    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        POWER_LEVEL(Doc.of(OpenemsType.FLOAT).unit(Unit.PERCENT).accessMode(AccessMode.READ_WRITE)); //
        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    default FloatWriteChannel getPwmPowerLevelChannel() {
        return this.channel(ChannelId.POWER_LEVEL);
    }
}

