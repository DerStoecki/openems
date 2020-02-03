package io.openems.edge.manager.valve.api;

import io.openems.edge.temperature.passing.valve.api.Valve;

public interface ManagerValve {

	void addValve(String id, Valve valve);

	void removeValve(String id);

}
