package io.openems.edge.consolinno.leaflet.maindevice.pca.task;

import io.openems.edge.bridge.i2c.task.AbstractPcaTask;
import io.openems.edge.bridge.i2c.task.I2cPcaWriteTask;
import io.openems.edge.common.channel.WriteChannel;


public class PcaDeviceWriteTask extends AbstractPcaTask implements I2cPcaWriteTask {
    private int pinPosition;
    private WriteChannel<Boolean> onOff;


    public PcaDeviceWriteTask(String pcaModuleId, String deviceId, int pinPosition, WriteChannel<Boolean> onOff) {
        super(pcaModuleId, deviceId);
        this.pinPosition = pinPosition;
        this.onOff = onOff;
    }


    @Override
    public int getPinPosition() {
        return pinPosition;
    }

    @Override
    public boolean getRequest() {
        if (this.onOff.getNextWriteValue().isPresent()) {
            this.onOff.setNextValue(this.onOff.getNextWriteValue().get());
            return this.onOff.getNextWriteValue().get();
        }
        return false;
    }

}
