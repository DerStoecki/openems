package io.openems.edge.bridge.lmnwired.hdlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HdlcFrame {
    HdlcAddress hdlcSourceAddress;
    HdlcAddress hdlcDestinationAddress;
    HdlcAddressPair addressPair;
    int sendSequence;
    int receiveSequence;
    boolean segmented;
    boolean addLcc;

    byte flag = 0x7E;
    byte format[] = new byte[2]; // 2 bytes
    byte dest[] = new byte[2];
    byte source = 0;
    byte ctrl = 0;
    byte hcs[];
    byte hcsData[];
    byte data[];
    byte fcs[] = new byte[2];
    byte fcsData[];

    byte[] retData;
    int length;

    enum ResponseType {
        AddressAssignmentResponse,
        DataRequestResponse
    }

    public static HdlcFrame createHdlcFrameFromByte(byte data[]) {
        HdlcFrame hdlcFrame = new HdlcFrame();

        if (data.length < 12) {
            System.out.println("HDLC Frame length to short, exit");
            return null;
        }

        // Format
        int length = getCurrentPackageLength(data);

        //if((data.length - 2) != length && data[data.length -2 ] != crc) {
        if ((data.length - 2) != length) {
            System.out.println("Frame size invalid! Length: " + data.length + "Calc. Length: " + length);
            return null;
        }

        //length = (data.length - 2);

        hdlcFrame.setFormat(length);

        // Dest
        byte type = (byte) ((byte) (0x80 & data[3]) >> 7);
        byte destination = (byte) (0x7F & data[3]);
        byte timeslots = (byte) ((byte) (0xFC & data[4]) >> 2);
        byte protocol = (byte) (0x03 & data[4]);

        hdlcFrame.setDestination(type, destination, timeslots, protocol);

        // Source
        hdlcFrame.setSource(data[5]);

        // Ctrl
        hdlcFrame.setCtrl(data[6]);

        // Data
        int dataLength = length - 10 + 9;
        if (dataLength >= 0 && data.length >= 9 && data.length >= dataLength)
            hdlcFrame.setData(Arrays.copyOfRange(data, 9, dataLength));

        // HCS
        byte tmpHCS[] = new byte[2];
        tmpHCS[0] = data[7];
        tmpHCS[1] = data[8];

        hdlcFrame.setHCSData();
        byte newHCS[] = hdlcFrame.setHCS();

        if (!Arrays.equals(tmpHCS, newHCS)) {
            System.out.println("HCS Value not valid!");
            return null;
        }

        // FCS
        byte tmpFCS[] = new byte[2];
        tmpFCS[0] = data[data.length - 3];
        tmpFCS[1] = data[data.length - 2];

        hdlcFrame.setFCSData();
        byte newFCS[] = hdlcFrame.setFCS();

        // Check FCS
        if (!Arrays.equals(tmpFCS, newFCS)) {
            System.out.println("FCS Value not valid!");
            return null;
        }

        return hdlcFrame;
    }

    /**
     * @param data
     * @return
     */
    public static int getCurrentPackageLength(byte[] data) {
        if (data.length >= 3) {
            int length = 0x07 & data[1];
            length <<= 8;
            length |= Byte.toUnsignedInt(data[2]);
            return length;
        } else {
            return 0;
        }
    }

    /**
     * @param length num data bytes
     * @return
     */
    public boolean setFormat(int length) {
        format[0] = (byte) 0xA0;

        format[0] |= (byte) (length >> 8);
        format[1] = (byte) length;

        return true;
    }

    /**
     * @param type
     * @param destination
     * @param timeslots
     * @param protocol
     * @return
     */
    public boolean setDestination(byte type, byte destination, byte timeslots, byte protocol) {

        dest[1] |= timeslots << 2;
        dest[1] |= protocol;
        dest[0] |= type << 7;
        dest[0] |= destination;

        return true;
    }

    public boolean setSource(byte source) {
        this.source = source;

        return true;
    }

    public boolean setCtrl(byte ctrl) {
        this.ctrl = ctrl;

        return true;
    }

    public byte[] setHCS() {
        if (data != null) {
            short calcHCS = (short) calculateCRC(hcsData);
            hcs = new byte[2];
            hcs[1] = (byte) calcHCS;
            hcs[0] = (byte) (calcHCS >> 8);
            return hcs;
        } else {
            hcs = null;
            return null;
        }
    }

    public byte[] getHCSData() {
        ByteArrayOutputStream hcsOutput = new ByteArrayOutputStream();
        try {
            hcsOutput.write(format);
            hcsOutput.write(dest);
            hcsOutput.write(new byte[]{source});
            hcsOutput.write(new byte[]{ctrl});

        } catch (IOException e) {
            e.printStackTrace();
        }
        return hcsOutput.toByteArray();
    }

    private void setHCSData() {
        hcsData = getHCSData();

    }

    public boolean setData(byte data[]) {
        this.data = data;

        return true;
    }

    public ResponseType responseType() {
        return ResponseType.DataRequestResponse;
    }

    public byte[] setFCS() {
        short calcFCS = (short) calculateCRC(fcsData);

        fcs[1] = (byte) calcFCS;
        fcs[0] = (byte) (calcFCS >> 8);

        return fcs;
    }

    public byte[] getFCSData() {
        ByteArrayOutputStream fcsOutput = new ByteArrayOutputStream();
        try {
            fcsOutput.write(hcsData);
            if (hcs != null)
                fcsOutput.write(hcs);
            if (data != null)
                fcsOutput.write(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fcsOutput.toByteArray();
    }

    private void setFCSData() {
        fcsData = getFCSData();
    }

    public byte[] getBytes() {

        // First Step: Combine Ctrl, Source, Dest and Format
        setHCSData();

        // Second Step: Calc and add HCS if needed
        setHCS();

        //Set FCSData
        setFCSData();

        // Third Step: Add FCS and add combine with data
        setFCS();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write(new byte[]{flag});
            output.write(fcsData);
            output.write(fcs);
            output.write(new byte[]{flag});
        } catch (IOException e) {
            e.printStackTrace();
        }

        retData = output.toByteArray();
        length = retData.length;

        return retData;
    }

    public int getLength() {
        return length;
    }

    /**
     * CRC16-CCITT
     */
    public int calculateCRC(byte[] bytes) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return crc;
    }

    public byte[] getData() {
        return data;
    }

    public byte getSource() {
        return source;
    }

}
