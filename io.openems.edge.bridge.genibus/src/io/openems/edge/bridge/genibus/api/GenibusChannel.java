package io.openems.edge.bridge.genibus.api;

import io.openems.common.channel.Debounce;
import io.openems.common.channel.Level;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface GenibusChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        SLAVE_COMMUNICATION_FAILED(Doc.of(Level.FAULT) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        CYCLE_TIME_IS_TOO_SHORT(Doc.of(Level.WARNING) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        EXECUTION_DURATION(Doc.of(OpenemsType.LONG)),


        //0/3
        APDU_MEASURED_DATA(Doc.of(OpenemsType.INTEGER)),
        //2/3
        APDU_COMMANDS(Doc.of(OpenemsType.INTEGER)),
        //0/2/3
        APDU_CONFIGURATION_PARAMETERS(Doc.of(OpenemsType.INTEGER)),
        //0/2/3
        APDU_REFERENCE_VALUES(Doc.of(OpenemsType.INTEGER)),
        //0
        APDU_ASCII_STRINGS(Doc.of(OpenemsType.INTEGER));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }

    }

    default Channel<Integer> getApduMeasuredData() {
        return this.channel(ChannelId.APDU_MEASURED_DATA);
    }

    default Channel<Integer> getApduCommands() {
        return this.channel(ChannelId.APDU_COMMANDS);
    }

    default Channel<Integer> getApduConfigurationParameters() {
        return this.channel(ChannelId.APDU_CONFIGURATION_PARAMETERS);
    }

    default Channel<Integer> getApduReferenceValues() {
        return this.channel(ChannelId.APDU_REFERENCE_VALUES);
    }

    default Channel<Integer> getAsciiStrings() {
        return this.channel(ChannelId.APDU_ASCII_STRINGS);
    }

}
