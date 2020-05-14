package io.openems.edge.rest.remote.device.general.task;

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
    }


    @Override
    public String getPostMessage() {
        /*
        * If is Autoadapt && isInverse --> ! value else set
        *
        *
        * */

        if(super.isAutoAdapt() && super.isInverse){


        }

    }

    @Override
    public void wasSuccess(Boolean succ, String response) {

    }

    @Override
    public boolean readyToWrite() {
        return false;
    }

    @Override
    public void setReadyToWrite(boolean ready) {

    }
}
