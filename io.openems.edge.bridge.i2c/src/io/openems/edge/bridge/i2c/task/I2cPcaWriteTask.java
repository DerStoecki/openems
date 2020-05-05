package io.openems.edge.bridge.i2c.task;

public interface I2cPcaWriteTask extends I2cPcaTask  {

    int getPinPosition();

    boolean getRequest();


}
