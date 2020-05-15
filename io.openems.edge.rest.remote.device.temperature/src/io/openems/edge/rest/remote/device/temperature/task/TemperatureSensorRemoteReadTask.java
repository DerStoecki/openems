package io.openems.edge.rest.remote.device.temperature.task;

import io.openems.edge.bridge.rest.communcation.task.RestReadRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureSensorRemoteReadTask implements RestReadRequest {

    private String deviceId;
    private String slaveId = "COMMUNICATOR IS NOT A SLAVE";
    private String masterId = "COMMUNICATOR IS NOT A MASTER";
    private boolean master;
    private String realTemperatureSensor;
    private Channel<Integer> temperature;

    public TemperatureSensorRemoteReadTask(String deviceId, String masterSlaveId, boolean master, String realTemperatureSensor, Channel<Integer> temperature) {
        this.deviceId = deviceId;
        this.master = master;
        if (master) {
            this.masterId = masterSlaveId;
        } else {
            this.slaveId = masterSlaveId;
        }
        this.realTemperatureSensor = realTemperatureSensor;
        this.temperature = temperature;

    }


    @Override
    public void setResponse(boolean succ, String answer) {

        if (succ) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(answer);
            StringBuilder answerNumeric = new StringBuilder();
            while (m.find()) {
                answerNumeric.append(m.group());
            }
            if (!answerNumeric.toString().equals("")) {
                temperature.setNextValue(answerNumeric.toString());
            }
        }
    }

    @Override
    public String getRequest() {
        return realTemperatureSensor + "/" + "Temperature";
    }

    @Override
    public String getAutoAdaptRequest() {
        return null;
    }

    @Override
    public String getMasterId() {
        return masterId;
    }

    @Override
    public String getSlaveId() {
        return slaveId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getRealDeviceId() {
        return null;
    }

    @Override
    public boolean isMaster() {
        return master;
    }

    @Override
    public String getDeviceType() {
        return null;
    }

    @Override
    public boolean isAutoAdapt() {
        return false;
    }

    @Override
    public boolean setAutoAdaptResponse(boolean succ, String answer) {
        return false;
    }

    @Override
    public boolean isInverseSet() {
        return false;
    }
}
