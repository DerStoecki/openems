package io.openems.edge.bridge.genibus.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;

import io.openems.edge.bridge.genibus.protocol.Telegram;

public class Handler {

    SerialPort serialPort;
    protected String portName;
    protected long timeout;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    OutputStream os;
    InputStream is;

    private final Logger log = LoggerFactory.getLogger(Handler.class);

    public void start(String portName) {
        this.portName = portName;

        try {
            SerialPort serialPort = SerialPortBuilder.newBuilder(portName).setBaudRate(9600)
                    .setDataBits(DataBits.DATABITS_8).setParity(Parity.NONE).setStopBits(StopBits.STOPBITS_1).build();
            os = serialPort.getOutputStream();
            is = serialPort.getInputStream();

        } catch (Exception e) {
            log.info("Error openening Port: " + e.getMessage());
        }
    }

    public boolean checkStatus() {

		/*
		SerialPort[] serialPorts = SerialPort.getCommPorts();
		serialPortFound = false;
		for (SerialPort tmpSerialPort : serialPorts) {
			String tmpPortName = "/dev/" + tmpSerialPort.getSystemPortName();
			if (tmpPortName.equals(portName)) {
				serialPortFound = true;
			}
		}

		if (serialPort instanceof SerialPort && serialPortFound) {
			return true;
		} else {
			if (serialPort instanceof SerialPort) {
				serialPort.closePort();
			}
			return false;
		}
		*/
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
            System.out.println("SD not OK");
            return false;
        }
        //Look for Length (LE), Check package length match
        boolean lengthOK = false;
        if (bytesCurrentPackage.length >= 2 && bytesCurrentPackage[1] == bytesCurrentPackage.length - 4) {
            lengthOK = true;
        }
        if (!lengthOK) { //collect more data
            System.out.println("Length not OK");
            return false;
        }
        //Check crc from relevant message part

        ByteArrayOutputStream bytesCRCRelevant = new ByteArrayOutputStream();
        bytesCRCRelevant.write(bytesCurrentPackage, 1, bytesCurrentPackage.length - 3);
        byte[] crc = Telegram.getCRC(bytesCRCRelevant.toByteArray());
        int length = bytesCurrentPackage.length;

        if (bytesCurrentPackage[length - 2] != crc[0] || bytesCurrentPackage[length - 1] != crc[1]) {
            System.out.println("CRC compare not OK");
            return false; //cancel operation
        }
        return true;
    }

    public void stop() {
        try {
            os.flush();
            serialPort.close();
            os = null;
            is = null;
        } catch (IOException e) {
            System.out.println("Error closing port: " + e.getMessage());
        }
        //		serialPort.removeDataListener();
        //		serialPort.closePort();
    }

    /**
     * .
     *
     * @param timeout in Milliseconds.
     * @param task    .
     * @return .
     */

    public Telegram writeTelegram(long timeout, Telegram task) {

        /*
         * Send data and save return handling task
         */
        try {
            // Send Reqeust

            byte[] bytes = task.getBytes();
            os.write(bytes);
            // Debug output data hex values
            System.out.println("Bytes send: " + bytesToHex(bytes));

            // Save return function/Task
            return handleResponse(task);

        } catch (Exception e) {
            System.out.println("Error while sending data: " + e.getMessage());
        }
        return null;
    }

    private Telegram handleResponse(Telegram task) {
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
                System.out.println("Data received: " + bytesToHex(receivedData));

                if (packageOK(receivedData)) {
                    System.out.println("CRC Check ok.");
                    // if all done create telegram
                    task = Telegram.parseEventStream(receivedData);
                    //task.setResponse(telegram);

                }
                break;
            }
            return task;
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

}
