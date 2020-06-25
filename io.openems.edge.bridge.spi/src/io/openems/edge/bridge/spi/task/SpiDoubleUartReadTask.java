package io.openems.edge.bridge.spi.task;

public interface SpiDoubleUartReadTask extends SpiDoubleUartTask {
    void setResponse(byte[] response);
}
