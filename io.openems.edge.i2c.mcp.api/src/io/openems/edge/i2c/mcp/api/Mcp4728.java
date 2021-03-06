package io.openems.edge.i2c.mcp.api;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import io.openems.edge.i2c.mcp.api.task.AbstractChpTask;
import io.openems.edge.i2c.mcp.api.task.McpTask;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//USED BY CHP MODULE
public class Mcp4728 extends Mcp implements McpChannelRegister {
    private String parentCircuitBoard;
    private final int length = 4;
    private I2CDevice device;
    private int[] values;
    private Map<String, AbstractChpTask> tasks = new ConcurrentHashMap<>();


    public Mcp4728(String address, String parentCircuitBoard, I2CBus device) throws IOException {
        values = new int[4];
        this.parentCircuitBoard = parentCircuitBoard;
        for (short x = 0; x < length; x++) {
            values[x] = 0;
        }
        //More addresses to come with further Modules / versions
        switch (address) {
            case "0x60":
            default:
                this.device = device.getDevice(0x60);
                break;
        }
        int zero = 0x00;
        int data = 0x00;
        try {
            this.device.write(zero, (byte) data);
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * gets the digit-value of it's chp-tasks, save them to their correct position and
     * setting them all in one in this.device.write.
     * max digitValue = 4095 bc Mcp supports only 12bit
     */
    public void shift() {

        int maxDigitValue = 4095;
        for (AbstractChpTask task : tasks.values()) {
            int digitValue = task.getDigitValue();
            values[task.getPosition()] = digitValue;
            if (values[task.getPosition()] < 0) {
                values[task.getPosition()] = 0;
            } else if (values[task.getPosition()] > maxDigitValue) {
                values[task.getPosition()] = maxDigitValue;
            }
        }

        try {
            byte[] allVoltage = setAllVoltage();
            this.device.write(0x50, allVoltage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds the Chp Task to the task-map.
     *  @param id      unique id of the Chp Device
     * @param mcpTask the created McpTask by the Chp device.
     */
    @Override
    public void addTask(String id, McpTask mcpTask) {
        if (mcpTask instanceof AbstractChpTask) {
            this.tasks.put(id, (AbstractChpTask) mcpTask);
        }
    }

    /**
     * Deactivating the Chp and removing them from the map.
     *
     * @param id Chp Id.
     */
    @Override
    public void removeTask(String id) {
        this.values[this.tasks.get(id).getPosition()] = 0;
        this.tasks.remove(id);
        this.shift();
    }

    /**
     * Writing all the digit-values from the correct position into a byte array.
     * 2 bytes == one digit byte[0][1] for position 0; [2][3] for pos 1 ....
     *
     * @return the byteArray that'll be written in the device.
     */

    private byte[] setAllVoltage() {

        byte[] allVoltage = new byte[8];
        int change1;
        int change2;
        for (short x = 0, y = 0; x < 8; y++) {
            change1 = this.values[y];
            change2 = this.values[y];
            allVoltage[x] = (byte) ((change1 >> 8) & 0xFF);
            x++;
            allVoltage[x] = (byte) (change2 & 0xFF);
            x++;
        }
        return allVoltage;
    }

    /**
     * Setting every value to 0 and write them in the device to deactivate them.
     */

    public void deactivate() {
        for (short x = 0; x < length; x++) {
            this.values[x] = 0;
            try {
                byte[] allVoltage = setAllVoltage();
                this.device.write(0x50, allVoltage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getParentCircuitBoard() {
        return parentCircuitBoard;
    }

    //Those Functions are not needed yet.
    @Override
    public void setPosition(int position, boolean activate) {

    }

    @Override
    public Map<Integer, Boolean> getValuesPerDefault() {
        return null;
    }

    @Override
    public void addToDefault(int position, boolean activate) {

    }

}
