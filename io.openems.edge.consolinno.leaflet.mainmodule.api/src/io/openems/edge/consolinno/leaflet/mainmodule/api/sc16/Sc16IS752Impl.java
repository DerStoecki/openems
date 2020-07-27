package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import com.pi4j.wiringpi.Spi;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class Sc16IS752Impl implements Sc16IS752 {
    private int spiChannel;
    private int frequency;
    private String id;
    private static DoubleUartRegistries SC16REGISTRY = DoubleUartRegistries.Sc16IS752;

    private boolean interruptSet;

    //private String versionId;
    //sets 7 5 4 3 to output and 0 1 2 6 to input
    // out == 0; in == 1;
    //io Dir recalculated 0x0A to 0x50
    private byte[] basicPinSetup = {calcForAddress(SC16REGISTRY.ioDir, false), (byte) 0x47};


    //changed via Config;
    private byte[] basicFcrSetup = {calcForAddress(SC16REGISTRY.fcr, false), 0};
    private byte[] basicTlrSetup = {calcForAddress(SC16REGISTRY.tlr, false), 0};
    private byte[] reset = {calcForAddress(SC16REGISTRY.ioControl, false), (byte) 0x09};

    //Address of Interrupt --> every set bit --> Change in state will cause interrupt
    private byte[] basicInterruptConfig = {calcForAddress(SC16REGISTRY.ioIntEna, false), 0};
    //Address for InterruptLatching --> change 2nd bit to 1 if you want your interrupts latched.
    private byte[] basicIoControl = {calcForAddress(SC16REGISTRY.ioControl, false), 0};

    private List<Sc16ReadTask> readTasks = new ArrayList<>();
    private List<Sc16WriteTask> writeTasks = new ArrayList<>();
    private final Logger log = LoggerFactory.getLogger(Sc16IS752Impl.class);

    /**
     * initializes the Sc16 with given param. and inits Connection.
     *
     * @param spiChannel Unique SPI Channel this Sc16 is communicating with.
     * @param frequency  Frequency of Clk.
     * @param id         unique Id of the Sc16 determined by Config of MainModuleId.
     * @param versionId  determines the Version of this. No concrete functionality yet. Comes in Future with more Versions.
     * @throws ConfigurationException if the SPI Channel isn't available.
     */
    @Override
    public void initialize(int spiChannel, int frequency, String id, String versionId, boolean interruptSet, String dataForType, String interruptType) throws ConfigurationException {
        this.spiChannel = spiChannel;
        this.frequency = frequency;
        this.id = id;
        this.interruptSet = interruptSet;
        // this.versionId = versionId
        dataForType = String.format("%8s", dataForType).replace(' ', '0');
        if (interruptSet) {
            if (validateDataForType(dataForType) == false) {
                throw new IllegalArgumentException(dataForType + " Contains illegal Arguments only put 0 and 1");
            }
            switch (interruptType) {
                case "FCR":
                    basicFcrSetup[1] = Byte.parseByte(dataForType);
                    break;
                case "TLR":
                    basicTlrSetup[1] = Byte.parseByte(dataForType);
                    break;
            }
        }
        spiSetup();
    }

    private boolean validateDataForType(String dataForType) {
        char[] dataCheck = dataForType.toCharArray();
        for (char c : dataCheck) {
            if (c != '0' & c != '1') {
                return false;
            }
        }
        return true;
    }

    /**
     * Shift starts the Communication of the DoubleUART via SPI. Utilizing it's given Tasks.
     * E.G. SC16 will communicate with SPI and sends Outputs and receive Data afterwards with the states of the GPIOs.
     *
     * @throws Throwable if Somethings wrong with the Communication.
     */

    @Override
    public void shift() throws Throwable {

        if (interruptSet) {
            isInterrupt();
        }
        //data[0] == registryAddress; 0x00 ist the init Data
        byte[] data = {calcForAddress(SC16REGISTRY.ioState, false), 0x00};
        this.writeTasks.forEach(task -> {
            //see if LEDs were set to 0 or not
            int valueForGpio = task.getRequest() ? 1 : 0;
            data[1] |= (valueForGpio << task.getPin());
        });

        if (Spi.wiringPiSPIDataRW(spiChannel, data) == -1) {
            log.error("Cannot Communicate to specified Address " + spiChannel + " SubAddress: "
                    + data[0] + " data " + data[1]);
            try {
                spiSetup();
            } catch (ConfigurationException e) {
                throw new Throwable();
            }
        }
        byte[] read = {calcForAddress(SC16REGISTRY.ioState, true), 0x00};
        Spi.wiringPiSPIDataRW(spiChannel, read);
        this.readTasks.forEach(task -> {
            int response = read[1] & 0xff;
            task.setResponse((response >> task.getPin()) & 1);
        });

    }

    /**
     * Checks if Interrupt has occurred.
     */

    private void isInterrupt() {

        byte[] interruptDataIir = {(byte) SC16REGISTRY.iir, 0};
        byte[] interruptDataLsr = {(byte) SC16REGISTRY.lsr, 0};
        Spi.wiringPiSPIDataRW(spiChannel, interruptDataIir);
        Spi.wiringPiSPIDataRW(spiChannel, interruptDataLsr);
        for (int i = 0; i < Byte.SIZE; i++) {
            int valueIir = ((interruptDataIir[0] >> i) & 1);
            int valueLsr = ((interruptDataLsr[0] >> i) & 1);
            if (valueIir != 0) {
                getInterruptInformationIir(i);
            }
            if (valueLsr != 0) {
                getInterruptInformationLsr(i);
            }
        }

    }

    /**
     * Get InterruptInformation of Lsr.
     * ATM Just Temporary
     *
     * @param interruptBit current SignalBit.
     */

    private void getInterruptInformationLsr(int interruptBit) {
        switch (interruptBit) {

            case 0:
                System.out.println("Data in Receiver");
                break;
            case 1:
                System.out.println("Overrun Error");
                break;
            case 2:
                System.out.println("Parity Error");
                break;
            case 3:
                System.out.println("Framing Error");
                break;
            case 4:
                System.out.println("Break Interrupt");
                break;
            case 5:
                System.out.println("THR Empty");
                break;
            case 6:
                System.out.println("THR and TSR empty");
                break;
            case 7:
                System.out.println("FIFO Data Error");
                break;
        }
    }

    /**
     * Get InterruptInformation of IIR communication;
     * ATM Just temporary with current impl.
     *
     * @param interruptBit which bit
     */
    private void getInterruptInformationIir(int interruptBit) {

        switch (interruptBit) {
            case 0:
                System.out.println("Interrupt was set");
                break;
            case 1:
                System.out.println("Lowest Priority");
                break;
            case 2:
                System.out.println("Low Priority");
                break;
            case 3:
                System.out.println("Mid Priority");
                break;
            case 4:
                System.out.println("High Priority");
                break;
            case 5:
                System.out.println("Highest Priority");
                break;
            case 6:
                System.out.println("FIFO Enable bit 6");
                break;
            case 7:
                System.out.println("FIFO Enable bit 7");
                break;
        }
    }

    /**
     * Add a Sc16Task. In Future it will add a UArtTask; But for now Sc16Task is enough.
     *

     * @param task Task created by the DoubleUARTDevice. Either Read or Write.
     * @throws ConfigurationException if the Id is already in tasks list --> Not Unique Id.
     */

    @Override
    public void addTask(Sc16Task task) throws ConfigurationException {
            if (task instanceof Sc16ReadTask) {
                this.readTasks.add((Sc16ReadTask) task);
            } else {
                this.writeTasks.add((Sc16WriteTask) task);
            }
        }

    /**
     * Setup connection with SPI. Using spichannel and frequency.
     *
     * @throws ConfigurationException if the SPIChannel is not Available --> Error at -1;
     */

    private void spiSetup() throws ConfigurationException {
        if (Spi.wiringPiSPISetup(spiChannel, frequency) == -1) {
            log.error("SPI Channel not available " + spiChannel);
            throw new ConfigurationException(Integer.toString(spiChannel), "SpiChannel not available");
        }
        // Spi.wiringPiSPIDataRW(spiChannel, reset);
        //set Pins
        Spi.wiringPiSPIDataRW(spiChannel, basicPinSetup);
        //Setup for Interrupt; Default : 0 (no interrupts when GPIO state changes.)
        //  Spi.wiringPiSPIDataRW(spiChannel, basicInterruptConfig);
        //interrupts is latched or not Default : 0 (no latch)
        //Spi.wiringPiSPIDataRW(spiChannel, basicIoControl);

        //Spi.wiringPiSPIDataRW(spiChannel, basicFcrSetup);
        //Spi.wiringPiSPIDataRW(spiChannel, basicTlrSetup);
    }


    private byte calcForAddress(int ioDir, boolean isRead) {
        //7th bit + ioDir Bits + 3 shift
        ioDir = ioDir << 3;
        if (isRead) {
            return (byte) (ioDir | 0x80);
        } else {
            return (byte) ioDir;
        }
    }

    @Override
    public int getSpiChannel() {
        return this.spiChannel;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
