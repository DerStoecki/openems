package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestReadRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestRemoteReadTask extends AbstractRestRemoteDeviceTask implements RestReadRequest {

    private String deviceId;
    private String slaveId = "COMMUNICATOR IS NOT A SLAVE";
    private String masterId = "COMMUNICATOR IS NOT A MASTER";
    private boolean master;
    private String realDeviceId;
    private String channel;
    private boolean autoAdapt;
    private boolean isRelaysCloser;
    private String requestString;
    private Channel<String> value;

    public RestRemoteReadTask(String remoteDeviceId, String masterSlaveId, Boolean master, String realDeviceId, String deviceChannel, boolean autoAdapt, Channel<String> value, String deviceType) {

        //super(....)
        super();
        this.deviceId = remoteDeviceId;
        this.master = master;
        if (master) {
            this.masterId = masterSlaveId;
        } else {
            this.slaveId = masterSlaveId;
        }
        this.realDeviceId = realDeviceId;
        this.channel = deviceChannel;
        this.autoAdapt = autoAdapt;
        this.value = value;

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
                //TODO
            }
        }
    }

}
