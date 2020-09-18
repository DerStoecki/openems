package io.openems.edge.controller.passing.heatingcurveregulator;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.heatingcurveregulator.api.HeatingCurveRegulatorChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Consolinno weather dependent heating controller.
 * - It takes the outside temperature as input and asks for the heating to be turned on or off based on the outside
 *   temperature.
 * - If the outside temperature is below the activation threshold, a heating temperature is calculated based
 *   on a parametrized heating curve.
 *
 */

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

	// Variables for channel readout
	private boolean tempSensorSendsData;
	private int outsideTemperature;

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
		// Activation temperature can not be higher than desired room temperature, otherwise the function will crash.
		if (activationTemp > roomTemp) {
			activationTemp = roomTemp;
		}
		// Convert to dezidegree, since sensor data is dezidegree too.
		activationTemp = activationTemp * 10;
		slope = config.slope();
		offset = config.offset();

		this.noError().setNextValue(true);

		// Allocate temperature sensor.
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
	public void deactivate() {
		super.deactivate();
		turnOnHeater(false);
	}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Transfer channel data to local variables for better readability of logic code.
		tempSensorSendsData = outsideTempSensor.getTemperature().value().isDefined();
		if (tempSensorSendsData) {
			outsideTemperature = outsideTempSensor.getTemperature().value().get();

			// Error handling.
			if (this.noError().value().get() == false) {
				this.noError().setNextValue(true);
				this.logInfo(this.log, "Everything is fine now! Reading from the temperature sensor is "
						+ outsideTemperature / 10 + "°C.");
			}
		} else {
			// No data from the temperature sensor (null in channel). -> Error
			turnOnHeater(false);
			this.noError().setNextValue(false);
			this.logError(this.log, "Not getting any data from the outside temperature sensor " + outsideTempSensor.id() + ".");
		}


		// Control logic.
		if (tempSensorSendsData) {
			if (outsideTemperature <= activationTemp) {
				turnOnHeater(true);

				// Calculate heating temperature. Function calculates everything in degree, not dezidegree!
				double function = (slope * 1.8317984 * Math.pow((roomTemp - (0.1 * outsideTemperature)), 0.8281902))
						+ roomTemp + offset;

				// Convert back to dezidegree integer.
				int outputTempDezidegree = (int)Math.round(function * 10);

				setHeatingTemperature(outsideTemperature);
				this.logDebug(this.log, "Outside thermometer measures " + 0.1 * outsideTemperature
						+ "°C. Heater function calculates forward temperature to " + outputTempDezidegree / 10 + "°C.");
			} else {
				turnOnHeater(false);
			}
		}
	}

	private void turnOnHeater(boolean activate) {
		this.signalTurnOnHeater().setNextValue(activate);
	}

	private void setHeatingTemperature(int temperature) {
		this.getHeatingTemperature().setNextValue(temperature);
	}

}
