package io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task;

import io.openems.edge.bridge.spi.task.SpiDoubleUartTask;
import io.openems.edge.common.channel.WriteChannel;

public abstract class AbstractUartTask implements SpiDoubleUartTask {

    int pinAddress;
    private int spiChannel;
    private String id;
    WriteChannel<Boolean> onOff;

    AbstractUartTask(int spiChannel, int pinAddress, WriteChannel<Boolean> onOff, String id) {

        this.spiChannel = spiChannel;
        this.pinAddress = pinAddress;
        this.onOff = onOff;
        this.id = id;

    }

    @Override
    public byte[] getPinAddressAsByte() {
        //pin Address == RW Bit + UART + Channel
        //0x0E == register Address; pinAddress to byte, and 0x00 for data; (?)
        return new byte[]{
                0x0E, (byte) pinAddress, 0x00
        };
    }


    @Override
    public int getSpiChannel() {
        return spiChannel;
    }

    @Override
    public void update() {
        if (this.onOff.getNextWriteValue().isPresent()) {
            this.onOff.setNextValue(this.onOff.getNextWriteValueAndReset());
        }
    }

    @Override
    public String getId() {
        return this.id;
    }
}
