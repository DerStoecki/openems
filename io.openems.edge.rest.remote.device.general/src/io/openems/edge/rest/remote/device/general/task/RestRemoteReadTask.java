package io.openems.edge.rest.remote.device.general.task;

import io.openems.edge.bridge.rest.communcation.task.RestReadRequest;
import io.openems.edge.common.channel.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestRemoteReadTask extends AbstractRestRemoteDeviceTask implements RestReadRequest {


    private Channel<String> value;
    private Channel<String> unit;
    private boolean unitSet;

    public RestRemoteReadTask(String remoteDeviceId, String realDeviceId, String deviceChannel,
                              boolean autoAdapt, Channel<String> value, String deviceType, Channel<String> unit) {

        super(remoteDeviceId, realDeviceId, deviceChannel, autoAdapt, deviceType);

        this.value = value;
        this.unit = unit;

    }

    @Override
    public void setResponse(boolean succ, String answer) {

        if (succ) {
            setResponseValue(answer);
            if (!unitSet) {
                setResponseUnit(answer);
            }
        }
    }

    private void setResponseUnit(String answer) {
        if (answer.contains("Unit")) {
            String[] parts = answer.split("\"Unit\"");
            if (parts[1].contains("\"")) {

                String newParts = parts[1].substring(parts[1].indexOf("\""), parts[1].indexOf("\"", parts[1].indexOf("\"")+1));
                System.out.println(newParts);
                parts[1] = parts[1].replace("\"", "");
            }
            if (parts[1].contains("}")) {
                parts[1] = parts[1].replace("}", "");
            }
            if(parts[1].contains(":")){
                parts[1] = parts[1].replace(":", "");
            }
            this.unit.setNextValue(parts[1]);
            this.unitSet = true;

        }
    }

    private void setResponseValue(String answer) {

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
