package io.openems.edge.bridge.i2c.task;

public interface I2cPcaTask {

    int getPinPosition();

    boolean isInverse();

    String getPcaModuleId();

    String getDeviceId();

}
