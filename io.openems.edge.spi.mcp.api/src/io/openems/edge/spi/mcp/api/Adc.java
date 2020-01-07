package io.openems.edge.spi.mcp.api;

import io.openems.edge.spi.mcp.api.pins.Pin;

import java.util.List;

public interface Adc {
    void initialize(int spiChannel, int frequency, String circuitBoardId, String versionId);

    List<Pin> getPins();

    int getSpiChannel();

    String getCircuitBoardId();

    void deactivate();

    int hashCode();

    String getVersionId();

    boolean equals(Object o);
}
