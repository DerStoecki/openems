package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRestRemoteDeviceTask implements RestRequest {

    private String remoteDeviceId;
    private boolean isMaster;
    private String slaveId = "COMMUNICATOR IS NOT A SLAVE";
    private String masterId = "COMMUNICATOR IS NOT A MASTER";
    private String deviceChannel;
    private boolean autoAdapt;
    private String realDeviceId;
    private String deviceType;
    boolean isInverse;
    private boolean isInverseSet;

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
        this.deviceType = deviceType;
        this.isInverse = isInverse;

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

    @Override
    public boolean isInverseSet() {
        return this.isInverseSet;
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

    @Override
    public boolean setAutoAdaptResponse(boolean succ, String answer) {

        if (succ && !isInverseSet) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(answer);
            StringBuilder answerNumeric = new StringBuilder();
            while (m.find()) {
                answerNumeric.append(m.group());
            }
            if (!answerNumeric.toString().equals("")) {

                boolean asBoolean = Boolean.parseBoolean(answerNumeric.toString());
                this.isInverse = !asBoolean;
                this.isInverseSet = true;
                return true;
            }


        }
        return false;

    }
}
