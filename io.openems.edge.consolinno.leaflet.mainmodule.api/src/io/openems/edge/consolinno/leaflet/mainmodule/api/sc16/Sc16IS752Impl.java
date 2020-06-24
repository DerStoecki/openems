package io.openems.edge.consolinno.leaflet.mainmodule.api.sc16;

import com.pi4j.wiringpi.Spi;

public class Sc16IS752Impl implements Sc16IS752 {
    private int spiChannel;
    private int frequency;
    private String id;
    private String versionId;




    @Override
    public boolean initialize(int spiChannel, int frequency, String id, String versionId) {
        this.spiChannel = spiChannel;
        this.frequency = frequency;
        this.id = id;
        this.versionId = versionId;
        Spi.wiringPiSPISetup(spiChannel, frequency);
        return true;
    }

    @Override
    public int getSpiChannel() {
        return this.spiChannel;
    }

    @Override
    public int getFrequency() {
        return this.frequency;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getVersionId() {
        return this.versionId;
    }

    @Override
    public void deactivate(){

    }
}
