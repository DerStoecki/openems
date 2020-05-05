package io.openems.edge.chp.device.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;

public interface ChpBasic extends OpenemsComponent {


    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        ON_OFF(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        FORWARD_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),
        REWIND_TEMP(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS)),
        ELECTRICAL_POWER(Doc.of(OpenemsType.FLOAT).unit(Unit.KILOWATT)),
        ERROR(Doc.of(OpenemsType.BOOLEAN)),
        WARNING(Doc.of(OpenemsType.BOOLEAN)),
        READY(Doc.of(OpenemsType.BOOLEAN));
        private final Doc doc;


        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    default Channel<Boolean> setOnOff(){
        return this.channel((ChannelId.ON_OFF));
    }

    default Channel<Integer> getForwardTemp(){
        return this.channel(ChannelId.FORWARD_TEMP);
    }
default Channel<Integer> getRewindTemp(){
        return this.channel(ChannelId.REWIND_TEMP);
}

default Channel<Float> getElectricalPower(){
        return this.channel(ChannelId.ELECTRICAL_POWER);
}

default Channel<Boolean> isError(){
        return this.channel(ChannelId.ERROR);
}
default Channel<Boolean> isWarning(){
        return this.channel(ChannelId.WARNING);
}

default Channel<Boolean> isReady(){
        return this.channel(ChannelId.READY);
}

}
