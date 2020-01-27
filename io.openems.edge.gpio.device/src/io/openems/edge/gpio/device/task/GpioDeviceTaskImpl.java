package io.openems.edge.gpio.device.task;

import io.openems.edge.bridge.gpio.task.AbstractGpioBridgeTask;
import io.openems.edge.bridge.gpio.task.GpioBridgeTask;
import io.openems.edge.common.channel.Channel;

public class GpioDeviceTaskImpl extends AbstractGpioBridgeTask implements GpioBridgeTask {

    private int gpioPinPosition;
    private Channel<Boolean> gpioChannel;

    public GpioDeviceTaskImpl(String deviceId, int gpioPinPosition, Channel<Boolean> gpioChannel) {
        super(deviceId);
        this.gpioPinPosition = gpioPinPosition;
        this.gpioChannel = gpioChannel;
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
