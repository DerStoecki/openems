package io.openems.edge.controller.passing.heatingcurveregulator;

import java.util.Map;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.heatingcurveregulator.api.HeatingCurveRegulatorChannel;
import io.openems.edge.thermometer.api.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name = "AutomaticRegulator", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HeatingCurveRegulatorImpl extends AbstractOpenemsComponent implements OpenemsComponent, HeatingCurveRegulatorChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(HeatingCurveRegulatorImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer outsideTempSensor;
    private int activationTemp;
	private int roomTemp;
	private double slope;
	private int offset;

    public HeatingCurveRegulatorImpl() {
		super(OpenemsComponent.ChannelId.values(),
				HeatingCurveRegulatorChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		activationTemp = config.activation_temp();
		roomTemp = config.room_temp();
		// Function will crash if sensor temp > room temp.
		if (activationTemp > roomTemp) {
		    activationTemp = roomTemp;
        }
        // Convert to dezidegree, since sensor data is dezidegree too.
        activationTemp = activationTemp * 10;
		slope = config.slope();
		offset = config.offset();

		this.noError().setNextValue(true);
		try {
			if (cpm.getComponent(config.temperatureSensorId()) instanceof Thermometer) {

				this.outsideTempSensor = cpm.getComponent(config.temperatureSensorId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorId(), "configured component is incorrect!");
			}
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
		}

	}


	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		if (outsideTempSensor.getTemperature().getNextValue().isDefined() && outsideTempSensor.getTemperature().getNextValue().get() <= activationTemp) {
			//function calculates everything in degree, not dezidegree!
            double function = (slope * 1.8317984 * Math.pow((roomTemp - (0.1 * outsideTempSensor.getTemperature().getNextValue().get())), 0.8281902)) + roomTemp + offset;
			//Convert back to dezidegree integer
			this.getHeatingTemperature().setNextValue((int)Math.round(function * 10));
			this.logInfo(this.log, "Thermometer measures " + 0.1 * outsideTempSensor.getTemperature().getNextValue().get() + "°C. Heater temperature calculates to " + (int)Math.round(function) + "°C.");
			//Set Error channel back to no error if there has been an error.
			if (this.noError().getNextValue().isDefined() && !this.noError().getNextValue().get()) {
				this.noError().setNextValue(true);
				this.logInfo(this.log, "Everything is fine now!");
			}
			this.isActive().setNextValue(true);
		} else {
			this.isActive().setNextValue(false);
			if (!outsideTempSensor.getTemperature().getNextValue().isDefined()) {
				this.noError().setNextValue(false);
				this.logInfo(this.log, "Not getting any data from the outside temperature sensor.");
			}

		}

	}

}
