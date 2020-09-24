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
     * TaskManager for ReadTasks.
     */
    private final TasksManager<GenibusTask> taskManager = new TasksManager<>();

    private final List<GenibusTask> taskQueue = new ArrayList<>();

    private final List<GenibusTask> onceTasksWithInfo = new ArrayList<>();

    private int deviceByteBufferLength = 70;
    private int genibusAddress;
    private int lowPrioTasksPerCycle;
    private long timestamp;
    private long executionDuration = 200;    // Milliseconds
    private boolean allLowPrioTasksAdded;
    private double[] millisecondsPerByte = {3.0, 3.0, 3.0};    // Conservative estimate. Based on a 70 byte telegram taking 190 ms to process (send and receive).
    private int arrayTracker = 0;

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

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    public long getExecutionDuration() {
        return executionDuration;
    }

    public void setMillisecondsPerByte(double millisecondsPerByte) {
        this.millisecondsPerByte[arrayTracker] = millisecondsPerByte;
        arrayTracker++;
        if (arrayTracker >= 3) {
            arrayTracker = 0;
        }
    }

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
}
