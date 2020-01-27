package io.openems.edge.bridge.lucidcontrol.task;

import io.openems.edge.bridge.lucidcontrol.api.LucidControlBridge;

public abstract class AbstractLucidControlBridgeTask implements LucidControlBridge {

    private String moduleId;
    private String deviceId;


    public AbstractLucidControlBridgeTask(String moduleId, String deviceId) {
        this.moduleId = moduleId;
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getModuleId() {
        return this.moduleId;
    }

}
