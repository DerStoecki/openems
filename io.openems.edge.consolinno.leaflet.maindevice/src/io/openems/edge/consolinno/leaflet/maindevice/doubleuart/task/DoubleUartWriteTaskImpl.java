package io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task;

import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16WriteTask;
import io.openems.edge.common.channel.WriteChannel;

public class DoubleUartWriteTaskImpl extends AbstractUartTask implements Sc16WriteTask {

    public DoubleUartWriteTaskImpl(String id, int spiChannel, byte pinAddress, WriteChannel<Boolean> onOff) {
        super(spiChannel, pinAddress, onOff, id);

    }

    @Override
    public boolean getRequest() {
        if (super.onOff.value().isDefined()) {
            return super.onOff.value().get();
        } else {
            return false;
        }
    }
}
