package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

public interface Sc16IS752 extends DoubleUart {

    boolean initialize(int spiChannel, int frequency, String id, String versionId);


}
