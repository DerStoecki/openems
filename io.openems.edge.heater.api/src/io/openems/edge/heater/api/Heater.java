package io.openems.edge.heater.api;

import io.openems.common.exceptions.OpenemsError;


public interface Heater {

    int calculateProvidedPower(int demand, float bufferValue) throws OpenemsError.OpenemsNamedException;

    int getMaximumThermalOutput();

    void setOffline() throws OpenemsError.OpenemsNamedException;

    int runFullPower();
}
