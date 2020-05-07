package io.openems.edge.gpio.device.task;

import io.openems.edge.bridge.gpio.task.GpioBridgeReadTask;
import io.openems.edge.common.channel.Channel;

public class GpioDeviceReadTaskImpl implements GpioBridgeReadTask {

    private int gpioPinPosition;
    private Channel<Boolean> gpioChannel;
    private String deviceId;

    public GpioDeviceReadTaskImpl(String deviceId, int gpioPinPosition, Channel<Boolean> gpioChannel) {
       this.deviceId = deviceId;
        this.gpioPinPosition = gpioPinPosition;
        this.gpioChannel = gpioChannel;
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

        this.gpioChannel.setNextValue(onOff);

    }
}
