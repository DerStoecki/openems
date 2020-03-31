package io.openems.edge.bridge.lmnwired.api;

import io.openems.edge.bridge.lmnwired.hdlc.HdlcFrame;
import io.openems.edge.common.channel.Channel;

public class ChannelDataRecordMapper {

    protected HdlcFrame data;

    public ChannelDataRecordMapper(HdlcFrame data, Channel<?> channel) {
        this.data = data;
        channel.setNextValue(new String(data.getData()));
    }

    public HdlcFrame getData() {
        return data;
    }

    public void setData(HdlcFrame data) {
        this.data = data;
    }

}
