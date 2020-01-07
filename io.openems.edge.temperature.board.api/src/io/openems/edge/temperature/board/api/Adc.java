package io.openems.edge.temperature.board.api;

import io.openems.edge.temperature.board.api.pins.Pin;

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
