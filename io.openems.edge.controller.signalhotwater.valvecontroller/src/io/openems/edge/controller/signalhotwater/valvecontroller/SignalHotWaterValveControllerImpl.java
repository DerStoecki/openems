package io.openems.edge.controller.signalhotwater.valvecontroller;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.valvepumpcontrol.api.ValvePumpControlChannel;
import io.openems.edge.controller.signalhotwater.api.SignalHotWaterChannel;
import io.openems.edge.controller.signalhotwater.valvecontroller.api.SignalHotWaterValvecontrollerChannel;
import io.openems.edge.temperature.passing.valve.api.Valve;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This controller receives the "needHotWater" signal from the "SignalHotWater" controller. This controller is
 * responsible for switching the right valves in the right order to transfer heat to the water tank the
 * "SignalHotWater" controller is monitoring.
 *
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "SignalHotWater.Valvecontroller", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SignalHotWaterValveControllerImpl extends AbstractOpenemsComponent implements OpenemsComponent, SignalHotWaterValvecontrollerChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(SignalHotWaterValveControllerImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer waermetauscherVorlauf;
	private SignalHotWaterChannel signalHotWaterChannel;
	private ValvePumpControlChannel valvePumpControlChannel;
	private Valve valveTL01;
	private int minTempVorlauf;
	private int stepcounter;
	private LocalDateTime timestamp;
	private int timeoutMinutes;
	private int temperatureControl;
	private boolean valveError;

	// Variables for channel readout
	private boolean temperatureSensorSendsData;
	private boolean needHotWater;
	private int heatNetworkTemp;
	private int waterTankTemperature;



	public SignalHotWaterValveControllerImpl() {
		super(OpenemsComponent.ChannelId.values(),
				Controller.ChannelId.values(),
				SignalHotWaterValvecontrollerChannel.ChannelId.values()
		);
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		minTempVorlauf = config.min_temp_vl() * 10;	// Convert to dezidegree.
		stepcounter = 0;
		this.noError().setNextValue(true);
		timeoutMinutes = config.timeout();
		valveError = false;
		temperatureSensorSendsData = true;

		// Allocate components
		try {
			if (cpm.getComponent(config.temperatureSensorVlId()) instanceof Thermometer) {
				this.waermetauscherVorlauf = cpm.getComponent(config.temperatureSensorVlId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorVlId(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.signalHotWaterId()) instanceof SignalHotWaterChannel) {
				this.signalHotWaterChannel = cpm.getComponent(config.signalHotWaterId());
			} else {
				throw new ConfigurationException("The configured component is not a SignalHotWater controller! Please check "
						+ config.signalHotWaterId(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.valveUS01overrideId()) instanceof ValvePumpControlChannel) {
				this.valvePumpControlChannel = cpm.getComponent(config.valveUS01overrideId());
			} else {
				throw new ConfigurationException("The configured component is not a valve override controller! Please check "
						+ config.valveUS01overrideId(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.valveTL01Id()) instanceof Valve) {
				this.valveTL01 = cpm.getComponent(config.valveTL01Id());
			} else {
				throw new ConfigurationException("The configured component is not a valve! Please check "
						+ config.valveTL01Id(), "configured component is incorrect!");
			}
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
		}

	}


	@Deactivate
	public void deactivate() {super.deactivate();}


	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Sensor checks and error handling.
		if (waermetauscherVorlauf.getTemperature().value().isDefined()) {
			if (signalHotWaterChannel.waterTankTemp().value().isDefined()) {
				heatNetworkTemp = waermetauscherVorlauf.getTemperature().value().get();
				waterTankTemperature = signalHotWaterChannel.waterTankTemp().value().get();
				if (temperatureSensorSendsData == false) {
					this.logDebug(this.log, "Temperature sensors are fine now!");
				}
				temperatureSensorSendsData = true;
				if (valveError == false) {
					this.noError().setNextValue(true);
				}
			} else {
				temperatureSensorSendsData = false;
				this.noError().setNextValue(false);
				this.logError(this.log, "Error, not getting a temperature signal from the water tank!");
			}
		} else {
			temperatureSensorSendsData = false;
			this.noError().setNextValue(false);
			this.logError(this.log, "Not getting any data from temperature sensor " + waermetauscherVorlauf.id() + ".");
		}


		// Transfer channel data to local variables for better readability of logic code.
		needHotWater = signalHotWaterChannel.needHotWater().value().isDefined() && signalHotWaterChannel.needHotWater().value().get();


		// Control logic.
		if (temperatureSensorSendsData) {
			if (needHotWater) {

				// Heating process is executed in steps. Stepcounter tracks progress, start is at stepcounter == 0.
				if (stepcounter == 0) {
					// When override is active, valveUS01 can't be changed by other controllers.
					valveUS01Override(true);
					controlValveUS01(true);

					stepcounter = 1;

					// Timestamp and temperature saved to check if valve/heat source is functional.
					timestamp = LocalDateTime.now();
					temperatureControl = heatNetworkTemp;
					this.logInfo(this.log, "NeedHotWater signal received. Waiting for heat source to reach temperature.");
				}


				if (stepcounter == 1) {
				    // "If = true" is the regular code, "else" is the error handling in case things go wrong.
				    if (heatNetworkTemp >= minTempVorlauf) {
                        valveTL01.forceOpen();
						controlValveUS01(false);
						stepcounter = 2;
                        this.logInfo(this.log, "Temperature reached, heating water tank.");

                        // In case valveError was triggered, reset error if program can actually get to here.
                        valveError = false;
                    } else {
						// Heat network should be heating up. If temperature is not reached before timer runs out,
						// start error handling.
						if (ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= timeoutMinutes) {
							checkForValveError();
						}
						this.logDebug(this.log, "Waiting for heat network to reach a temperature of "
								+ minTempVorlauf / 10 + "°C. Currently temperature is at " + heatNetworkTemp / 10 + "°C.");
                    }
				}

				// Execute once every minute while heating the water tank. Sparse execution to not spam the log.
				if (stepcounter == 2 && ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= 1) {
				    timestamp = LocalDateTime.now();

				    // waterTankTemperature is the temperature of the bottom sensor in the tank.
				    this.logDebug(this.log, "Heating the water tank. Heat source temperature is at "
							+ heatNetworkTemp / 10 + "°C, water tank lowest temperature is at "
							+ waterTankTemperature / 10 + "°C.");
                }

			} else {
				// This executes when needHotWater = false. While heating the water tank, needHotWater is true and will turn
				// to false when the required temperature in the tank is reached or the external signal stopped the heating.

				// Deactivate override, stop blocking ValveUS01. ValveUS01 now returns to state before override.
				valveUS01Override(false);

				valveTL01.forceClose();

				// Executes after heating the water tank.
				if (stepcounter == 2) {
					stepcounter = 0;
					this.logInfo(this.log, "Stopped heating the water tank.");
				}

				// Executes if needHotWater was true but turned false before heating the water tank started.
				if (stepcounter > 0) {
					stepcounter = 0;
					this.logInfo(this.log, "Aborted trying to heat the water tank.");
				}
			}
		}
	}

	private void checkForValveError() throws OpenemsError.OpenemsNamedException {

		// After timeout, check if temperature has increased at least 5°C
		if (heatNetworkTemp < temperatureControl + 50) {
			// Temperature has not increase at least 5°C --> something is broken.
			this.noError().setNextValue(false);
			valveError = true;
			this.logError(this.log, "Error: Valve or heat source malfunction! Getting only "
					+ (waermetauscherVorlauf.getTemperature().value().get() / 10) + "°C from the heat source. Valve should "
					+ "be open and temperature should rise, but not detecting the expected increase in temperature.");
		} else {
			// Temperature has increased a bit, but not enough
			if (ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= timeoutMinutes * 2) {
				// After waiting two times the timeout length, try to heat anyway.

				// open valveTL01
				valveTL01.forceOpen();

				// close valveUS01
				valvePumpControlChannel.setValveOverrideOpenClose().setNextWriteValue(false);

				stepcounter = 2;
				this.logWarn(this.log, "Warning: Temperature is only at " + (waermetauscherVorlauf.getTemperature().value().get() / 10)
						+ "°C, but should be at least at " + (minTempVorlauf / 10) + "°C by now. Check heat source for possible errors." +
						" Trying to heat the water tank anyway.");

				// Reset error in case error was triggered.
				this.noError().setNextValue(true);
				valveError = false;
			}
		}

	}

	private void valveUS01Override(boolean enable) throws OpenemsError.OpenemsNamedException {
		valvePumpControlChannel.activateValveOverride().setNextWriteValue(enable);
	}

	// This only works when valveUS01Override is enabled!
	private void controlValveUS01(boolean open) throws OpenemsError.OpenemsNamedException {
		valvePumpControlChannel.setValveOverrideOpenClose().setNextWriteValue(open);
	}

}