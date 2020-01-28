package io.openems.edge.lucidcontrol.device.task;

import io.openems.edge.bridge.lucidcontrol.task.AbstractLucidControlBridgeTask;
import io.openems.edge.bridge.lucidcontrol.task.LucidControlBridgeTask;
import io.openems.edge.common.channel.Channel;

public class LucidControlReadTask extends AbstractLucidControlBridgeTask implements LucidControlBridgeTask {

    private Channel<Double> barChannel;
    private String path;
    private String voltage;
    private int pinPos;
    private double lastBarValue = 0;

    public LucidControlReadTask(String moduleId, String deviceId, String path, String voltage, int pinPos, Channel<Double> barChannel) {
        super(moduleId, deviceId);

        this.barChannel = barChannel;
        this.path = path;
        this.voltage = voltage;
        this.pinPos = pinPos;
    }


    @Override
    public void setResponse(double voltageRead) {
        this.barChannel.setNextValue(voltageRead / 2);

    }


    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public int getPinPos() {
        return this.pinPos;
    }
}
