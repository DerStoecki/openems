package io.openems.edge.consolinno.leaflet.maindevice.pca.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.i2c.task.AbstractPcaTask;
import io.openems.edge.bridge.i2c.task.I2cPcaReadTask;
import io.openems.edge.common.channel.WriteChannel;


public class PcaDeviceReadTask extends AbstractPcaTask implements I2cPcaReadTask {
    private int pinPosition;
    private WriteChannel<Boolean> onOff;


    public PcaDeviceReadTask(String pcaModuleId, String deviceId, int pinPosition, WriteChannel<Boolean> onOff) {
        super(pcaModuleId, deviceId);
        this.pinPosition = pinPosition;
        this.onOff = onOff;
    }


    @Override
    public int getRequest() {
        return this.pinPosition;
    }

    @Override
    public void setResponse(boolean value) {
        this.onOff.setNextValue(value);
        try {
            this.onOff.setNextWriteValue(value);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
