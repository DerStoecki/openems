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

	private Thermometer watertankTempSensorUpper;
	private Thermometer watertankTempSensorLower;
	private int minTempUpper;
	private int maxTempLower;
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

		minTempUpper = config.min_temp_upper() * 10;	// Convert to dezidegree.
		maxTempLower = config.max_temp_lower() * 10;
		responseTimeout = config.response_timeout();
		this.heatTankRequest().setNextValue(false);
		this.needHotWater().setNextValue(false);
		this.remoteHotWaterSignal().setNextValue(false);
		remoteSignal = false;

		this.noError().setNextValue(true);
		try {
			if (cpm.getComponent(config.temperatureSensorUpperId()) instanceof Thermometer) {
				this.watertankTempSensorUpper = cpm.getComponent(config.temperatureSensorUpperId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorUpperId(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.temperatureSensorLowerId()) instanceof Thermometer) {
				this.watertankTempSensorLower = cpm.getComponent(config.temperatureSensorLowerId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorLowerId(), "configured component is incorrect!");
			}
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
		}

	}


	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {


		// Sensor checks.
		if (watertankTempSensorUpper.getTemperature().value().isDefined()) {
			if (watertankTempSensorLower.getTemperature().value().isDefined()) {

				// Remote changes from false to true
				if (this.remoteHotWaterSignal().value().isDefined() && this.remoteHotWaterSignal().value().get()
						&& !remoteSignal) {
					this.heatTankRequest().setNextValue(true);
					remoteSignal = true;
				}

				// Remote changes from true to false
				if (this.remoteHotWaterSignal().value().isDefined() && !this.remoteHotWaterSignal().value().get()
						&& remoteSignal) {
					this.heatTankRequest().setNextValue(false);
					remoteSignal = false;
				}

				// Check lower temperature limit. Use heatTankRequest().getNextValue() here, to override remote if needed.
				if (!this.heatTankRequest().getNextValue().get()) {
					if (watertankTempSensorUpper.getTemperature().value().get() < minTempUpper) {
						timestamp = LocalDateTime.now();
						this.heatTankRequest().setNextValue(true);
					}
				}

				// Stop when hot enough
				if (watertankTempSensorLower.getTemperature().value().get() > maxTempLower) {
					this.heatTankRequest().setNextValue(false);
					this.needHotWater().setNextValue(false);
				} else {
					if (this.heatTankRequest().value().get()) {
						// Wait for remote signal or execute after timeout.
						if ((this.remoteHotWaterSignal().value().isDefined() && this.remoteHotWaterSignal().value().get())
								|| ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now()) >= responseTimeout) {
							if (!(this.remoteHotWaterSignal().value().isDefined() && this.remoteHotWaterSignal().value().get())
									&& !this.needHotWater().value().get()) {
								this.logInfo(this.log, "Warning: remote response timeout.");
							}
							this.needHotWater().setNextValue(true);
						}
					} else {
						this.needHotWater().setNextValue(false);
					}
				}


				if (!this.noError().value().get()) {
					this.noError().setNextValue(true);
					this.logInfo(this.log, "Temperature sensors are fine now!");
				}
			} else {
				this.noError().setNextValue(false);
				this.logInfo(this.log, "Not getting any data from the water tank upper temperature sensor " + watertankTempSensorUpper.id() + ".");
			}
		} else {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Not getting any data from the water tank lower temperature sensor " + watertankTempSensorLower.id() + ".");
		}
	}

}