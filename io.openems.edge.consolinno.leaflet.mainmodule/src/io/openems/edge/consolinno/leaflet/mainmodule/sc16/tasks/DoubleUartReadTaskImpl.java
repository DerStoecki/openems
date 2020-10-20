package io.openems.edge.consolinno.leaflet.mainmodule.sc16.tasks;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16ReadTask;

public class DoubleUartReadTaskImpl extends AbstractUartTask implements Sc16ReadTask {

    private Channel<Boolean> status;

    public DoubleUartReadTaskImpl( int pinAddress, Channel<Boolean> onOff) {
        super(pinAddress);
        this.status = onOff;
    }

    @Override
    public void setResponse(int response) {
        //INVERSE LOGIC!
        if (response == 0) {
            this.status.setNextValue(true);
        } else {
            this.status.setNextValue(false);
        }
    }
}
