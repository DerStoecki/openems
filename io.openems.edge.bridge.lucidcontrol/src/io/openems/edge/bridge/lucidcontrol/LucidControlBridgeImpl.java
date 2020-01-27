package io.openems.edge.bridge.lucidcontrol;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.lucidcontrol.api.LucidControlBridge;
import io.openems.edge.bridge.lucidcontrol.task.LucidControlBridgeTask;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
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
@Component(name = "GpioBridge",
		immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class LucidControlBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, EventHandler, LucidControlBridge {


	//String Key : Module Id, String Value  = Address
	private Map<String, String> addressMap = new ConcurrentHashMap<>();
	//String Key: Module Id; Integer Value : Voltage of Module
	private Map<String, Integer> voltageMap = new ConcurrentHashMap<>();

	private Map<String, LucidControlBridgeTask> tasks = new ConcurrentHashMap<>();

	private LucidControlWorker worker = new LucidControlWorker();

	public LucidControlBridgeImpl() {
		super(OpenemsComponent.ChannelId.values());
	}


	@Activate
	public void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		if (config.enabled()) {
			this.worker.activate(super.id());
		}
	}


	@Deactivate
	public void deactivate() {
		super.deactivate();
		this.worker.deactivate();
	}

	@Override
	public void addAddress(String id, String path) {
			this.addressMap.put(id, path);
	}

	@Override
	public void addVoltage(String id, int voltage) {
		this.voltageMap.put(id, voltage);
	}

	@Override
	public void removeModule(String id) {

		//this.tasks.forEach(
				//TODO get each tasks module id and remove etc.
		//		 );

	}

	@Override
	public void removeTask(String id) {

	}

	private class LucidControlWorker extends AbstractCycleWorker {
		@Override
		public void activate(String id) {
			super.activate(id);
		}

		@Override
		public void deactivate() {
			super.deactivate();
		}

		@Override
		protected void forever() throws Throwable {
			//TODO DO SOMETHING --> Read data from file of task and setResponse
		}


	}

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
			this.worker.triggerNextRun();
		}
	}

}
