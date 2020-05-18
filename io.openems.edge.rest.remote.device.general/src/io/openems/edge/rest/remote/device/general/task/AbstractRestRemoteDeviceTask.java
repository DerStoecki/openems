package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRestRemoteDeviceTask implements RestRequest {

    private String remoteDeviceId;
    private String deviceChannel;
    private boolean autoAdapt;
    private String realDeviceId;
    private String deviceType;
    boolean isInverse = false;
    private boolean isInverseSet;

    AbstractRestRemoteDeviceTask(String remoteDeviceId, String realDeviceId, String deviceChannel,
                                 boolean autoAdapt, String deviceType) {
        this.remoteDeviceId = remoteDeviceId;

        this.deviceChannel = deviceChannel;
        if(deviceType.toLowerCase().equals("relays")) {
            this.autoAdapt = autoAdapt;
        } else {
            this.autoAdapt = false;
        }

        this.realDeviceId = realDeviceId;
        this.deviceType = deviceType;
    }

    @Override
    public String getRequest() {
        return this.realDeviceId + "/" + this.deviceChannel;
    }



    @Override
    public String getDeviceId() {
        return this.remoteDeviceId;
    }


    @Override
    public String getDeviceType() {
        return this.deviceType;
    }

    @Override
    public String getRealDeviceId() {
        return this.realDeviceId;
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
            if (this.deviceType.toLowerCase().equals("relays")) {
                return this.realDeviceId + "/" + "IsCloser";
            } else {
                return "Device is Not Supported via AutoAdapt";
            }
        }
        return "AutoAdaptNotSet!";
    }

    @Override
    public boolean setAutoAdaptResponse(boolean succ, String answer) {
        if (isInverseSet) {
            return true;
        } else if (succ) {
            //only true or false allowed
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(answer);
            StringBuilder answerNumeric = new StringBuilder();
            while (m.find()) {
                answerNumeric.append(m.group());
            }
            if (!answerNumeric.toString().equals("")) {
                this.isInverse = answerNumeric.toString().equals("0");
                this.isInverseSet = true;
                return true;
            }
        }
        return false;
    }
}
