package io.openems.edge.controller.pump.grundfos.api;


import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface PumpGrundfosControllerChannels extends OpenemsComponent {
    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        MAX_PRESSURE(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR)),
        H_REF_MAX(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR).accessMode(AccessMode.READ_WRITE)),
        H_REF_MIN(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR).accessMode(AccessMode.READ_WRITE)),
        R_REM(Doc.of(OpenemsType.DOUBLE).unit(Unit.PERCENT).accessMode(AccessMode.READ_WRITE));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }

    }

    default Channel<Double> getMaxPressure() {
        return channel(ChannelId.MAX_PRESSURE);
    }

    default WriteChannel<Double> setHrefMax() {
        return channel(ChannelId.H_REF_MAX);
    }

    default WriteChannel<Double> setHrefMin() {
        return channel(ChannelId.H_REF_MIN);
    }

    default WriteChannel<Double> setRrem() {
        return channel(ChannelId.R_REM);
    }
}


