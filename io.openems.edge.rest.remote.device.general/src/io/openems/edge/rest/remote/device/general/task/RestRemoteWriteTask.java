package io.openems.edge.rest.remote.device.general.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.task.RestWriteRequest;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;

public class RestRemoteWriteTask extends AbstractRestRemoteDeviceTask implements RestWriteRequest {

    private WriteChannel<String> value;
    private WriteChannel<Boolean> allowRequest;
    private String lastValue = "Nothing set";
    private boolean hasBeenSet = false;


    public RestRemoteWriteTask(String remoteDeviceId,String realDeviceId,
                               String deviceChannel, boolean autoAdapt, WriteChannel<String> value, String deviceType,
                               WriteChannel<Boolean> allowRequest) {
        super(remoteDeviceId, realDeviceId, deviceChannel, autoAdapt, deviceType);

        this.value = value;
        this.allowRequest = allowRequest;
    }


    @Override
    public String getPostMessage() {
        if (readyToWrite()) {

            if (this.value.getNextValue().isDefined()) {
                String msg = "{\"value\":";
                if (super.isAutoAdapt()) {
                    if (super.isInverse) {
                        if (this.value.getNextValue().get().toLowerCase().equals("true")) {
                            msg += "false}";
                        } else if (this.value.getNextValue().get().toLowerCase().equals("false")) {
                            msg += "true}";
                        }
                    } else {
                        msg += this.value.getNextValue().get() + "}";
                    }
                } else {
                    msg += this.value.getNextValue().get() + "}";
                }
                return msg;

            }
            System.out.println("NoValueDefined");
            return "NoValueDefined";


        }
        System.out.println("NotReadyToWrite");
        return "NotReadyToWrite";
    }

    @Override
    public void wasSuccess(Boolean succ, String response) {
        if (succ) {
            hasBeenSet = true;
            System.out.println("Was successfully set to " + response);
        } else {
            System.out.println("Error while Posting Value, please try again!");
        }

    }

    @Override
    public boolean readyToWrite() {
        return this.allowRequest.getNextValue().get();
    }

    @Override
    public boolean setReadyToWrite(boolean ready) {
        try {
            this.allowRequest.setNextWriteValue(ready);
            return true;
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean valueHasChanged() {
        if (this.value.getNextValue().isDefined()) {
            if (this.lastValue.equals(this.value.getNextValue().get()) && hasBeenSet) {
                return false;
            } else {
                this.lastValue = this.value.getNextValue().get();
                hasBeenSet = false;
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void nextValueSet() {

        if (this.allowRequest.getNextWriteValue().isPresent()) {
            this.allowRequest.setNextValue(this.allowRequest.getNextWriteValueAndReset());
        }
        if (this.value.getNextWriteValue().isPresent()) {
            this.value.setNextValue(this.value.getNextWriteValueAndReset());
        }

    }
}
