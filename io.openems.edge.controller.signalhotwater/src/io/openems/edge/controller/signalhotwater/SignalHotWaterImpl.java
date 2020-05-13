package io.openems.edge.controller.signalhotwater;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.signalhotwater.api.SignalHotWaterChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Designate(ocd = Config.class, factory = true)
@Component(name = "SignalHotWater", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SignalHotWaterImpl extends AbstractOpenemsComponent implements OpenemsComponent, SignalHotWaterChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(SignalHotWaterImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer watertankTempSensor;
	private int minTemp;
	private int responseTimeout;
	private LocalDateTime timestamp;
	private boolean remoteSignal;

	public SignalHotWaterImpl() {
		super(OpenemsComponent.ChannelId.values(),
				SignalHotWaterChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		minTemp = config.min_temp() * 10;	// Convert to dezidegree.
		responseTimeout = config.response_timeout();
		this.temperatureLow().setNextValue(false);
		this.needHotWater().setNextValue(false);
		this.remoteHotWaterSignal().setNextWriteValue(false);
		remoteSignal = false;

		this.noError().setNextValue(true);
		try {
			if (cpm.getComponent(config.temperatureSensorId()) instanceof Thermometer) {
				this.watertankTempSensor = cpm.getComponent(config.temperatureSensorId());
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

		// Check if water tank temperature is low, start call and timer if yes.
		if (watertankTempSensor.getTemperature().getNextValue().isDefined()) {
			if (watertankTempSensor.getTemperature().getNextValue().get() < minTemp) {
				if (!this.temperatureLow().getNextValue().get() && !this.remoteHotWaterSignal().getNextWriteValue().get()) {
					timestamp = LocalDateTime.now();
				}
				this.temperatureLow().setNextValue(true);
			} else {
				this.temperatureLow().setNextValue(false);
			}
			if (this.noError().getNextValue().isDefined() && !this.noError().getNextValue().get()) {
				this.noError().setNextValue(true);
				this.logInfo(this.log, "Everything is fine now!");
			}
		} else {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Not getting any data from the water tank temperature sensor.");
		}

		// Wait for remote signal or execute after timeout. Also pass through of remote signal independent of water tank temp.
		if ((this.remoteHotWaterSignal().getNextWriteValue().isPresent() && this.remoteHotWaterSignal().getNextWriteValue().get())
				|| (this.temperatureLow().getNextValue().get() && ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now()) >= responseTimeout)) {
			if (!(this.remoteHotWaterSignal().getNextWriteValue().isPresent() && this.remoteHotWaterSignal().getNextWriteValue().get())
					&& !this.needHotWater().getNextValue().get()) {
				this.logInfo(this.log, "Warning: remote response timeout.");
			}
			if (this.remoteHotWaterSignal().getNextWriteValue().get()) {
				remoteSignal = true;
			}
			this.needHotWater().setNextValue(true);
		} else {
			this.needHotWater().setNextValue(false);
		}

		// Reset timer when remote signal changes from true to false.
		if (!(this.remoteHotWaterSignal().getNextWriteValue().isPresent() && this.remoteHotWaterSignal().getNextWriteValue().get()) && remoteSignal) {
			timestamp = LocalDateTime.now();
			remoteSignal = false;
		}


	}

}