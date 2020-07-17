package io.openems.edge.consolinno.leaflet.mainmodule.sc16.tasks;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16Task;

public abstract class AbstractUartTask implements Sc16Task {

    private int pinAddress;

    AbstractUartTask(int pinAddress) {

        this.pinAddress = pinAddress;
    }

    @Override
    public int getPin() {
        //pin Address == RW Bit + UART + Channel
        //0x0E == register Address; pinAddress to byte, and 0x00 for data; (?)
        return this.pinAddress;
    }
}
