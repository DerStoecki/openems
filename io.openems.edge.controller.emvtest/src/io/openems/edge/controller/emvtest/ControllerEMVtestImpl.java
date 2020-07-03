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

/**
 * This is the Consolinno EMV test Controller. It is used to test relays, dac's and pwm's.
 * - Relays: turn on and off, one after the other.
 * - Dac and pwm: ramp up and down, one after the other.
 *
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.EMVtest")
public class ControllerEMVtestImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

    private final Logger log = LoggerFactory.getLogger(ControllerEMVtestImpl.class);

    @Reference
    protected ComponentManager cpm;

    private ActuatorRelaysChannel[] relayArray;
    private boolean cycleRelays;        // Switch for activating relays cycling.
    private long relaysCycleOnTime;
    private long relaysCycleBreakTime;

    private long timestampRelaysCycling;
    private boolean isRelayOn;
    private int cycleRelaysCount;        // Track which relay to turn on or off.


    private PowerLevel[] dacArray;
    private boolean rampDac;        // Switch for activating dac ramping.
    private long dacRampStepTime;
    private int dacRampStepValue;
    private int dacRampMaxValue;

    private long timestampDacRamping;
    private boolean dacRampUp;        // Track if dac ramp is going up or down. True for up.
    private int dacCurrentPower;    // Track the currently set output power of the dac.
    private int dacRampCount;        // Track which dac is ramping.


    private PwmPowerLevelChannel[] pwmArray;
    private boolean rampPwm;        // Switch for activating pwm ramping.
    private long pwmRampStepTime;
    private int pwmRampStepValue;

    private long timestampPwmRamping;
    private boolean pwmRampUp;        // Track if pwm ramp is going up or down. True for up.
    private int pwmCurrentPower;    // Track the currently set output power of the pwm.
    private int pwmRampCount;        // Track which pwm is ramping.


    public ControllerEMVtestImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        // Start of variables needed for relays cycling
        for (int index = 0; index < config.relaysDeviceList().length; index++) {
            this.logDebug(this.log, "Relay array entry " + index + ": " + config.relaysDeviceList()[index]);
        }
        relayArray = new ActuatorRelaysChannel[config.relaysDeviceList().length];        // Array is filled with entries in allocate_Component method
        cycleRelays = config.cycle_relays();
        relaysCycleOnTime = config.relays_cycle_on_time() * 1000;  // Convert from s to ms
        relaysCycleBreakTime = config.relays_cycle_break_time() * 1000;  // Convert from s to ms
        timestampRelaysCycling = System.currentTimeMillis() - relaysCycleBreakTime; // Subtract relaysCycleBreakTime so that relay turns on immediately.
        isRelayOn = false;
        cycleRelaysCount = 0;
        //end of variables needed for relays cycling


        //start of variables needed for dac ramping
        for (int index = 0; index < config.DacDeviceList().length; index++) {
            this.logDebug(this.log, "DAC array entry " + index + ": " + config.DacDeviceList()[index]);
        }
        dacArray = new PowerLevel[config.DacDeviceList().length];        // Array is filled with entries in allocate_Component method
        rampDac = config.ramp_dac();
        dacRampStepTime = config.dac_ramp_step_time() * 1000;    // Convert from s to ms
        dacRampStepValue = config.dac_ramp_step_value();
        dacRampMaxValue = config.dac_ramp_max_value();
        if (dacRampMaxValue > 100) {      // Dac can't output more than 100% power.
            dacRampMaxValue = 100;
        }
        timestampDacRamping = System.currentTimeMillis() - dacRampStepTime;        // Subtract dacRampStepTime so ramping starts immediately
        dacRampUp = true;
        dacCurrentPower = 0;
        dacRampCount = 0;
        //end of variables needed for dac ramping


        //start of variables needed for pwm ramping
        for (int index = 0; index < config.PwmDeviceList().length; index++) {
            this.logDebug(this.log, "PWM array entry " + index + ": " + config.PwmDeviceList()[index]);
        }
        pwmArray = new PwmPowerLevelChannel[config.PwmDeviceList().length];        // Array is filled with entries in allocate_Component method.
        rampPwm = config.ramp_pwm();
        pwmRampStepTime = config.pwm_ramp_step_time() * 1000;    // Convert from s to ms
        pwmRampStepValue = config.pwm_ramp_step_value();
        timestampPwmRamping = System.currentTimeMillis() - pwmRampStepTime;        // Subtract pwmRampStepTime so ramping starts immediately.
        pwmRampUp = true;
        pwmCurrentPower = 0;
        pwmRampCount = 0;
        //end of variables needed for pwm ramping


        try {
            allocate_Component(config.relaysDeviceList(), "Relay");
            allocate_Component(config.DacDeviceList(), "Dac");
            allocate_Component(config.PwmDeviceList(), "Pwm");
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            throw e;
        }

    }


    @Deactivate
    public void deactivate() {
        super.deactivate();

        for (ActuatorRelaysChannel entry : relayArray) {       // Turn off all initialized relays.
            controlRelay(false, entry);
        }
        for (PowerLevel entry : dacArray) {     // Turn off all initialized dac.
            controlDac(0, entry);
        }
        for (PwmPowerLevelChannel entry : pwmArray) {      // Turn off all initialized pwm.
            controlPwm(0, entry);
        }
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        // Part that does the relays cycling.
        if (cycleRelays) {
            boolean offTimeOver = System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleBreakTime;
            if (isRelayOn == false && offTimeOver) {
                // Turn on relay number "cycleRelaysCount"
                controlRelay(true, relayArray[cycleRelaysCount]);
                isRelayOn = true;   // Track state
                timestampRelaysCycling = System.currentTimeMillis();
                this.logDebug(this.log, "Switched on relay " + relayArray[cycleRelaysCount]);
            }

            boolean onTimeOver = System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleOnTime;
            if (isRelayOn && onTimeOver) {
                // Turn off relay number "cycleRelaysCount"
                controlRelay(false, relayArray[cycleRelaysCount]);
                isRelayOn = false;
                cycleRelaysCount = (cycleRelaysCount + 1) % relayArray.length;  // Switch to next relay.
                timestampRelaysCycling = System.currentTimeMillis();
                this.logDebug(this.log, "Switched off relay "
                        + relayArray[(cycleRelaysCount - 1 + relayArray.length) % relayArray.length]
                        + ". Next relay to switch on is " + relayArray[cycleRelaysCount]);
            }
        } else {
            // Relays cycling is not switched on, so turn off all initialized relays.
            for (ActuatorRelaysChannel entry : relayArray) {
                controlRelay(false, entry);
            }
        }
        // End of relays cycling part.


        // Part that does the dac ramping.
        if (rampDac) {
            boolean proceedToNextRampStep = System.currentTimeMillis() - timestampDacRamping >= dacRampStepTime;

            if (dacRampUp && proceedToNextRampStep) {        // Execute if dac ramp needs to go up and step time has passed.
                dacCurrentPower = dacCurrentPower + dacRampStepValue;
                if (dacCurrentPower >= dacRampMaxValue) {    // Check if top of ramp is reached.
                    dacCurrentPower = dacRampMaxValue;    // Make sure there is no overshoot.
                    dacRampUp = false;    // Set ramp direction to down.
                }
                controlDac(dacCurrentPower, dacArray[dacRampCount]);
                timestampDacRamping = System.currentTimeMillis();
                this.logDebug(this.log, "Set power for " + dacArray[dacRampCount] + " to " + dacCurrentPower
                        + "%, currently ramping up.");
            }

            if (dacRampUp == false && proceedToNextRampStep) {        // Execute if dac ramp needs to go down and step time has passed.
                dacCurrentPower = dacCurrentPower - dacRampStepValue;
                if (dacCurrentPower <= 0) {    // Check if bottom of ramp is reached.
                    dacCurrentPower = 0;    // Make sure there is no overshoot.
                    dacRampUp = true;    // Set ramp direction to up.
                }
                controlDac(dacCurrentPower, dacArray[dacRampCount]);
                timestampDacRamping = System.currentTimeMillis();
                this.logDebug(this.log, "Set power for " + dacArray[dacRampCount] + " to " + dacCurrentPower
                        + "%, currently ramping down.");

                // When ramp is done, switch to next dac.
                if (dacCurrentPower == 0) {
                    dacRampCount = (dacRampCount + 1) % dacArray.length;
                    this.logDebug(this.log, "Done ramping "
                            + dacArray[(dacRampCount - 1 + dacArray.length) % dacArray.length]
                            + ". Next is " + dacArray[dacRampCount]);
                }
            }
        } else {
            // Dac ramping is not switched on, so turn off all dac.
            for (PowerLevel entry : dacArray) {
                controlDac(0, entry);
            }
        }
        // End of dac ramping part.


        // Part that does the pwm ramping.
        if (rampPwm) {
            boolean proceedToNextRampStep = System.currentTimeMillis() - timestampPwmRamping >= pwmRampStepTime;

            if (pwmRampUp && proceedToNextRampStep) {        // Execute if pwm ramp needs to go up and step time has passed.
                pwmCurrentPower = pwmCurrentPower + pwmRampStepValue;
                if (pwmCurrentPower >= 100) {    // Check if top of ramp is reached.
                    pwmCurrentPower = 100;    // Make sure there is no overshoot.
                    pwmRampUp = false;    // Set ramp direction to down.
                }
                controlPwm(pwmCurrentPower, pwmArray[pwmRampCount]);
                timestampPwmRamping = System.currentTimeMillis();
                this.logDebug(this.log, "Set power for " + pwmArray[pwmRampCount] + " to " + pwmCurrentPower
                        + "%, currently ramping up.");
            }

            if (pwmRampUp == false && proceedToNextRampStep) {        // Execute if pwm ramp needs to go down and step time has passed
                pwmCurrentPower = pwmCurrentPower - pwmRampStepValue;
                if (pwmCurrentPower <= 0) {    // Check if bottom of ramp is reached.
                    pwmCurrentPower = 0;    // Make sure there is no overshoot.
                    pwmRampUp = true;    // Set ramp direction to up.
                }
                controlPwm(pwmCurrentPower, pwmArray[pwmRampCount]);
                timestampPwmRamping = System.currentTimeMillis();
                this.logDebug(this.log, "Set power for " + pwmArray[pwmRampCount] + " to " + pwmCurrentPower
                        + "%, currently ramping down.");

                // When ramp is done, switch to next pwm.
                if (pwmCurrentPower == 0) {
                    pwmRampCount = (pwmRampCount + 1) % pwmArray.length;
                    this.logDebug(this.log, "Done ramping "
                            + pwmArray[(pwmRampCount - 1 + pwmArray.length) % pwmArray.length]
                            + ". Next is " + pwmArray[pwmRampCount]);
                }
            }
        } else {
            // Pwm ramping is not switched on, so turn off all pwm.
            for (PwmPowerLevelChannel entry : pwmArray) {
                controlPwm(0, entry);
            }
        }
        // End of pwm ramping part.


    }


    private void allocate_Component(String[] id, String type) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        switch (type) {
            case "Relay":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof ActuatorRelaysChannel) {
                        relayArray[index] = cpm.getComponent(id[index]);
                        // set relay to "off" state upon initialization
                        relayArray[index].getRelaysChannel().setNextWriteValue(false);
                    } else {
                        throw new ConfigurationException(id[index], "Allocated relay is not a (configured) relay.");
                    }
                }
                break;

            case "Dac":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof PowerLevel) {
                        dacArray[index] = cpm.getComponent(id[index]);
                        // set dac output to 0 upon initialization.
                        dacArray[index].getPowerLevelChannel().setNextWriteValue(0);
                    } else {
                        throw new ConfigurationException(id[index], "Allocated DAC is not a (configured) DAC.");
                    }
                }
                break;

            case "Pwm":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof PwmPowerLevelChannel) {
                        pwmArray[index] = cpm.getComponent(id[index]);
                        // set pwm output to 0 upon initialization.
                        pwmArray[index].getPwmPowerLevelChannel().setNextWriteValue(0.0f);
                    } else {
                        throw new ConfigurationException(id[index], "Allocated PWM is not a (configured) PWM.");
                    }
                }
                break;
        }
    }

    private void controlRelay(boolean activate, ActuatorRelaysChannel relay) {
        try {
            relay.getRelaysChannel().setNextWriteValue(activate);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    private void controlDac(int value, PowerLevel dac) {
        try {
            dac.getPowerLevelChannel().setNextWriteValue(value);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    private void controlPwm(int value, PwmPowerLevelChannel pwm) {
        float convert = (float) value;
        try {
            pwm.getPwmPowerLevelChannel().setNextWriteValue(convert);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

}
