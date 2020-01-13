package io.openems.edge.bridge.spi.task;


public abstract class AbstractSpiTask implements SpiTask {
    private final int spiChannel;

    public AbstractSpiTask(int spiChannel) {
        this.spiChannel = spiChannel;
    }

    @Override
    public int getSpiChannel() {
        return spiChannel;
    }

}