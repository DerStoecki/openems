package io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task;

import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16ReadTask;
import io.openems.edge.common.channel.WriteChannel;

public class DoubleUartReadTaskImpl extends AbstractUartTask implements Sc16ReadTask {


    public DoubleUartReadTaskImpl(String id, int spiChannel, int pinAddress, WriteChannel<Boolean> onOff) {
        super(spiChannel, pinAddress, onOff, id);
    }

    @Override
    public void setResponse(int response) {
        //INVERSE LOGIC!
        if (response == 0) {
                super.onOff.setNextValue(true);
            } else {
                super.onOff.setNextValue(false);
            }
    }
}
