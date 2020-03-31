package io.openems.edge.bridge.lmnwired.api;

import java.io.ByteArrayOutputStream;

/**
 * Device Data for HDLC Layer
 *
 * @author Leonid Verhovskij
 *
 */
public class Device {
    private byte hdlcAddress;
    private byte serialNumber[];
    private byte timeSlot;
    private byte responseData[];
    private byte status[] = new byte[] { 0x00, 0x00 };
    private boolean devicePresent = true;

    public Device(byte hdlcAddress, byte[] serialNumber) {
        this.hdlcAddress = hdlcAddress;
        this.serialNumber = serialNumber;
    }

    public byte getHdlcAddress() {
        return hdlcAddress;
    }

    public void setHdlcAddress(byte hdlcAddress) {
        this.hdlcAddress = hdlcAddress;
    }

    public byte getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(byte timeSlot) {
        this.timeSlot = timeSlot;
    }

    public byte[] getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(byte[] serialNumber) {
        this.serialNumber = serialNumber;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public void setResponseData(byte responseData[]) {
        this.responseData = responseData;
    }

    public void setPresent() {
        devicePresent = true;
    }

    public void setAbsent() {
        devicePresent = false;
    }

    public boolean isPresent() {
        return devicePresent;
    }

    public byte[] getBytesForAddress(int timeslot) {
        devicePresent = false;
        ByteArrayOutputStream bytesForAddress = new ByteArrayOutputStream();
        try {
            bytesForAddress.write(new byte[] { hdlcAddress });
            bytesForAddress.write(timeslot);
            bytesForAddress.write(serialNumber);
            bytesForAddress.write(serialNumber);
            bytesForAddress.write(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return bytesForAddress.toByteArray();
    }

    public byte[] getBytesForObisRequest(String obisShort) {

        // Format: 1-0:obisShort*255
        String obis = "1-0:" + obisShort + "*255";

        return obis.getBytes();
    }

}
