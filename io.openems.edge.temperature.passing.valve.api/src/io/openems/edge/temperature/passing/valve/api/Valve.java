package io.openems.edge.temperature.passing.valve.api;

import io.openems.edge.temperature.passing.api.PassingForPid;

public interface Valve extends PassingForPid {

    boolean readyToChange();

    boolean changeByPercentage(double percentage);

    void controlRelais(boolean activate, String whichRelais);

}
