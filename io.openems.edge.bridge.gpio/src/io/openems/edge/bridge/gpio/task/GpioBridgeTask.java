package io.openems.edge.bridge.gpio.task;

public interface GpioBridgeTask {

    String getDeviceId();

    int getRequest();

    void setResponse(boolean onOff);
}
