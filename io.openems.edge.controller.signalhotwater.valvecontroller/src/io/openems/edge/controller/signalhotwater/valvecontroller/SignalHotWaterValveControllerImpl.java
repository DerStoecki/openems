package io.openems.edge.controller.signalhotwater.valvecontroller;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
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

@Designate(ocd = Config.class, factory = true)
@Component(name = "SignalHotWater.Valvecontroller", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SignalHotWaterValveControllerImpl extends AbstractOpenemsComponent implements OpenemsComponent, SignalHotWaterValvecontrollerChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(SignalHotWaterValveControllerImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer waermetauscherVorlauf;
	private SignalHotWaterChannel signalHotWaterChannel;
	private Valve valveUS01;
	private Valve valveTL01;
	private int minTempVorlauf;
	private int stepcounter;
	private LocalDateTime timestamp;
	private int timeoutMinutes;
	private int temperatureControl;
	private boolean valveError;

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
		this.blockValve().setNextValue(false);
		this.noError().setNextValue(true);
		timeoutMinutes = config.timeout();
		valveError = false;

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
			if (cpm.getComponent(config.valveUS01Id()) instanceof Valve) {
				this.valveUS01 = cpm.getComponent(config.valveUS01Id());
			} else {
				throw new ConfigurationException("The configured component is not a valve! Please check "
						+ config.valveUS01Id(), "configured component is incorrect!");
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

	/**
	 * This controller receives the "needHotWater" signal from the "SignalHotWater" controller. This controller is
	 * responsible for switching the right valves in the right order to transfer heat to the water tank the
	 * "SignalHotWater" controller is monitoring.
	 *
	 */

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Sensor checks.
		if (waermetauscherVorlauf.getTemperature().value().isDefined()) {
			// Listen to the needHotWater signal
			if (signalHotWaterChannel.needHotWater().value().isDefined() && signalHotWaterChannel.needHotWater().value().get()) {

				// Heating process is executed in steps. Stepcounter tracks progress, start is at stepcounter == 0.
				if (stepcounter == 0) {
					// open valveUS01
					valveUS01.controlRelays(true, "Open");
					valveUS01.controlRelays(false, "Closed");

					stepcounter = 1;

					// Timestamp and temperature saved to check if valve/heat source is functional.
					timestamp = LocalDateTime.now();
					temperatureControl = waermetauscherVorlauf.getTemperature().value().get();
					this.logInfo(this.log, "NeedHotWater signal received. Waiting for heat source to reach temperature.");
				}


				if (stepcounter == 1) {
				    // "If = true" is the regular code, "else" is the error handling in case things go wrong.
				    if (waermetauscherVorlauf.getTemperature().value().get() >= minTempVorlauf) {
                        this.blockValve().setNextValue(true);

                        // open valveTL01
                        valveTL01.controlRelays(true, "Open");
                        valveTL01.controlRelays(false, "Closed");

                        // close valveUS01
                        valveUS01.controlRelays(false, "Open");
                        valveUS01.controlRelays(true, "Closed");

                        stepcounter = 2;
                        this.logInfo(this.log, "Temperature reached, heating water tank.");

                        // In case error was triggered, delete error if program can actually get to here.
                        valveError = false;
                        this.noError().setNextValue(true);
                    } else {
				        // After timeout, check if temperature has increased at least 5°C
				        if (ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= timeoutMinutes) {
				            if (waermetauscherVorlauf.getTemperature().value().get() < temperatureControl + 50) {
				                // Temperature has not increase at least 5°C --> something is broken.
                                this.noError().setNextValue(false);
				                valveError = true;
                                this.logInfo(this.log, "Error: Valve or heat source malfunction! Getting only "
										+ (waermetauscherVorlauf.getTemperature().value().get() / 10) + "°C from the heat source. Valve should "
										+ "be open and temperature should rise, but not detecting the expected increase in temperature.");
                            } else {
				                // Temperature has increased a bit, but not enough
                                if (ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= timeoutMinutes * 2) {
                                    // After waiting two times the timeout length, try to heat anyway.
                                    this.blockValve().setNextValue(true);

                                    // open valveTL01
                                    valveTL01.controlRelays(true, "Open");
                                    valveTL01.controlRelays(false, "Closed");

                                    // close valveUS01
                                    valveUS01.controlRelays(false, "Open");
                                    valveUS01.controlRelays(true, "Closed");

                                    stepcounter = 2;
                                    this.logInfo(this.log, "Temperature is only at " + (waermetauscherVorlauf.getTemperature().value().get() / 10)
                                            + "°C, but should be at least at " + (minTempVorlauf / 10) + "°C by now. Check heat source for possible errors." +
											" Trying to heat the water tank anyway.");
                                }
                            }
                        }
                    }
				}

				// Execute once every minute while heating the water tank. Sparse execution to not spam the log.
				if (stepcounter == 2 && ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now()) >= 1) {
				    timestamp = LocalDateTime.now();

				    // waterTankTemp() is the temperature of the bottom sensor in the tank.
				    if (signalHotWaterChannel.waterTankTemp().value().isDefined()) {
						this.logInfo(this.log, "Heating the water tank. Heat source temperature is at "
										+ (waermetauscherVorlauf.getTemperature().value().get() / 10)
										+ "°C, water tank lowest temperature is at "
								+ (signalHotWaterChannel.waterTankTemp().value().get() / 10) + "°C.");
					} else {
						this.logInfo(this.log, "Error, not getting a temperature signal from the water tank!");
					}
                }

			// This executes when needHotWater = false. While heating the water tank, needHotWater is true and will turn
			// to false when the required temperature in the tank is reached or the external signal stopped the heating.
			} else {
				this.blockValve().setNextValue(false);

				// close valveTL01
				valveTL01.controlRelays(false, "Open");
				valveTL01.controlRelays(true, "Closed");

				// Executes after heating the water tank. Do just once to not mess with other controllers accessing valveUS01.
				if (stepcounter == 2) {
					//open valveUS01
					valveUS01.controlRelays(true, "Open");
					valveUS01.controlRelays(false, "Closed");

					stepcounter = 0;
					this.logInfo(this.log, "Stopped heating the water tank.");
				}

				// Executes if needHotWater was true but turned false before heating the water tank started.
				if (stepcounter > 0) {
					stepcounter = 0;
					this.logInfo(this.log, "Aborted trying to heat the water tank.");
				}
			}

			if (!this.noError().value().get() && !valveError) {
				this.noError().setNextValue(true);
				this.logInfo(this.log, "Temperature sensor is fine now!");
			}
		} else {
			this.noError().setNextValue(false);
			this.logInfo(this.log, "Not getting any data from the passing station forward temperature sensor " + waermetauscherVorlauf.id() + ".");
		}
	}

}