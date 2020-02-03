package io.openems.edge.bridge.genibus.api;

public class Device {

    protected byte address;

    public Device(int address) {
        setAddress((byte)address);
    }

    public byte getAddress() {
        return address;
    }

    public void setAddress(byte address) {
        this.address = address;
    }
}
