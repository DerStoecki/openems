package io.openems.edge.temperature.passing.pump.api;

import io.openems.edge.temperature.passing.api.PassingForPid;

public interface Pump extends PassingForPid {

	boolean readyToChange();

	boolean changeByPercentage(double percentage);

	void controlRelais(boolean activate, String whichRelais);

}
