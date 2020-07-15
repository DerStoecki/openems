package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

public interface Sc16ReadTask extends Sc16Task {

    /**
     * Sc16 Response for this tasks.
     *
     * @param response either 0 or 1 depending on GPIO State for this Task.
     */

    void setResponse(int response);
}
