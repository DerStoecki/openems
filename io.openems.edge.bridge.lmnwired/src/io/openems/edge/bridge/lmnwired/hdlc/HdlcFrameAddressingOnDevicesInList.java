package io.openems.edge.bridge.lmnwired.hdlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import io.openems.edge.bridge.lmnwired.api.Device;

public class HdlcFrameAddressingOnDevicesInList extends HdlcFrame {

    /**
     *
     * @param timeslots
     */
    public HdlcFrameAddressingOnDevicesInList(byte timeslots, List<Device> deviceList) {
        setFormat(deviceList.size() * 32 + 10);
        setDestination((byte) 1, (byte) 0x7F, (byte) timeslots, (byte) 0x01);
        setSource((byte) 0x01);
        setCtrl((byte) 0x13);

        ByteArrayOutputStream dataClients = new ByteArrayOutputStream();
        for(Device device: deviceList) {
            try {
                dataClients.write(device.getBytesForAddress(0));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        setData(dataClients.toByteArray());
    }
}
