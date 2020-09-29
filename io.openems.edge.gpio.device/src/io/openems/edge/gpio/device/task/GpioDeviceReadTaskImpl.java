package io.openems.edge.gpio.device.task;

import io.openems.edge.bridge.gpio.task.GpioBridgeReadTask;
import io.openems.edge.common.channel.Channel;

public class GpioDeviceReadTaskImpl implements GpioBridgeReadTask {

    private boolean isInverse;
    private int gpioPinPosition;
    private Channel<Boolean> gpioChannel;
    private String deviceId;

    public GpioDeviceReadTaskImpl(String deviceId, int gpioPinPosition, Channel<Boolean> gpioChannel, boolean isInverse) {
        this.deviceId = deviceId;
        this.gpioPinPosition = gpioPinPosition;
        this.gpioChannel = gpioChannel;
        this.isInverse = isInverse;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public int getRequest() {

        return gpioPinPosition;
    }

    @Override
    public void setResponse(boolean onOff) {
        if (this.isInverse) {
            onOff = !onOff;
        }
        this.gpioChannel.setNextValue(onOff);

    }
}
