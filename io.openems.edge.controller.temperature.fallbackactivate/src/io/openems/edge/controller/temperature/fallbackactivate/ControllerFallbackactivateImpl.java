package io.openems.edge.controller.temperature.fallbackactivate;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "temperature.controller.fallbackactivate")
public class ControllerFallbackactivateImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

	@Reference
	protected ComponentManager cpm;

	private Thermometer TempSensor;

	public ControllerFallbackactivateImpl() {

		super(OpenemsComponent.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		try {
			allocate_Component(config.primary_Temp_Sensor(), "Thermometer");
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
			throw e;
		}

	}


	@Deactivate
	public void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {


	}


	private void allocate_Component(String id, String type) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		if (cpm.getComponent(id) instanceof Thermometer) {
			TempSensor = cpm.getComponent(id);
		} else {
			throw new ConfigurationException(id, "The temperature-sensor " + id + " Is not a (configured) temperature sensor.");
		}

	}

}
