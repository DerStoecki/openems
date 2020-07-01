package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

public interface Sc16WriteTask extends Sc16Task {
    /**
     * Get the Value of the OnOff Channel of Nature: DoubleUartDevice .
     *
     * @return boolean true or false depending on value.
     */

    boolean getRequest();
}
