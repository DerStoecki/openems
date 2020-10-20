package io.openems.edge.consolinno.leaflet.mainmodule.sc16.tasks;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16WriteTask;

public class DoubleUartWriteTaskImpl extends AbstractUartTask implements Sc16WriteTask {

    private WriteChannel<Boolean> status;

    public DoubleUartWriteTaskImpl(int pinAddress, WriteChannel<Boolean> onOff) {
        super(pinAddress);
        this.status = onOff;
    }

    @Override
    public boolean getRequest() {
        if (this.status.value().isDefined()) {
            return this.status.value().get();
        } else {
            return false;
        }
    }
}
