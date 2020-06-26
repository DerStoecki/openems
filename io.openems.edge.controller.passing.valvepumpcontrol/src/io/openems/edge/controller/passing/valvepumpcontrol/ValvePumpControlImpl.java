package io.openems.edge.controller.passing.valvepumpcontrol;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.channel.ChannelId;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.controlcenter.api.PassingControlCenterChannel;
import io.openems.edge.temperature.passing.pump.api.Pump;
import io.openems.edge.temperature.passing.valve.api.Valve;
import io.openems.edge.temperature.passing.api.PassingForPid;
import io.openems.edge.controller.passing.valvepumpcontrol.api.ValvePumpControlChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This controller opens a valve and activates a pump when it receives the signal that the heater wants to heat.
 * There is also an override channel to manage access to the valve by another controllers. The override channel
 * has priority for valve control over the heater signal.
 *
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "ValvePumpControl", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ValvePumpControlImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller, ValvePumpControlChannel {

	private final Logger log = LoggerFactory.getLogger(ValvePumpControlImpl.class);

	@Reference
	protected ComponentManager cpm;

	private PassingControlCenterChannel heatingController;
	private Valve valveUS01;
	private Pump pumpHK01;


	public ValvePumpControlImpl() {
		super(OpenemsComponent.ChannelId.values(),
				ValvePumpControlChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}


	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.noError().setNextValue(true);

		//allocate components
		try {
			if (cpm.getComponent(config.allocated_Heating_Controller()) instanceof PassingControlCenterChannel) {
				heatingController = cpm.getComponent(config.allocated_Heating_Controller());
			} else {
				throw new ConfigurationException(config.allocated_Heating_Controller(),
						"Allocated Heating Controller not a Heating Controller; Check if Name is correct and try again.");
			}
			if (cpm.getComponent(config.valveUS01Id()) instanceof Valve) {
				valveUS01 = cpm.getComponent(config.valveUS01Id());
			} else {
				throw new ConfigurationException("The configured component is not a valve! Please check "
						+ config.valveUS01Id(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.pumpHK01Id()) instanceof Pump) {
				pumpHK01 = cpm.getComponent(config.pumpHK01Id());
			} else {
				throw new ConfigurationException("The configured component is not a pump! Please check "
						+ config.valveUS01Id(), "configured component is incorrect!");
			}
		} catch (ConfigurationException | OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
			throw e;
		}

	}


	@Deactivate
	public void deactivate() {
		super.deactivate();
		valveUS01.forceClose();
		pumpHK01.changeByPercentage(-101); // = deactivate. Use 101 because variable is double and math with double is not accurate.
	}


	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Check if override is active. Null in channel is counted as false.
		if (activateValveOverride().value().orElse(false)) { // Branch for active override
			// Check if there is a value.
			if (setValveOverrideOpenClose().value().isDefined()) {
				// True means open
				if (setValveOverrideOpenClose().value().get()) {
					valveOpen();
				} else {
					valveClose();
				}
			}
		} else { // This executes when there is no override

			// If heating controller says it wants to heat, open valve and activate pump.
			if (heatingController.activateHeater().value().orElse(false)) {

				valveOpen();

				// Check if pump is operational. If there is null in power level channel, something is wrong.
				if (pumpHK01.getPowerLevel().value().isDefined()){
					// Check if pump is already at full power.
					if (pumpHK01.getPowerLevel().value().get() < 100) {
						pumpHK01.changeByPercentage(100); // = full power.
					}
				} else {
					this.logInfo(this.log, "ERROR: null in pump power level channel. Something must be wrong with the pump!");
				}
			} else {
				// When no heating is required, close valve and stop pump.

				valveClose();

				// Check if pump has already stopped.
				if (pumpHK01.getPowerLevel().value().orElse(0.0) > 0) {
					pumpHK01.changeByPercentage(-100); // = deactivate
				}
			}
		}




	}

	private void valveClose () {
		// Check if valve is already closed. If there is null in the channel, it's probably offline and closed.
		if (valveUS01.getPowerLevel().value().orElse(0.0) > 0) {
			valveUS01.forceClose();
		}
	}

	private void valveOpen () {
		// Check if valve is operational. If there is null in power level channel, something is wrong.
		if (valveUS01.getPowerLevel().value().isDefined()){
			// Check if valve is already open.
			if (valveUS01.getPowerLevel().value().get() < 100) {
				valveUS01.forceOpen();
			}
		} else {
			this.logInfo(this.log, "ERROR: null in valve power level channel. Something must be wrong with the valve!");
		}
	}

}


