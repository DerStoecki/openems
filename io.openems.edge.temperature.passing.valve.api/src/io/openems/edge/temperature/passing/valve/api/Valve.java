package io.openems.edge.temperature.passing.valve.api;

import io.openems.edge.temperature.passing.api.PassingForPid;

public interface Valve extends PassingForPid {

    boolean readyToChange();

    boolean changeByPercentage(double percentage);

    void controlRelays(boolean activate, String whichRelays);

}
