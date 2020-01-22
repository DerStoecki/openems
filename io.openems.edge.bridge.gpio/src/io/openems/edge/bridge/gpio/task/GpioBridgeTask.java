package io.openems.edge.bridge.gpio.task;

public interface GpioBridgeTask {

    int getGpioPosition();

    String getDeviceId();

    int getRequest();

    void setResponse(boolean onOff);
}
