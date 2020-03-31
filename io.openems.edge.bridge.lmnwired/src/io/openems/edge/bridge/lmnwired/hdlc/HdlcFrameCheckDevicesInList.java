package io.openems.edge.bridge.lmnwired.hdlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import io.openems.edge.bridge.lmnwired.api.Device;

public class HdlcFrameCheckDevicesInList extends HdlcFrame {

    /**
     *
     * @param timeslots
     */
    public HdlcFrameCheckDevicesInList(byte timeslots, List<Device> deviceList) {
        setFormat(deviceList.size() * 32 + 10);
        setDestination((byte) 1, (byte) 0x7F, (byte) timeslots, (byte) 0x02);
        setSource((byte) 0x01);
        setCtrl((byte) 0x13);

        ByteArrayOutputStream dataClients = new ByteArrayOutputStream();
        for(Device device: deviceList) {

            try {
                //Write serialNumber from device with timeslot
                dataClients.write(device.getBytesForAddress(deviceList.indexOf(device) + 1));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        setData(dataClients.toByteArray());
    }
}
