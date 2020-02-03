package io.openems.edge.manager.valve;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.manager.valve.api.ManagerValve;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Designate(ocd = Config.class, factory = true)
@Component(name = "ConsolinnoManagerValve",
		immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_CONTROLLERS)
public class ManagerValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, EventHandler, ManagerValve {

	private Map<String, Valve> valves = new ConcurrentHashMap<>();
	private ManagerValveWorker worker = new ManagerValveWorker();

	public ManagerValveImpl() {
		super(OpenemsComponent.ChannelId.values());
	}


	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		worker.activate(config.id());
	}

	@Deactivate
	public void deactivate() {
		super.deactivate();
	}

	@Override
	public void addValve(String id, Valve valve) {
		this.valves.put(id, valve);

	}

	@Override
	public void removeValve(String id) {
		this.valves.remove(id);
	}


	private class ManagerValveWorker extends AbstractCycleWorker {
		@Override
		public void activate(String name) {
			super.activate(name);
		}

		@Override
		public void deactivate() {
			super.deactivate();
		}

		@Override
		protected void forever() throws Throwable {
			//just set off so position is fix
			valves.values().forEach(valve -> {
				if (valve.readyToChange()) {
					valve.controlRelays(false, "Closed");
					valve.controlRelays(false, "Opened");
				}
			});
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_CONTROLLERS)) {
			this.worker.triggerNextRun();
		}


	}
}
