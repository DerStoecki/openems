package io.openems.edge.bridge.genibus.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.openems.edge.bridge.genibus.GenibusImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;
import gnu.io.*;

import io.openems.edge.bridge.genibus.protocol.Telegram;


public class Handler {

    private SerialPort serialPort;
    protected String portName;
    protected long timeout;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    OutputStream os;
    InputStream is;

    private final Logger log = LoggerFactory.getLogger(Handler.class);
    protected final GenibusImpl parent;

    public Handler(GenibusImpl parent) {
        this.parent = parent;
    }

    public boolean start(String portName) {
        this.portName = portName;
        String[] serialPortsOfSystem =  SerialPortBuilder.getSerialPortNames();
        boolean portFound = false;
        this.parent.logInfo(this.log, "--Starting serial connection--");
        if (serialPortsOfSystem.length == 0) {
            this.parent.logError(this.log, "No serial ports found or nothing plugged in.");
            return false;
        }
        for (String entry : serialPortsOfSystem) {
            this.parent.logInfo(this.log, "Found serial port: " + entry);
            if (entry.contains(this.portName)) {
                portFound = true;
            }
        }

        if (portFound) {
            try {
                serialPort = SerialPortBuilder.newBuilder(portName).setBaudRate(9600)
                        .setDataBits(DataBits.DATABITS_8).setParity(Parity.NONE).setStopBits(StopBits.STOPBITS_1).build();
                is = serialPort.getInputStream();
                os = serialPort.getOutputStream();
            } catch (IOException e) {
                this.parent.logError(this.log, "Failed to open connection on port " + portName);
                e.printStackTrace();
                return false;
            }
            this.parent.logInfo(this.log, "Connection opened on port " + portName);
            return true;
        } else {
            this.parent.logError(this.log, "Configuration error: The specified serial port " + portName
                    + " does not match any of the available ports or nothing is plugged in. Please check configuration and/or make sure the connector is plugged in.");
            return false;
        }

    }

    public boolean checkStatus() {
        if (os != null) {
            // os in not null when a connection was established at some point by the start() method.
            try {
                // Test the connection by trying to write something to the output stream os. Writes a single 0, should
                // not interfere with anything.
                os.write(0);
            } catch (IOException e) {
                this.parent.logError(this.log, "Serial connection lost on port " + portName + ". Attempting to reconnect...");

                if (serialPort != null) {
                    try {
                        serialPort.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return false;

            }
        } else {
            // If os is null, there has not been a connection yet.
            return false;
        }
        return true;
    }


    public boolean packageOK(byte[] bytesCurrentPackage) {
        //Look for Start Delimiter (SD)
        boolean sdOK = false;
        if (bytesCurrentPackage.length >= 1) {
            switch (bytesCurrentPackage[0]) {
                case 0x27:
                case 0x26:
                case 0x24:
                    sdOK = true;
                    break;
                default:
                    sdOK = false;
            }
        }
        if (!sdOK) { //wrong package start, reset
            this.parent.logWarn(this.log, "SD not OK");
            return false;
        }
        //Look for Length (LE), Check package length match
        boolean lengthOK = false;
        if (bytesCurrentPackage.length >= 2 && bytesCurrentPackage[1] == bytesCurrentPackage.length - 4) {
            lengthOK = true;
        }
        if (!lengthOK) { //collect more data
            this.parent.logWarn(this.log, "Length not OK");
            return false;
        }
        //Check crc from relevant message part

        ByteArrayOutputStream bytesCRCRelevant = new ByteArrayOutputStream();
        bytesCRCRelevant.write(bytesCurrentPackage, 1, bytesCurrentPackage.length - 3);
        byte[] crc = Telegram.getCRC(bytesCRCRelevant.toByteArray());
        int length = bytesCurrentPackage.length;

        if (bytesCurrentPackage[length - 2] != crc[0] || bytesCurrentPackage[length - 1] != crc[1]) {
            this.parent.logWarn(this.log, "CRC compare not OK");
            return false; //cancel operation
        }
        return true;
    }

    public void stop() {
        if (os != null) {
            try {
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        os = null;
        is = null;
        if (serialPort == null) {
            this.parent.logError(this.log, "serialPort is null. This should never happen.");
            return;
        }
        if (serialPort.isClosed()) {
            this.parent.logInfo(this.log, "serialPort is already closed.");
            return;
        }
        try {
            serialPort.close();
        } catch (IOException e) {
            this.parent.logError(this.log, "Error closing port: " + e.getMessage());
        }
    }

    /**
     * .
     *
     * @param timeout in Milliseconds.
     * @param task    .
     * @return .
     */

    public Telegram writeTelegram(long timeout, Telegram task, boolean debug) {

        /*
         * Send data and save return handling task
         */
        try {
            // Send Reqeust

            byte[] bytes = task.getBytes();
            os.write(bytes);
            if (debug) {
                // Debug output data hex values
                //this.parent.logInfo(this.log, "Bytes send: " + bytesToHex(bytes));
                this.parent.logInfo(this.log, "Bytes send: " + bytesToInt(bytes));
            }

            // Save return function/Task
            return handleResponse(task, debug);

        } catch (Exception e) {
            this.parent.logError(this.log, "Error while sending data: " + e.getMessage());
        }
        return null;
    }

    private Telegram handleResponse(Telegram task, boolean debug) {
        try {
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < 500) {
                byte[] readBuffer = new byte[1024];
                int numRead = is.available();
                if (numRead <= 0) {
                    continue;
                }
                is.read(readBuffer, 0, numRead);
                ByteArrayOutputStream bytesRelevant = new ByteArrayOutputStream();
                bytesRelevant.write(readBuffer, 0, numRead);
                byte[] receivedData = bytesRelevant.toByteArray();
                if (debug) {
                    // Debug return data hex values
                    //this.parent.logInfo(this.log, "Data received: " + bytesToHex(receivedData));
                    this.parent.logInfo(this.log, "Data received: " + bytesToInt(receivedData));
                }

                if (packageOK(receivedData)) {
                    if (debug) {
                        this.parent.logInfo(this.log, "CRC Check ok.");
                    }
                    // if all done create telegram
                    task = Telegram.parseEventStream(receivedData);
                    return task;
                    //task.setResponse(telegram);

                }
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("0x%02x ", b));
        }
        return sb.toString();

    }

    private static String bytesToInt(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            int convert = Byte.toUnsignedInt(b);
            sb.append(String.format("%d ", convert));
        }
        return sb.toString();

    }

}
