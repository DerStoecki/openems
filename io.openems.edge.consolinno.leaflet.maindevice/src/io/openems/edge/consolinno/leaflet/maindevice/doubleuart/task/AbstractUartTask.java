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

    /*       long output = pinAddress;
            byte[] data = {0, 0, 0};
            for (int i = 0; i < 3; i++) {
                data[2 - i] = (byte) (output % 256);
                output = output >> 8;
            }
           return data;
      */
        //0x41 == read; pinAddress to byte, and 0x00 for data; (?)
        return new byte[]{
                0x41, (byte) pinAddress, 0x00
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
