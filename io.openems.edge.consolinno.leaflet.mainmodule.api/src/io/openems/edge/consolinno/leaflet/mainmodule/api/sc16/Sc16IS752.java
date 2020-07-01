package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import org.osgi.service.cm.ConfigurationException;


public interface Sc16IS752 extends DoubleUart {


    /**
     * initializes the Sc16 with given param. and inits Connection.
     *
     * @param spiChannel Unique SPI Channel this Sc16 is communicating with.
     * @param frequency  Frequency of Clk.
     * @param id         unique Id of the Sc16 determined by Config of MainModuleId.
     * @param versionId  determines the Version of this. No concrete functionality yet. Comes in Future with more Versions.
     *
     * @throws ConfigurationException if the SPI Channel isn't available.
     */
    void initialize(int spiChannel, int frequency, String id, String versionId) throws ConfigurationException;


}
