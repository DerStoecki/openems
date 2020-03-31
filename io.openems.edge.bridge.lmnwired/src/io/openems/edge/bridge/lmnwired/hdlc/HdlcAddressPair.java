package io.openems.edge.bridge.lmnwired.hdlc;

public class HdlcAddressPair {

    HdlcAddress hdlcSourceAddress;
    HdlcAddress hdlcDestinationAddress;

    public HdlcAddressPair(HdlcAddress hdlcSourceAddress, HdlcAddress hdlcDestinationAddress) {
        this.hdlcSourceAddress = hdlcSourceAddress;
        this.hdlcDestinationAddress = hdlcDestinationAddress;
    }

    public byte[] getBytes() {
        return new byte[] {hdlcSourceAddress.getByte(),  hdlcDestinationAddress.getByte()};
    }

}
