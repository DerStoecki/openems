package io.openems.edge.controller.emvtest;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.EMVtest")
public class ControllerEMVtestImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

	private final Logger log = LoggerFactory.getLogger(ControllerEMVtestImpl.class);

	@Reference
	protected ComponentManager cpm;

	private ActuatorRelaysChannel[] relay_array;
	private boolean cycleRelays;
	private long relaysCycleOnTime;
	private long relaysCycleBreakTime;
	private boolean relayIsOpenerInversion;

	private long timestampRelaysCycling;
	private boolean cycleRelaysState;		//track if any relays are on. true for on, false for off.
	private int cycleRelaysCount;		//track which relay to turn on or off.


	private PowerLevel[] dac_array;
	private boolean rampDac;
	private long dacRampStepTime;
	private int dacRampStepValue;
	private int dacRampMaxValue;

	private long timestampDacRamping;
	private boolean dacRampUp;		//track if dac ramp is going up or down. True for up.
	private int dacCurrentPower;	//track the currently set output power of the dac.
	private int dacRampCount;		//track which dac is ramping.


	private PwmPowerLevelChannel[] pwm_array;
	private boolean rampPwm;
	private long pwmRampStepTime;
	private int pwmRampStepValue;

	private long timestampPwmRamping;
	private boolean pwmRampUp;		//track if pwm ramp is going up or down. True for up.
	private int pwmCurrentPower;	//track the currently set output power of the pwm.
	private int pwmRampCount;		//track which pwm is ramping.


	public ControllerEMVtestImpl() {

		super(OpenemsComponent.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		//start of variables needed for relays cycling

		//print log just for debugging, can be turned off
		for ( int index = 0; index < config.relaysDeviceList().length; index++) {
			this.logInfo(this.log, "Relay array entry " + index + ": " + config.relaysDeviceList()[index]);
		}

		relay_array = new ActuatorRelaysChannel[config.relaysDeviceList().length];		//array is filled with entries in allocate_Component method
		cycleRelays = config.cycle_relays();
		relaysCycleOnTime = config.relays_cycle_on_time() * 1000;  //convert from s to ms
		relaysCycleBreakTime = config.relays_cycle_break_time() * 1000;  //convert from s to ms
		relayIsOpenerInversion = config.relays_isopener_inversion();
		timestampRelaysCycling = System.currentTimeMillis();
		cycleRelaysState = false;   //false means currently no relay is on
		cycleRelaysCount = 0;
		//end of variables needed for relays cycling


		//start of variables needed for dac ramping

		//print log just for debugging, can be turned off
		for ( int index = 0; index < config.DacDeviceList().length; index++) {
			this.logInfo(this.log, "DAC array entry " + index + ": " + config.DacDeviceList()[index]);
		}

		dac_array = new PowerLevel[config.DacDeviceList().length];		//array is filled with entries in allocate_Component method
		rampDac = config.ramp_dac();
		dacRampStepTime = config.dac_ramp_step_time() * 1000;	//convert from s to ms
		dacRampStepValue = config.dac_ramp_step_value();
		dacRampMaxValue = config.dac_ramp_max_value();
		if (dacRampMaxValue > 100)		//dac can't output more than 100% power.
			dacRampMaxValue = 100;

		timestampDacRamping = System.currentTimeMillis() - dacRampStepTime;		//subtract dacRampStepTime so ramping starts immediately
		dacRampUp = true;
		dacCurrentPower = 0;
		dacRampCount = 0;
		//end of variables needed for dac ramping


		//start of variables needed for pwm ramping

		//print log just for debugging, can be turned off
		for ( int index = 0; index < config.PwmDeviceList().length; index++) {
			this.logInfo(this.log, "PWM array entry " + index + ": " + config.PwmDeviceList()[index]);
		}

		pwm_array = new PwmPowerLevelChannel[config.PwmDeviceList().length];		//array is filled with entries in allocate_Component method
		rampPwm = config.ramp_pwm();
		pwmRampStepTime = config.pwm_ramp_step_time() * 1000;	//convert from s to ms
		pwmRampStepValue = config.pwm_ramp_step_value();

		timestampPwmRamping = System.currentTimeMillis() - pwmRampStepTime;		//subtract pwmRampStepTime so ramping starts immediately
		pwmRampUp = true;
		pwmCurrentPower = 0;
		pwmRampCount = 0;
		//end of variables needed for pwm ramping


		try {
			allocate_Component(config.relaysDeviceList(), "Relay");
			allocate_Component(config.DacDeviceList(), "Dac");
			allocate_Component(config.PwmDeviceList(), "Pwm");
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
			throw e;
		}

	}


	@Deactivate
	public void deactivate() {
		super.deactivate();

		for (ActuatorRelaysChannel entry : relay_array)		// turn off all initialized relays
			controlRelay(false, entry);

		for (PowerLevel entry : dac_array)		// turn off all initialized dac
			controlDac(0, entry);

		for (PwmPowerLevelChannel entry : pwm_array)		// turn off all initialized pwm
			controlPwm(0, entry);

	}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		//part that does the relays cycling
		if (cycleRelays){
			if (!cycleRelaysState && System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleBreakTime){		//execute if no relay is on and break time has passed
				controlRelay(true, relay_array[cycleRelaysCount]);
				cycleRelaysState = true;
				this.logInfo(this.log, "Switched on " + relay_array[cycleRelaysCount]);

				timestampRelaysCycling = System.currentTimeMillis();
			}

			if (cycleRelaysState && System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleOnTime){		//execute if any relay is on and on time has passed
				controlRelay(false, relay_array[cycleRelaysCount]);
				cycleRelaysState = false;
				cycleRelaysCount = (cycleRelaysCount+1)%relay_array.length;
				this.logInfo(this.log, "Switched off " + relay_array[(cycleRelaysCount - 1 + relay_array.length)%relay_array.length] + ". Next relay to switch on is " + relay_array[cycleRelaysCount]);

				timestampRelaysCycling = System.currentTimeMillis();
			}
		} else {
			for (ActuatorRelaysChannel entry : relay_array)		// turn off all initialized relays
				controlRelay(false, entry);
		}
		//end of relays cycling part


		//part that does the dac ramping
		if (rampDac){
			if ( (dacRampUp && dacCurrentPower <= dacRampMaxValue) && System.currentTimeMillis() - timestampDacRamping >= dacRampStepTime){		//execute if dac ramp needs to go up and step time has passed
				dacCurrentPower = dacCurrentPower + dacRampStepValue;
				if (dacCurrentPower >= dacRampMaxValue){	//check if top of ramp is reached
					dacCurrentPower = dacRampMaxValue;	//make sure there is no overshoot
					dacRampUp = false;	//set ramp direction to down
				}

				controlDac(dacCurrentPower, dac_array[dacRampCount]);
				this.logInfo(this.log, "Set power for " + dac_array[dacRampCount] + " to " + dacCurrentPower + "%, currently ramping up.");

				timestampDacRamping = System.currentTimeMillis();
			}

			if ( !dacRampUp && System.currentTimeMillis() - timestampDacRamping >= dacRampStepTime){		//execute if dac ramp needs to go down and step time has passed
				dacCurrentPower = dacCurrentPower - dacRampStepValue;
				if (dacCurrentPower <= 0){	//check if bottom of ramp is reached
					dacCurrentPower = 0;	//make sure there is no overshoot
					dacRampUp = true;	//set ramp direction to up
				}

				controlDac(dacCurrentPower, dac_array[dacRampCount]);
				this.logInfo(this.log, "Set power for " + dac_array[dacRampCount] + " to " + dacCurrentPower + "%, currently ramping down.");

				if (dacCurrentPower == 0){
					dacRampCount = (dacRampCount+1)%dac_array.length;	//change to next dac
					this.logInfo(this.log, "Done ramping " + dac_array[(dacRampCount - 1 + dac_array.length)%dac_array.length] + ". Next is " + dac_array[dacRampCount]);
				}

				timestampDacRamping = System.currentTimeMillis();
			}
		} else {
			for (PowerLevel entry : dac_array)		// turn off all initialized dac
				controlDac(0, entry);
		}
		//end of dac ramping part


		//part that does the pwm ramping
		if (rampPwm){
			if ( (pwmRampUp && pwmCurrentPower <= 100) && System.currentTimeMillis() - timestampPwmRamping >= pwmRampStepTime){		//execute if pwm ramp needs to go up and step time has passed
				pwmCurrentPower = pwmCurrentPower + pwmRampStepValue;
				if (pwmCurrentPower >= 100){	//check if top of ramp is reached
					pwmCurrentPower = 100;	//make sure there is no overshoot
					pwmRampUp = false;	//set ramp direction to down
				}

				controlPwm(pwmCurrentPower, pwm_array[pwmRampCount]);
				this.logInfo(this.log, "Set power for " + pwm_array[pwmRampCount] + " to " + pwmCurrentPower + "%, currently ramping up.");

				timestampPwmRamping = System.currentTimeMillis();
			}

			if ( !pwmRampUp && System.currentTimeMillis() - timestampPwmRamping >= pwmRampStepTime){		//execute if pwm ramp needs to go down and step time has passed
				pwmCurrentPower = pwmCurrentPower - pwmRampStepValue;
				if (pwmCurrentPower <= 0){	//check if bottom of ramp is reached
					pwmCurrentPower = 0;	//make sure there is no overshoot
					pwmRampUp = true;	//set ramp direction to up
				}

				controlPwm(pwmCurrentPower, pwm_array[pwmRampCount]);
				this.logInfo(this.log, "Set power for " + pwm_array[pwmRampCount] + " to " + pwmCurrentPower + "%, currently ramping down.");

				if (pwmCurrentPower == 0){
					pwmRampCount = (pwmRampCount+1)%pwm_array.length;	//change to next pwm
					this.logInfo(this.log, "Done ramping " + pwm_array[(pwmRampCount - 1 + pwm_array.length)%pwm_array.length] + ". Next is " + pwm_array[pwmRampCount]);
				}

				timestampPwmRamping = System.currentTimeMillis();
			}
		} else {
			for (PwmPowerLevelChannel entry : pwm_array)		// turn off all initialized pwm
				controlPwm(0, entry);
		}
		//end of pwm ramping part


	}


	private void allocate_Component(String[] id, String type) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		switch (type) {
			case "Relay":
				for ( int index = 0; index < id.length; index++) {
					if (cpm.getComponent(id[index]) instanceof ActuatorRelaysChannel) {
						relay_array[index] = cpm.getComponent(id[index]);
						if (relayIsOpenerInversion)
							relay_array[index].getRelaysChannel().setNextWriteValue(!relay_array[index].isCloser().getNextValue().get());        // set relay to "off" state upon initialization
						else
							relay_array[index].getRelaysChannel().setNextWriteValue(false);        // set relay to "off" state upon initialization

					} else {
						throw new ConfigurationException(id[index], "Allocated relay is not a (configured) relay.");
					}
				}
				break;

			case "Dac":
				for ( int index = 0; index < id.length; index++) {
					if (cpm.getComponent(id[index]) instanceof PowerLevel) {
						dac_array[index] = cpm.getComponent(id[index]);
						dac_array[index].getPowerLevelChannel().setNextWriteValue(0);			// set dac output to 0 upon initialization.
					} else {
						throw new ConfigurationException(id[index], "Allocated DAC is not a (configured) DAC.");
					}
				}
				break;

			case "Pwm":
				for ( int index = 0; index < id.length; index++) {
					if (cpm.getComponent(id[index]) instanceof PwmPowerLevelChannel) {
						pwm_array[index] = cpm.getComponent(id[index]);
						pwm_array[index].getPwmPowerLevelChannel().setNextWriteValue(0.0f);			// set pwm output to 0 upon initialization.
					} else {
						throw new ConfigurationException(id[index], "Allocated PWM is not a (configured) PWM.");
					}
				}
				break;
		}
	}

	public void controlRelay(boolean activate, ActuatorRelaysChannel relay) {
		try {
			if (relayIsOpenerInversion) {
				if (relay.isCloser().value().get()) {
					relay.getRelaysChannel().setNextWriteValue(activate);
				} else {
					relay.getRelaysChannel().setNextWriteValue(!activate);
				}
			} else {
				relay.getRelaysChannel().setNextWriteValue(activate);
			}

		} catch (OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
		}
	}

	public void controlDac(int value, PowerLevel dac) {
		try {
			dac.getPowerLevelChannel().setNextWriteValue(value);
		} catch (OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
		}
	}

	public void controlPwm(int value, PwmPowerLevelChannel pwm) {
		float convert = (float) value;
		try {
			pwm.getPwmPowerLevelChannel().setNextWriteValue(convert);
		} catch (OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
		}
	}

}
