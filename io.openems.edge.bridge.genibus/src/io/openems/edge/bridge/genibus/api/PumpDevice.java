package io.openems.edge.bridge.genibus.api;

import io.openems.edge.bridge.genibus.api.task.GenibusTask;
import io.openems.edge.common.taskmanager.TasksManager;

import java.util.ArrayList;
import java.util.List;

public class PumpDevice {

    /**
     * The Parent component.
     */
    private final String pumpDeviceId;

    /**
     * TaskManager that contains all tasks.
     */
    private final TasksManager<GenibusTask> taskManager = new TasksManager<>();

    /**
     * Queue of tasks that should be sent to the device. This list is filled by getting tasks from the task manager.
     * When a telegram is created, the tasks are taken from this queue. When a task is picked it is removed from the
     * queue. A telegram has limited capacity, so it may happen that tasks remain in the queue after a telegram is
     * created. The tasks then stay in the queue until they can be placed in a telegram.
     */
    private final List<GenibusTask> taskQueue = new ArrayList<>();

    /**
     * This list is for priority once tasks that also have INFO. They need two executions to get the complete
     * information. They are placed in this list for the second execution.
     */
    private final List<GenibusTask> onceTasksWithInfo = new ArrayList<>();

    private int deviceByteBufferLength = 70;
    private int genibusAddress;
    private int lowPrioTasksPerCycle;
    private boolean connectionOk = true;    // Initialize with true, to avoid "no connection" message on startup.
    private long timestamp;
    private long executionDuration = 200;    // Milliseconds. This information is currently not used.
    private boolean allLowPrioTasksAdded;
    private boolean addAllOnceTasks = true;
    private double[] millisecondsPerByte = {3.0, 3.0, 3.0};    // Rough estimate. Exact value measured at runtime.
    private int arrayTracker = 0;

    // These are needed to calculate the setpoint, since GENIbus setpoints are relative to the sensor range.
    private double pressureSensorMinBar;
    private double pressureSensorRangeBar;

    public PumpDevice(String deviceId, int genibusAddress, int lowPrioTasksPerCycle, GenibusTask... tasks) {
        this.genibusAddress = genibusAddress;
        this.pumpDeviceId = deviceId;
        this.lowPrioTasksPerCycle = lowPrioTasksPerCycle;
        for (GenibusTask task : tasks) {
            addTask(task);
        }
        timestamp = System.currentTimeMillis() - 1000;
    }

    public synchronized void addTask(GenibusTask task) {
        task.setPumpDevice(this);
        this.taskManager.addTask(task);
    }

    public TasksManager<GenibusTask> getTaskManager() {
        return taskManager;
    }

    public List<GenibusTask> getTaskQueue() { return taskQueue; }

    public List<GenibusTask> getOnceTasksWithInfo() { return onceTasksWithInfo; }

    public synchronized void setDeviceByteBufferLength(int value) {
        // 70 is minimum buffer length.
        if (value >= 70) {
            deviceByteBufferLength = value;
        }
    }

    /**
     * Gets the buffer length of this GENIbus device in byte. The buffer length is the maximum length a telegram can
     * have that is sent to this device.
     * @return
     */
    public int getDeviceByteBufferLength() {
        return deviceByteBufferLength;
    }

    public int getGenibusAddress() {
        return genibusAddress;
    }

    public int getLowPrioTasksPerCycle() {
        return lowPrioTasksPerCycle;
    }

    public String getPumpDeviceId() {
        return pumpDeviceId;
    }

    /**
     * Set a timestamp for this pump device. This is done once per cycle when the first telegram is sent to this pump.
     * This information is used to track which pump has already received a telegram this cycle.
     */
    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the last timestamp of this pump device.
     * This information is used to track which pump has already received a telegram this cycle.
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }

    // This information is currently not used.
    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    public long getExecutionDuration() {
        return executionDuration;
    }

    /**
     * This method is used to store timing information about telegram send and receive. The timing information is
     * used to estimate if a telegram can still fit in a cycle or not.
     * To send and receive a telegram takes roughly 33 ms + 3 ms per byte of the APDU. The 33 ms is for an empty telegram.
     * For a more accurate timing the ms per byte is measured and stored with this method. The method saves the last
     * three values so an average can be calculated.
     *
     * @param millisecondsPerByte
     */
    public void setMillisecondsPerByte(double millisecondsPerByte) {
        // Save three values so we can average and the value won't jump that much.
        this.millisecondsPerByte[arrayTracker] = millisecondsPerByte;
        arrayTracker++;
        if (arrayTracker >= 3) {
            arrayTracker = 0;
        }
    }

    /**
     * Gets the time in ms that is added to a telegram process (send and receive) per byte in the apdu. An empty
     * telegram takes ~33 ms to send and receive, each byte in the apdus adds "this" amount of ms to the process.
     *
     * @return
     */
    public double getMillisecondsPerByte() {
        // Average over the three entries.
        double returnValue = 0;
        for (int i = 0; i < 3; i++) {
            returnValue += millisecondsPerByte[i];
        }
        return returnValue / 3;
    }

    public void setAllLowPrioTasksAdded(boolean allLowPrioTasksAdded) {
        this.allLowPrioTasksAdded = allLowPrioTasksAdded;
    }

    public boolean isAllLowPrioTasksAdded() {
        return allLowPrioTasksAdded;
    }

    public double getPressureSensorMinBar() {
        return pressureSensorMinBar;
    }

    public void setPressureSensorMinBar(double pressureSensorMinBar) {
        this.pressureSensorMinBar = pressureSensorMinBar;
    }

    public double getPressureSensorRangeBar() {
        return pressureSensorRangeBar;
    }

    public void setPressureSensorRangeBar(double pressureSensorRangeBar) {
        this.pressureSensorRangeBar = pressureSensorRangeBar;
    }

    public boolean isConnectionOk() {
        return connectionOk;
    }

    public void setConnectionOk(boolean connectionOk) {
        this.connectionOk = connectionOk;
    }

    public boolean isAddAllOnceTasks() {
        return addAllOnceTasks;
    }

    public void setAddAllOnceTasks(boolean addAllOnceTasks) {
        this.addAllOnceTasks = addAllOnceTasks;
    }

    // In case of connection loss. The pump might have been turned off and back on, or another pump is now using this
    // address. Reset everything, as if the program had just started.
    public void resetDevice() {
        taskManager.getAllTasks().forEach(task -> {
            task.resetInfo();
        });
        addAllOnceTasks = true;
        deviceByteBufferLength = 70;
        millisecondsPerByte[0] = 3.0;
        millisecondsPerByte[1] = 3.0;
        millisecondsPerByte[2] = 3.0;
    }
}
