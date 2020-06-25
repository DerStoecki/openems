package io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task;

import io.openems.edge.bridge.spi.task.SpiDoubleUartWriteTask;
import io.openems.edge.common.channel.WriteChannel;

public class DoubleUartWriteTaskImpl extends AbstractUartTask implements SpiDoubleUartWriteTask {

    public DoubleUartWriteTaskImpl(String id, int spiChannel, int pinAddress, WriteChannel<Boolean> onOff) {
        super(spiChannel, pinAddress, onOff, id);

    }

    @Override
    public byte[] getRequest() {
        // May be correct ? 0x40 == Write address; register address might be pin Address and then the value...but idk
        return new byte[]{0x40, (byte) pinAddress, (byte) (onOff.getNextValue().get() ? 1 : 0)};
    }

}
