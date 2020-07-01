package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import org.osgi.service.cm.ConfigurationException;

public interface DoubleUart {

    int getSpiChannel();

    int getFrequency();

    String getId();

    String getVersionId();

    //Current Version only uses Sc16...maybe in Future different DoubleUARTs
    void addTask(String id, Sc16Task task) throws ConfigurationException;

    void removeTask(String id);

    void shift() throws ConfigurationException;


}
