package io.openems.edge.bridge.gpio.task;

public interface GpioBridgeWriteTask {
    int getPosition();

    boolean getRequest();

    String getDeviceId();
}
