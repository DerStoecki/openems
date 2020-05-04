package io.openems.edge.bridge.i2c.task;

public abstract class AbstractPcaTask implements I2cPcaTask {
    private String pcaModuleId;
    private String deviceId;

    public AbstractPcaTask(String pcaModuleId, String deviceId) {
        this.pcaModuleId = pcaModuleId;
        this.deviceId = deviceId;
    }

    @Override
    public String getPcaModuleId() {
        return pcaModuleId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }
}
