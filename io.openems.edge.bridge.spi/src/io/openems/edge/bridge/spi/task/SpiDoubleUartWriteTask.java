package io.openems.edge.bridge.spi.task;

public interface SpiDoubleUartWriteTask extends SpiDoubleUartTask {
    byte[] getRequest();

}
