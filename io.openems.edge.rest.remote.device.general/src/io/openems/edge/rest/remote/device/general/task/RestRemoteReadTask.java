package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestReadRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestRemoteReadTask extends AbstractRestRemoteDeviceTask implements RestReadRequest {


    private Channel<String> value;

    public RestRemoteReadTask(String remoteDeviceId, String realDeviceId,
                              String deviceChannel, boolean autoAdapt, Channel<String> value, String deviceType) {

        super(remoteDeviceId, realDeviceId, deviceChannel, autoAdapt, deviceType);

        this.value = value;

    }

    @Override
    public void setResponse(boolean succ, String answer) {

        if (succ) {
            Pattern p = Pattern.compile("[-+]?([0-9]*[.][0-9]+|[0-9]+)");
            Matcher m = p.matcher(answer);
            StringBuilder answerNumeric = new StringBuilder();
            while (m.find()) {
                answerNumeric.append(m.group());
            }
            if (!answerNumeric.toString().equals("")) {
                this.value.setNextValue(answerNumeric);
            }
        }
    }

}
