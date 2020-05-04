package io.openems.edge.bridge.i2c.task;

public interface I2cPcaReadTask extends I2cPcaTask {

    int getRequest();

    void setResponse(boolean value);

}
