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


@Designate( ocd=Config.class, factory=true)
@Component(name="AutomaticRegulator", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HeatingCurveRegulatorImpl extends AbstractOpenemsComponent implements OpenemsComponent, HeatingCurveRegulatorChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(HeatingCurveRegulatorImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer outsideTempSensor;
	private int roomTemp;
	private double slope;
	private double function;

	public HeatingCurveRegulatorImpl() {

		super(OpenemsComponent.ChannelId.values(),
				HeatingCurveRegulatorChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		roomTemp = config.room_temp();
		slope = config.slope();

		this.noError().setNextValue(true);
		try {
			if (cpm.getComponent(config.temperatureSensorId()) instanceof Thermometer) {

				this.outsideTempSensor = cpm.getComponent(config.temperatureSensorId());
			} else {
				throw new ConfigurationException("The configured Component is not a TemperatureSensor! Please Check "
						+ config.temperatureSensorId(), "Configured Component is incorrect!");
			}
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
		}

	}


	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		if (outsideTempSensor.getTemperature().getNextValue().isDefined()){
			//function calculates everything in degree, not dezidegree!
			function = (slope*1.8317984*Math.pow((roomTemp - (0.1*outsideTempSensor.getTemperature().getNextValue().get())), 0.8281902)) + roomTemp;
			//Convert back to dezidegree integer
			this.getHeatingTemperature().setNextValue((int)Math.round(function*10));
			this.logInfo(this.log, "Thermometer measures " + 0.1*outsideTempSensor.getTemperature().getNextValue().get() + "°C. Heater temperature calculates to " + (int)Math.round(function) + "°C.");
			if (this.noError().getNextValue().isDefined() && !this.noError().getNextValue().get()){
				this.noError().setNextValue(true);
				this.logInfo(this.log, "Everything is fine now!");
			}
		} else {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Not getting any data from the thermometer.");
		}

	}

}
