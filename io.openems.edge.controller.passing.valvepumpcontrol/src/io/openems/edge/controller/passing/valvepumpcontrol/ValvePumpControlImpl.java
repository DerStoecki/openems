package io.openems.edge.controller.passing.valvepumpcontrol;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.controlcenter.api.PassingControlCenterChannel;
import io.openems.edge.temperature.passing.pump.api.Pump;
import io.openems.edge.temperature.passing.valve.api.Valve;
import io.openems.edge.controller.passing.valvepumpcontrol.api.ValvePumpControlChannel;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;


/**
 * This controller opens a valve and activates a pump when it receives the signal that the heater wants to heat.
 * There is also an override channel to manage access to the valve by another controller. The override channel
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
	private int closeValvePercent=20;

	// Variables for channel readout
	private boolean heaterWantsToHeat;
	private boolean valveOverrideActive;


	public ValvePumpControlImpl() {
		super(OpenemsComponent.ChannelId.values(),
				ValvePumpControlChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}


	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.noError().setNextValue(true);
		this.closeValvePercent = config.closeValvePercent();
		//allocate components

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
			try {
				if ( cpm.getComponent(config.pumpHK01Id()) instanceof Pump) {
					pumpHK01 = cpm.getComponent(config.pumpHK01Id());
				} else {

				}
			}catch (Exception e){

			}

	}


	@Deactivate
	public void deactivate() {
		super.deactivate();

		valveUS01.forceClose();
		if(pumpHK01 !=null)
		{
			pumpHK01.changeByPercentage(-101); // = deactivate. Use 101 because variable is double and math with double is not accurate.

		}
	}

	@Reference
	ConfigurationAdmin ca;
	private void updateConfig() {
		Configuration c;

		try {
			c = ca.getConfiguration(this.servicePid(), "?");
			Dictionary<String, Object> properties = c.getProperties();
			if(this.valveClosing().value().isDefined())
			{
				properties.put("closeValvePercent",this.valveClosing().value().get());
				closeValvePercent =this.valveClosing().value().get();
			}
			c.update(properties);
		} catch (IOException e) {
		}
	}
	@Override
	public void run() throws OpenemsError.OpenemsNamedException {
		boolean restchange = this.valveClosing().getNextWriteValueAndReset().isPresent();

		if(restchange)
		{
			updateConfig();
		}
		// Transfer channel data to local variables for better readability of logic code.
		heaterWantsToHeat = heatingController.activateHeater().value().orElse(false);	// Null in channel is counted as false.
		valveOverrideActive = activateValveOverride().value().orElse(false); // Null in channel is counted as false.

		// Control logic.
		// If heating controller says it wants to heat, open valve and activate pump.
		// When no heating is required, close valve and stop pump.
		if (valveOverrideActive) {
			executeOverrideCommand();
		} else {
			if (heaterWantsToHeat) {
				valveOpen();
			} else {
				//
				valveClose(closeValvePercent);
			}
		}
		if (heaterWantsToHeat) {
			startPump();
		} else {
			stopPump();
		}
	}

	private void valveClose(int percent) {
		// Check if valve is already closed. If there is null in the channel, it's probably offline and closed.
		boolean isValveBusy= valveUS01.getIsBusy().value().isDefined() &&valveUS01.getIsBusy().value().get();
		if(isValveBusy || valveUS01.getPowerLevel().value().get() <percent)
		{
			return;
		}

		valveUS01.changeByPercentage(percent - valveUS01.getPowerLevel().value().get());

	}

	private void valveOpen() {
		// Check if valve is operational. If there is null in power level channel, something is wrong.
		if (valveUS01.getPowerLevel().value().isDefined()){
			// Check if valve is already open.
			if (valveUS01.getPowerLevel().value().get() < 100) {

				boolean isValveBusy= valveUS01.getIsBusy().value().isDefined() &&valveUS01.getIsBusy().value().get();
				if(isValveBusy)
				{
					return;
				}
				valveUS01.changeByPercentage(100- valveUS01.getPowerLevel().value().get());
			}
			this.noError().setNextValue(true);
		} else {
			this.noError().setNextValue(false);
			this.logError(this.log, "ERROR: null in valve power level channel. Something must be wrong with the valve!");
		}
	}

	private void stopPump() {
		// Check if pump has already stopped.
		if (closeValvePercent==0 && pumpHK01!= null && pumpHK01.getPowerLevel().value().orElse(0.0) > 0) {
			pumpHK01.changeByPercentage(-101); // = deactivate. Use 101 because variable is double and math with double is not accurate.
		}
	}

	// Pump goes to full speed.
	private void startPump() {
		if(pumpHK01 != null) {
			// Check if pump is operational. If there is null in power level channel, something is wrong.
			if (pumpHK01.getPowerLevel().value().isDefined()) {
				// Check if pump is already at full power.
				if (pumpHK01.getPowerLevel().value().get() < 100) {
					pumpHK01.changeByPercentage(100); // = full power.
				}
				this.noError().setNextValue(true);
			} else {
				this.noError().setNextValue(false);
				this.logError(this.log, "ERROR: null in pump power level channel. Something must be wrong with the pump!");
			}
		}
	}

	// The override opens or closes the valve.
	private void executeOverrideCommand() {
		// Check if there is a value.
		if (setValveOverrideOpenClose().value().isDefined()) {
			// True means open
			if (setValveOverrideOpenClose().value().get()) {
				valveOpen();
			} else {
				valveClose(closeValvePercent);
			}
		}
	}

}


