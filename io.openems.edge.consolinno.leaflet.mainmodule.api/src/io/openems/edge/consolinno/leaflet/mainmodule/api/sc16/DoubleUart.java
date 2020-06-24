package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

public interface DoubleUart {

    int getSpiChannel();

    int getFrequency();

    String getId();

    String getVersionId();

    void deactivate();
}
