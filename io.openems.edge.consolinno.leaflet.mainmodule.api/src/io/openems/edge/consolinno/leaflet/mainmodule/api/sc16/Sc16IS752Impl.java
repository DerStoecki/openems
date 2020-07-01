package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import com.pi4j.wiringpi.Spi;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sc16IS752Impl implements Sc16IS752 {
    private int spiChannel;
    private int frequency;
    private String id;
    //private String versionId;
    //sets 7 5 4 3 to output and 0 1 2 6 to input
    // out == 0; in == 1;
    private byte[] basicSetup = {(byte) 0x0A, (byte) 0x47};
    private Map<String, Sc16ReadTask> readTasks = new ConcurrentHashMap<>();
    private Map<String, Sc16WriteTask> writeTasks = new ConcurrentHashMap<>();
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
    public void initialize(int spiChannel, int frequency, String id, String versionId) throws ConfigurationException {
        this.spiChannel = spiChannel;
        this.frequency = frequency;
        this.id = id;
        // this.versionId = versionId;
        spiSetup();
    }

    /**
     * Shift starts the Communication of the DoubleUART via SPI. Utilizing it's given Tasks.
     * E.G. SC16 will communicate with SPI and sends Outputs and receive Data afterwards with the states of the GPIOs.
     *
     * @throws Throwable if Somethings wrong with the Communication.
     */

    @Override
    public void shift() throws Throwable {
        //data[0] == registryAddress; 0x00 ist the init Data
        byte[] data = {0x0B, 0x00};
        this.writeTasks.values().forEach(task -> {
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
        this.readTasks.values().forEach(task -> {
            int response = data[0];
            task.setResponse((response >> task.getPin()) & 1);
        });

    }

    /**
     * Add a Sc16Task. In Future it will add a UArtTask; But for now Sc16Task is enough.
     *
     * @param id   Unique Id of this task, usually from the DoubleUArtDevice you activate in Config.
     * @param task Task created by the DoubleUARTDevice. Either Read or Write.
     * @throws ConfigurationException if the Id is already in tasks list --> Not Unique Id.
     */

    @Override
    public void addTask(String id, Sc16Task task) throws ConfigurationException {

        if (this.readTasks.containsKey(id) || this.writeTasks.containsKey(id)) {
            throw new ConfigurationException("ID Already in Tasks", id + " already used; please change Unique id");
        } else {
            if (task instanceof Sc16ReadTask) {
                this.readTasks.put(id, (Sc16ReadTask) task);
            } else {
                this.writeTasks.put(id, (Sc16WriteTask) task);
            }
        }
    }

    /**
     * Removes the Task given previously by the DobuleUARTDevice.
     *
     * @param id unique ID of the Task.
     */

    @Override
    public void removeTask(String id) {
        this.readTasks.remove(id);
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
        Spi.wiringPiSPIDataRW(spiChannel, basicSetup);
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
