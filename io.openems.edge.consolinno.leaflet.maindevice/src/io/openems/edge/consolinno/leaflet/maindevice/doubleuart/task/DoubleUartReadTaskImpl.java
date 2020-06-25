package io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.spi.task.SpiDoubleUartReadTask;
import io.openems.edge.common.channel.WriteChannel;

import java.nio.ByteBuffer;

public class DoubleUartReadTaskImpl extends AbstractUartTask implements SpiDoubleUartReadTask {


    public DoubleUartReadTaskImpl(String id, int spiChannel, int pinAddress, WriteChannel<Boolean> onOff) {
        super(spiChannel, pinAddress, onOff, id);
    }

    @Override
    public void setResponse(byte[] response) {
        int digit = (response[1] << 8) + (response[2] & 0xFF);
        digit &= 0xFFF;
        try {
            if (digit != 0) {
                super.onOff.setNextWriteValue(true);
            } else {
                super.onOff.setNextWriteValue(false);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
