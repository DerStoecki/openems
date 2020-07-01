package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import org.osgi.service.cm.ConfigurationException;

import java.util.Map;

public interface Sc16IS752 extends DoubleUart {

    void initialize(int spiChannel, int frequency, String id, String versionId) throws ConfigurationException;


    void removeTask(String id);


}
