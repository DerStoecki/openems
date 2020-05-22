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
    private boolean unitWasSet;
    private Channel<String> unit;

    AbstractRestRemoteDeviceTask(String remoteDeviceId, String realDeviceId, String deviceChannel,
                                 boolean autoAdapt, String deviceType, Channel<String> unit) {
        this.remoteDeviceId = remoteDeviceId;

        this.deviceChannel = deviceChannel;
        if (deviceType.toLowerCase().equals("relays")) {
            this.autoAdapt = autoAdapt;
        } else {
            this.autoAdapt = false;
        }
        this.unit = unit;
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

    /**
     * Returns String for AutoAdaptRequest, if the Device is "Relays" Type.
     *
     * @return String IsCloser yes or no If Yes --> no Inverse Logic
     */
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

    /**
     * sets IsInverse depending if the relays is a closer or not.
     *
     * @param succ   success of the REST GET Request.
     * @param answer 1 or 0 for Relays --> IsCloser.
     *               <p> If Relays is not a Closer ---> answer == 0; Inverse logic is true.</p>
     */
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

    /**
     * Sets the Unit for a Read or Write Task.
     *
     * @param succ   Success of the REST GET Request for Unit.
     * @param answer complete GET String. Will be Split at "Unit".
     */
    @Override
    public void setUnit(boolean succ, String answer) {
        if (succ && !this.unitWasSet) {
            if (answer.contains("Unit")) {
                String[] parts = answer.split("\"Unit\"");
                if (parts[1].contains("\"")) {

                    String newParts = parts[1].substring(parts[1].indexOf("\""), parts[1].indexOf("\"", parts[1].indexOf("\"") + 1));
                    newParts = newParts.replace("\"", "");
                    this.unit.setNextValue(newParts);
                    this.unitWasSet = true;
                }
            } else {
                this.unit.setNextValue("");
                this.unitWasSet = true;
            }
        }
    }

    @Override
    public boolean unitWasSet() {
        return this.unitWasSet;
    }
}
