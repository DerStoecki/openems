package io.openems.edge.consolinno.leaflet.mainmodule.api;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.osgi.service.cm.ConfigurationException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Pca9536MainModuleProvider extends AbstractPcaMainModuleProvider {

    private I2CDevice device;


    public Pca9536MainModuleProvider(int bus, int address, String mainModuleVersion, String moduleId) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException {
        super(mainModuleVersion, moduleId);
        I2CBus bus1 = allocateBus(bus);
        this.device = bus1.getDevice(address);
        configureReadAndWrite();
        //        switch (address) {
        //            case "0x41":
        //            default:
        //                this.device = bus1.getDevice(0x41);
        //                break;
        //        }

    }

    private void configureReadAndWrite() throws IOException {
        switch (super.getVersion()) {
            case "0.05":
            default:
                this.device.write(0x03, (byte) 0xFD);

        }

    }

    private I2CBus allocateBus(int bus_address) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException {
        switch (bus_address) {

            case 0:
                return I2CFactory.getInstance(I2CBus.BUS_0);
            case 1:
                return I2CFactory.getInstance(I2CBus.BUS_1);

            case 2:
                return I2CFactory.getInstance(I2CBus.BUS_2);

            case 3:
                return I2CFactory.getInstance(I2CBus.BUS_3);

            case 4:
                return I2CFactory.getInstance(I2CBus.BUS_4);

            case 5:
                return I2CFactory.getInstance(I2CBus.BUS_5);

            case 6:
                return I2CFactory.getInstance(I2CBus.BUS_6);

            case 7:
                return I2CFactory.getInstance(I2CBus.BUS_7);

            case 8:
                return I2CFactory.getInstance(I2CBus.BUS_8);

            case 9:
                return I2CFactory.getInstance(I2CBus.BUS_9);

            case 10:
                return I2CFactory.getInstance(I2CBus.BUS_10);

            case 11:
                return I2CFactory.getInstance(I2CBus.BUS_11);

            case 12:
                return I2CFactory.getInstance(I2CBus.BUS_12);

            case 13:
                return I2CFactory.getInstance(I2CBus.BUS_13);

            case 14:
                return I2CFactory.getInstance(I2CBus.BUS_14);

            case 15:
                return I2CFactory.getInstance(I2CBus.BUS_15);

            case 16:
                return I2CFactory.getInstance(I2CBus.BUS_16);

            case 17:
                return I2CFactory.getInstance(I2CBus.BUS_17);


        }
        throw new ConfigurationException("I2CBus", "I2cBus not supported!: " + bus_address);
    }

    @Override
    public boolean getDataOnPinPosition(int position) throws IOException {
        byte[] data = new byte[1];
        //byte pinPosition = (byte) (position & 0x0F);
        byte pinPosition = calculatePinPos(position);
        data[0] = (byte) device.read(0x00);
        if ((data[0] & pinPosition) == 0) {
            return (isValueInverse(position, false));
        }
        return isValueInverse(position, true);
    }

    private byte calculatePinPos(int position) {
        switch (position) {
            case 1:
                return (byte) 2;
            case 2:
                return (byte) 4;
            case 3:
                return (byte) 8;

            case 0:
            default:
                return (byte) 1;
        }
    }

    private boolean isValueInverse(int position, boolean readData) {
        switch (super.getVersion()) {
            case "0.05":
            default:
                switch (position) {
                    case 0:
                    case 2:
                    case 3:
                        return !readData;
                }
        }
        return readData;
    }

    @Override
    public void writeToPinPosition(boolean onOff) throws IOException {
        byte[] address;

        switch (super.getVersion()) {
            case "0.05":
            default:
                int valueToWrite = 0xFD;
                if (onOff) {
                    valueToWrite = 0xFF;
                }
                address = ByteBuffer.allocate(4).putInt(valueToWrite).array();
        }
        this.device.write(0x01, address);
    }
}
