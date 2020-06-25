package io.openems.edge.bridge.spi.task;

public interface SpiDoubleUartTask {
    int getSpiChannel();

    byte[] getPinAddressAsByte();

    void update();

    String getId();
}
