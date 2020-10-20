package io.openems.edge.gpio.device.task;

import io.openems.edge.bridge.gpio.task.GpioBridgeWriteTask;
import io.openems.edge.common.channel.WriteChannel;

public class GpioDeviceWriteTaskImpl implements GpioBridgeWriteTask {

    private String id;
    private int pinPosition;
    private WriteChannel<Boolean> writeError;

    public GpioDeviceWriteTaskImpl(String id, int pinPosition, WriteChannel<Boolean> writeError) {
        this.id = id;
        this.pinPosition = pinPosition;
        this.writeError = writeError;
    }

    @Override
    public int getPosition() {
        return this.pinPosition;
    }

    @Override
    public boolean getRequest() {

        if (this.writeError.getNextWriteValue().isPresent()) {
            return this.writeError.getNextWriteValue().get();
        }
        return false;
    }

    @Override
    public String getDeviceId() {
        return this.id;
    }
}
