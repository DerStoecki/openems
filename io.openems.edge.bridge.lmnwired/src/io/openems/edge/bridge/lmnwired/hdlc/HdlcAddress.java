package io.openems.edge.bridge.lmnwired.hdlc;

public class HdlcAddress {

    int hdlcAddress;

    public HdlcAddress(int i) throws Exception{
        if(i <= 32 && i > 0)
            hdlcAddress = i;
        else
            throw new Exception("HDLC Address must be between 1 and 32");
    }

    public byte getByte() {
        return (byte)hdlcAddress;
    }

}
