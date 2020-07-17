package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.nature;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface Sc16Nature extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        LED_RED_STATUS(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE).onInit(
                channel -> { //
                    ((BooleanWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        LED_YELLOW_STATUS(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE).onInit(
                channel -> { //
                    ((BooleanWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        LED_GREEN_STATUS(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE).onInit(
                channel -> { //
                    ((BooleanWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        ENABLE_OUTPUT(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE).onInit(
                channel -> { //
                    ((BooleanWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        H_BUS_IN_5V_ERROR_FLAG_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        H_BUS_IN_24V_ERROR_FLAG_STATUS(Doc.of(OpenemsType.BOOLEAN)),
        OUTPUT_VOLTAGE_FLAG(Doc.of(OpenemsType.BOOLEAN));
        private final Doc doc;


        ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    default WriteChannel<Boolean> ledRedStatus() {
        return this.channel(ChannelId.LED_RED_STATUS);
    }

    default WriteChannel<Boolean> ledYellowStatus() {
        return this.channel(ChannelId.LED_YELLOW_STATUS);
    }

    default WriteChannel<Boolean> ledGreenStatus() {
        return this.channel(ChannelId.LED_GREEN_STATUS);
    }

    default WriteChannel<Boolean> enableOutput() {
        return this.channel(ChannelId.ENABLE_OUTPUT);
    }

    default Channel<Boolean> hBus5V() {
        return this.channel(ChannelId.H_BUS_IN_5V_ERROR_FLAG_STATUS);
    }

    default Channel<Boolean> hBus24V() {
        return this.channel(ChannelId.H_BUS_IN_24V_ERROR_FLAG_STATUS);
    }

    default Channel<Boolean> outputVoltageFlag() {
        return this.channel(ChannelId.OUTPUT_VOLTAGE_FLAG);
    }
}
