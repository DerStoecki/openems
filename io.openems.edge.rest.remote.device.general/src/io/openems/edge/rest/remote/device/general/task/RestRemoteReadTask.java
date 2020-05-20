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

    /**
     * Called by the Rest Bridge sets answer after successful REST Communication.
     *
     * @param succ   declares successful communication.
     * @param answer the REST Response from the GET Method.
     */
    @Override
    public void setResponse(boolean succ, String answer) {

        if (succ) {
            setResponseValue(answer);
            if (!unitSet) {
                setResponseUnit(answer);
            }
        }
    }

    /**
     * Only Calld until unitSet = true. Sets the Unit of the Remote Device.
     *
     * @param answer REST GET Answer
     *               <p>Splits the Answer into 2 pieces, when Unit is found. Complete Unit.toString() of
     *               the Device is gotten and written into Remote Device Unit Channel.
     *               </p>
     */
    private void setResponseUnit(String answer) {
        if (answer.contains("Unit")) {
            String[] parts = answer.split("\"Unit\"");
            if (parts[1].contains("\"")) {

                String newParts = parts[1].substring(parts[1].indexOf("\""), parts[1].indexOf("\"", parts[1].indexOf("\"") + 1));
                newParts = newParts.replace("\"", "");
                this.unit.setNextValue(newParts);
                this.unitSet = true;
            }
        }
    }

    /**
     * Sets the Value of the REST GET Method.
     *
     * @param answer REST response
     *               <p>Get only Number Value and set that value to the Value of the Remote Device.
     *               Splits after "value" and get the substring of 0 and "\"" ---> only the Value number Part will be
     *               considered. ---> Get Only Numbers (with optional floatingpoint) .
     *               </p>
     */
    private void setResponseValue(String answer) {
        String[] parts = answer.split("\"value\"");
        String newParts = parts[1].substring(0, parts[1].indexOf("\""));
        Pattern p = Pattern.compile("[-+]?([0-9]*[.][0-9]+|[0-9]+)");
        Matcher m = p.matcher(newParts);
        StringBuilder answerNumeric = new StringBuilder();
        while (m.find()) {
            answerNumeric.append(m.group());
        }
        if (!answerNumeric.toString().equals("")) {
            this.value.setNextValue(answerNumeric);
        }
    }

}
