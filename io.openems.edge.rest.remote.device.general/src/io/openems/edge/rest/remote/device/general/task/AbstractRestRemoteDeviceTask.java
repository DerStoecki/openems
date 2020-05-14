package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestRequest;

public abstract class AbstractRestRemoteDeviceTask implements RestRequest {

    private String remoteDeviceId;
    private boolean isMaster;
    private String slaveId = "COMMUNICATOR IS NOT A SLAVE";
    private String masterId = "COMMUNICATOR IS NOT A MASTER";
    private String deviceChannel;
    private boolean autoAdapt;
    private String realDeviceId;
    private String deviceType;

    AbstractRestRemoteDeviceTask(String remoteDeviceId, String slaveMasterId, boolean isMaster,
                                 String realDeviceId, String deviceChannel, boolean autoAdapt, String deviceType) {
        this.remoteDeviceId = remoteDeviceId;
        this.isMaster = isMaster;
        if (isMaster) {
            this.masterId = slaveMasterId;
        } else {
            this.slaveId = slaveMasterId;
        }
        this.deviceChannel = deviceChannel;
        this.autoAdapt = autoAdapt;
        this.realDeviceId = realDeviceId;

    }

    @Override
    public String getRequest() {
        return this.realDeviceId + "/" + this.deviceChannel;
    }

    @Override
    public String getMasterId() {
        return this.masterId;
    }

    @Override
    public String getSlaveId() {
        return this.slaveId;
    }

    @Override
    public String getDeviceId() {
        return this.remoteDeviceId;
    }

    @Override
    public boolean isMaster() {
        return this.isMaster;
    }

    @Override
    public String getDeviceType() {
        return this.deviceType;
    }

    @Override
    public boolean isAutoAdapt() {
        return this.autoAdapt;

    }

    public String getAutoAdaptRequest() {
        if (isAutoAdapt()) {
            if (this.deviceType.equals("Relays")) {
                return this.realDeviceId + "/" + "OnOff";
            } else {
                return "Device is Not Supported via AutoAdapt";
            }
        }
        return "AutoAdaptNotSet!";
    }

}
