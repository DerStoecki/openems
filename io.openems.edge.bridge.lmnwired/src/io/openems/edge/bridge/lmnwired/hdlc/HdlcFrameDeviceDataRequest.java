package io.openems.edge.bridge.lmnwired.hdlc;

import io.openems.edge.bridge.lmnwired.api.Device;

public class HdlcFrameDeviceDataRequest extends HdlcFrame{
    public HdlcFrameDeviceDataRequest(Device device, String obis) {
        byte data[] = obis.getBytes();
        setFormat(data.length + 10);
        setDestination((byte) 0, device.getHdlcAddress(), (byte) 0, (byte) 0x03);
        setSource((byte) 0x01);
        setCtrl((byte) 0x13);
        setData(data);
    }
}
