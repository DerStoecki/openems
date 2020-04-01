package io.openems.edge.bridge.lmnwired.hdlc;

public class HdlcFrameAddressingOnEmptyList extends HdlcFrame {

    /**
     *
     * @param timeslots
     */
    public HdlcFrameAddressingOnEmptyList(byte timeslots) {
        setFormat(8);
        setDestination((byte)1, (byte)0x7F, (byte)timeslots, (byte)0x01);
        setSource((byte)0x01);
        setCtrl((byte)0x13);
    }

}
