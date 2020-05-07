package io.openems.edge.bridge.gpio.task;

public interface GpioBridgeReadTask {

    String getDeviceId();

    int getRequest();

    void setResponse(boolean onOff);
}
