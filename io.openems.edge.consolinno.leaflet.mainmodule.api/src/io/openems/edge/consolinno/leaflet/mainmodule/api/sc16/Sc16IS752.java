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
     * @param innteruptSet tells if interrupt will be configured or not; usually comes from Config.
     * @param interruptType declares which Interrupt Type will be set, usually comes from Config.
     * @param dataForType saved InterruptSetupBits as String.
     * @throws ConfigurationException if the SPI Channel isn't available.
     * @throws IllegalArgumentException if dataForType is wrong.
     */
    void initialize(int spiChannel, int frequency, String id, String versionId, boolean innteruptSet, String interruptType, String dataForType) throws ConfigurationException, IllegalArgumentException;


}
