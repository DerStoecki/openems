package io.openems.edge.rest.remote.device.general.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.task.RestWriteRequest;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;

public class RestRemoteWriteTask extends AbstractRestRemoteDeviceTask implements RestWriteRequest {

    private WriteChannel<String> value;
    private WriteChannel<Boolean> allowRequest;


    public RestRemoteWriteTask(String remoteDeviceId, String slaveMasterId, boolean isMaster, String realDeviceId,
                               String deviceChannel, boolean autoAdapt, WriteChannel<String> value, String deviceType,
                               WriteChannel<Boolean> allowRequest) {
        super(remoteDeviceId, slaveMasterId, isMaster, realDeviceId, deviceChannel, autoAdapt, deviceType);

        this.value = value;
        this.allowRequest = allowRequest;
    }


    @Override
    public String getPostMessage() {
        if (readyToWrite()) {

            if (super.isAutoAdapt() && this.value.getNextValue().isDefined()) {
                if (super.isInverse) {
                    if (this.value.getNextValue().get().toLowerCase().equals("true")) {
                        return super.getRealDeviceId() + "/" + "false";
                    } else if (this.value.getNextValue().get().toLowerCase().equals("false")) {
                        return super.getRealDeviceId() + "/" + "true";
                    }
                } else {
                    return super.getRealDeviceId() + "/" + this.value.getNextValue().get();
                }
            }
        }

        return "NotReadyToWrite";

    }

    @Override
    public void wasSuccess(Boolean succ, String response) {
        if (succ) {
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
    public void nextValueSet() {

        if(this.allowRequest.getNextWriteValue().isPresent()) {
            this.allowRequest.setNextValue(this.allowRequest.getNextWriteValueAndReset());
        }
        if(this.value.getNextWriteValue().isPresent()){
            this.value.setNextValue(this.value.getNextWriteValueAndReset());
        }

    }
}
