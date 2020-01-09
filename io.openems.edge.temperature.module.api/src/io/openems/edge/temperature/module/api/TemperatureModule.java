package io.openems.edge.temperature.module.api;

import io.openems.edge.spi.mcp.api.Adc;

import java.util.Set;

public interface TemperatureModule {

    String getCircuitBoardId();

    String getVersionId();

    Set<Adc> getAdcSet();
}
