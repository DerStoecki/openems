package io.openems.edge.i2c.mcp.api;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import io.openems.edge.i2c.mcp.api.task.McpTask;
import io.openems.edge.i2c.mcp.api.task.RelaysTask;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//USED BY RELAYSMODULE
public class Mcp23008 extends Mcp implements McpChannelRegister {

    private String parentCircuitBoard;
    private final int length = 8;
    private I2CDevice device;
    private Map<Integer, Boolean> valuesPerDefault = new ConcurrentHashMap<>();
    private final boolean[] shifters;
    private final Map<String, RelaysTask> tasks = new ConcurrentHashMap<>();
    private boolean wasRemoved = false;
    private String address;
    private I2CBus i2CBus;

    public Mcp23008(String address, I2CBus device, String parentCircuitBoard) throws IOException {
        this.parentCircuitBoard = parentCircuitBoard;
        this.shifters = new boolean[length];

        for (int i = 0; i < length; i++) {
            this.shifters[i] = false;
        }
        this.address = address;
        this.i2CBus = device;
        getPhysicalDevice();
        int data = 0x00;
            this.device.write(0x00, (byte) data);
    }

    private boolean getPhysicalDevice() throws IOException {

        switch (address) {
            case "0x22":
                this.device = i2CBus.getDevice(0x22);
                break;
            case "0x24":
                this.device = i2CBus.getDevice(0x24);
                break;
            case "0x26":
                this.device = i2CBus.getDevice(0x26);
                break;
            default:
                this.device = i2CBus.getDevice(0x20);
                break;
        }
        return true;
    }

    public void setPosition(int position, boolean activate) {
        if (position < this.length) {
            this.shifters[position] = activate;
        } else {
            throw new IllegalArgumentException("There is no such position." + position + " maximum is " + this.length);
        }
    }

    /**
     * <p>
     * Gets all RelaysTasks; gets their next write value and writes them in the"SetPosition" --> shifters.
     * Explained at the SetPosition Method.
     * After every Task is handled. The Byte data gets all data from the shifters (is true or false).
     * This data will be written in the device at the 0x09 address.
     * </p>
     */
    @Override
    public void shift(){

            for (RelaysTask task : tasks.values()) {

            task.getWriteChannel().getNextWriteValueAndReset()
                    .ifPresent(aboolean -> {
                        setPosition(task.getPosition(), aboolean);
                        task.getWriteChannel().setNextValue(aboolean);
                    });
        }

        byte data = 0x00;
        for (int i = length - 1; i >= 0; i--) {
            data = (byte) (data << 1);
            if (this.shifters[i]) {
                data += 1;
            }
        }
        try {
            if(wasRemoved) {
                try {
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_1);
                } catch (I2CFactory.UnsupportedBusNumberException e) {
                    e.printStackTrace();
                }
                getPhysicalDevice();
            }
            device.write(0x09, data);
            wasRemoved = false;
        } catch (IOException ex) {
            ex.printStackTrace();
            wasRemoved = true;
        }
    }


    /**
     * Gets a position and default status for deactivation method.
     * @param position position of the task.
     * @param activate is default position true or false (Opener is closed; Closer is open)
     */
    @Override
    public void addToDefault(int position, boolean activate) {
        this.valuesPerDefault.put(position, activate);
    }

    @Override
    public Map<Integer, Boolean> getValuesPerDefault() {
        return valuesPerDefault;
    }

    @Override
    public String getParentCircuitBoard() {
        return parentCircuitBoard;
    }

    /**
     * Resets the values to default (setPosition for shifters); and then shifts them.
     * */
    @Override
    public void deactivate() {
        for (Map.Entry<Integer, Boolean> entry : getValuesPerDefault().entrySet()) {
            setPosition(entry.getKey(), entry.getValue());
        }
            shift();
    }

    @Override
    public void addTask(String id, McpTask mcpTask) {
        if (mcpTask instanceof RelaysTask) {
            this.tasks.put(id, (RelaysTask) mcpTask);
        }
    }

    @Override
    public void removeTask(String id) {
        setPosition(this.tasks.get(id).getPosition(), valuesPerDefault.get(this.tasks.get(id).getPosition()));
        this.tasks.get(id);
        this.tasks.remove(id);
    }
}
