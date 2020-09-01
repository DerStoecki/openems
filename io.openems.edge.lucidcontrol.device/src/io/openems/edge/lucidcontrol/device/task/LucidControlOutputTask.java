package io.openems.edge.lucidcontrol.device.task;

import io.openems.edge.bridge.lucidcontrol.task.AbstractLucidControlBridgeTask;
import io.openems.edge.bridge.lucidcontrol.task.LucidControlBridgeTask;
import io.openems.edge.common.channel.Channel;

public class LucidControlOutputTask extends AbstractLucidControlBridgeTask implements LucidControlBridgeTask {

    private Channel<Double> percentChannel;
    private String path;
    private String voltage;
    private int pinPos;
    private double lastVoltValue = 0;
    //will be changed, just a placeholder atm
    private double maxPressure;
    //max Voltage is needed later depending on module and device; atm we just need 10V
    private int maxVoltage;

    public LucidControlOutputTask(String moduleId, String deviceId, String path, String voltage, int pinPos,
                                  Channel<Double> percentChannel) {
        super(moduleId, deviceId);

        this.percentChannel = percentChannel;
        this.path = path;
        this.voltage = voltage;
        this.pinPos = pinPos;
        allocateMaxVoltage();
    }

    private void allocateMaxVoltage() {
        maxVoltage = Integer.parseInt(voltage.replaceAll("\\D+", ""));

    }

    @Override
    public void setResponse(double voltageRead) {
    }

    /**
     * path of the LucidControModule.
     *
     * @return the path.
     */
    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean writeTaskDefined() {
        return this.percentChannel.value().isDefined();
    }


    /**
     * Pin Position of the Device.
     *
     * @return pinPosition.
     */
    public int getPinPos() {
        return this.pinPos;
    }

    /**
     * Returns the Request String to write to a device.
     */
    @Override
    public String getRequest() {
        return " -w" + calculateVoltage() + " -c" + this.pinPos + " -tV";
    }

    private double calculateVoltage() {
        double volt = this.percentChannel.value().get() * maxVoltage / 100;
        this.lastVoltValue = volt;
        return volt;
    }

    @Override
    public boolean isRead() {
        return false;
    }


}
