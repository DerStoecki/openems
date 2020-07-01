package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import com.pi4j.wiringpi.Spi;
import org.osgi.service.cm.ConfigurationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sc16IS752Impl implements Sc16IS752 {
    private int spiChannel;
    private int frequency;
    private String id;
    private String versionId;
    //sets 7 5 4 3 to output and 0 1 2 6 to input
    // out == 0; in == 1;
    private byte[] basicSetup = {(byte) 0x0A, (byte) 0x47};
    private Map<String, Sc16ReadTask> readTasks = new ConcurrentHashMap<>();
    private Map<String, Sc16WriteTask> writeTasks = new ConcurrentHashMap<>();


    @Override
    public void initialize(int spiChannel, int frequency, String id, String versionId) throws ConfigurationException {
        this.spiChannel = spiChannel;
        this.frequency = frequency;
        this.id = id;
        this.versionId = versionId;
        spiSetup();
    }

    @Override
    public void shift() throws ConfigurationException {
        //data[0] == initAddress; 0x00 ist the init Data
        byte[] data = {0x0B, 0x00};
        this.writeTasks.values().forEach(task -> {
            int valueForGpio = task.getRequest() ? 1 : 0;
            data[1] |= (valueForGpio << task.getPin());
        });

        if (Spi.wiringPiSPIDataRW(spiChannel, data) == -1) {

            spiSetup();
        }
        this.readTasks.values().forEach(task -> {
            int response = data[0];
            task.setResponse((response >> task.getPin()) & 1);
        });

    }

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

    @Override
    public void removeTask(String id) {
        this.readTasks.remove(id);
    }

    private void spiSetup() throws ConfigurationException {
        if (Spi.wiringPiSPISetup(spiChannel, frequency) == -1) {
            throw new ConfigurationException(Integer.toString(spiChannel), "SpiChannel not available");
        }
        Spi.wiringPiSPIDataRW(spiChannel, basicSetup);
    }

    @Override
    public int getSpiChannel() {
        return this.spiChannel;
    }

    @Override
    public int getFrequency() {
        return this.frequency;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getVersionId() {
        return this.versionId;
    }
}
