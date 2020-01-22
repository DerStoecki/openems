package io.openems.edge.bridge.gpio.task;

import io.openems.edge.bridge.gpio.api.GpioBridge;

public abstract class AbstractGpioBridgeTask implements GpioBridgeTask {

    private String deviceId;

    public AbstractGpioBridgeTask(String deviceId) {
        this.deviceId = deviceId;
    }


    @Override
    public String getDeviceId() {
        return this.deviceId;
    }



}
