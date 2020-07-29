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

    private ActuatorRelaysChannel[] relayArray;
    private boolean cycleRelays;
    private long relaysCycleOnTime;
    private long relaysCycleBreakTime;
    private boolean relayIsOpenerInversion;

    private long timestampRelaysCycling;
    private boolean cycleRelaysState;        //track if any relays are on. true for on, false for off.
    private int cycleRelaysCount;        //track which relay to turn on or off.


    private PowerLevel[] dacArray;
    private boolean rampDac;
    private long dacRampStepTime;
    private int dacRampStepValue;
    private int dacRampMaxValue;

    private long timestampDacRamping;
    private boolean dacRampUp;        //track if dac ramp is going up or down. True for up.
    private int dacCurrentPower;    //track the currently set output power of the dac.
    private int dacRampCount;        //track which dac is ramping.


    private PwmPowerLevelChannel[] pwmArray;
    private boolean rampPwm;
    private long pwmRampStepTime;
    private int pwmRampStepValue;

    private long timestampPwmRamping;
    private boolean pwmRampUp;        //track if pwm ramp is going up or down. True for up.
    private int pwmCurrentPower;    //track the currently set output power of the pwm.
    private int pwmRampCount;        //track which pwm is ramping.


    public ControllerEMVtestImpl() {

        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        //start of variables needed for relays cycling

        //print log just for debugging, can be turned off
       // for (int index = 0; index < config.relaysDeviceList().length; index++) {
           // this.logInfo(this.log, "Relay array entry " + index + ": " + config.relaysDeviceList()[index]);
        //}

        relayArray = new ActuatorRelaysChannel[config.relaysDeviceList().length];        //array is filled with entries in allocate_Component method
        cycleRelays = config.cycle_relays();
        relaysCycleOnTime = config.relays_cycle_on_time() * 1000 - 500;  //convert from s to ms
        relaysCycleBreakTime = config.relays_cycle_break_time() * 1000;  //convert from s to ms
        relayIsOpenerInversion = config.relays_isopener_inversion();
        timestampRelaysCycling = System.currentTimeMillis() - relaysCycleBreakTime;
        cycleRelaysState = false;   //false means currently no relay is on
        cycleRelaysCount = 0;
        //end of variables needed for relays cycling


        //start of variables needed for dac ramping

        //print log just for debugging, can be turned off
        //for (int index = 0; index < config.DacDeviceList().length; index++) {
         //   this.logInfo(this.log, "DAC array entry " + index + ": " + config.DacDeviceList()[index]);
        //}

        dacArray = new PowerLevel[config.DacDeviceList().length];        //array is filled with entries in allocate_Component method
        rampDac = config.ramp_dac();
        dacRampStepTime = config.dac_ramp_step_time() * 1000;    //convert from s to ms
        dacRampStepValue = config.dac_ramp_step_value();
        dacRampMaxValue = config.dac_ramp_max_value();
        if (dacRampMaxValue > 100) {      //dac can't output more than 100% power.
            dacRampMaxValue = 100;
        }
        timestampDacRamping = System.currentTimeMillis() - dacRampStepTime;        //subtract dacRampStepTime so ramping starts immediately
        dacRampUp = true;
        dacCurrentPower = 0;
        dacRampCount = 0;
        //end of variables needed for dac ramping


        //start of variables needed for pwm ramping

        //print log just for debugging, can be turned off
        //for (int index = 0; index < config.PwmDeviceList().length; index++) {
         //   this.logInfo(this.log, "PWM array entry " + index + ": " + config.PwmDeviceList()[index]);
        //}

        pwmArray = new PwmPowerLevelChannel[config.PwmDeviceList().length];        //array is filled with entries in allocate_Component method
        rampPwm = config.ramp_pwm();
        pwmRampStepTime = config.pwm_ramp_step_time() * 1000 - 500;    //convert from s to ms
        pwmRampStepValue = config.pwm_ramp_step_value();

        timestampPwmRamping = System.currentTimeMillis() - pwmRampStepTime;        //subtract pwmRampStepTime so ramping starts immediately
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

        for (ActuatorRelaysChannel entry : relayArray) {       // turn off all initialized relays
            controlRelay(false, entry);
        }
        for (PowerLevel entry : dacArray) {     // turn off all initialized dac
            controlDac(0, entry);
        }
        for (PwmPowerLevelChannel entry : pwmArray) {      // turn off all initialized pwm
            controlPwm(0, entry);
        }
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        //part that does the relays cycling
        if (cycleRelays) {
            if (!cycleRelaysState && System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleBreakTime) {        //execute if no relay is on and break time has passed
                controlRelay(true, relayArray[cycleRelaysCount]);
                cycleRelaysState = true;
               // this.logInfo(this.log, "Switched on " + relayArray[cycleRelaysCount]);

                timestampRelaysCycling = System.currentTimeMillis();
            }

            if (cycleRelaysState && System.currentTimeMillis() - timestampRelaysCycling >= relaysCycleOnTime) {        //execute if any relay is on and on time has passed
                controlRelay(false, relayArray[cycleRelaysCount]);
                cycleRelaysState = false;
                cycleRelaysCount = (cycleRelaysCount + 1) % relayArray.length;
                //this.logInfo(this.log, "Switched off " + relayArray[(cycleRelaysCount - 1 + relayArray.length) % relayArray.length] + ". Next relay to switch on is " + relayArray[cycleRelaysCount]);

                timestampRelaysCycling = System.currentTimeMillis();
            }
        } else {
            for (ActuatorRelaysChannel entry : relayArray) {       // turn off all initialized relays
                controlRelay(false, entry);
            }
        }
        //end of relays cycling part


        //part that does the dac ramping
        if (rampDac) {
            if ((dacRampUp && dacCurrentPower <= dacRampMaxValue) && System.currentTimeMillis() - timestampDacRamping >= dacRampStepTime) {        //execute if dac ramp needs to go up and step time has passed
                dacCurrentPower = dacCurrentPower + dacRampStepValue;
                if (dacCurrentPower >= dacRampMaxValue) {    //check if top of ramp is reached
                    dacCurrentPower = dacRampMaxValue;    //make sure there is no overshoot
                    dacRampUp = false;    //set ramp direction to down
                }

                controlDac(dacCurrentPower, dacArray[dacRampCount]);
                //this.logInfo(this.log, "Set power for " + dacArray[dacRampCount] + " to " + dacCurrentPower + "%, currently ramping up.");

                timestampDacRamping = System.currentTimeMillis();
            }

            if (!dacRampUp && System.currentTimeMillis() - timestampDacRamping >= dacRampStepTime) {        //execute if dac ramp needs to go down and step time has passed
                dacCurrentPower = dacCurrentPower - dacRampStepValue;
                if (dacCurrentPower <= 0) {    //check if bottom of ramp is reached
                    dacCurrentPower = 0;    //make sure there is no overshoot
                    dacRampUp = true;    //set ramp direction to up
                }

                controlDac(dacCurrentPower, dacArray[dacRampCount]);
                //this.logInfo(this.log, "Set power for " + dacArray[dacRampCount] + " to " + dacCurrentPower + "%, currently ramping down.");

                if (dacCurrentPower == 0) {
                    dacRampCount = (dacRampCount + 1) % dacArray.length;    //change to next dac
                  //  this.logInfo(this.log, "Done ramping " + dacArray[(dacRampCount - 1 + dacArray.length) % dacArray.length] + ". Next is " + dacArray[dacRampCount]);
                }

                timestampDacRamping = System.currentTimeMillis();
            }
        } else {
            for (PowerLevel entry : dacArray) {    // turn off all initialized dac
                controlDac(0, entry);
            }
        }
        //end of dac ramping part


        //part that does the pwm ramping
        if (rampPwm) {
            if ((pwmRampUp && pwmCurrentPower <= 100) && System.currentTimeMillis() - timestampPwmRamping >= pwmRampStepTime) {        //execute if pwm ramp needs to go up and step time has passed
                pwmCurrentPower = pwmCurrentPower + pwmRampStepValue;
                if (pwmCurrentPower >= 100) {    //check if top of ramp is reached
                    pwmCurrentPower = 100;    //make sure there is no overshoot
                    pwmRampUp = false;    //set ramp direction to down
                }

                controlPwm(pwmCurrentPower, pwmArray[pwmRampCount]);
                //this.logInfo(this.log, "Set power for " + pwmArray[pwmRampCount] + " to " + pwmCurrentPower + "%, currently ramping up.");

                timestampPwmRamping = System.currentTimeMillis();
            }

            if (!pwmRampUp && System.currentTimeMillis() - timestampPwmRamping >= pwmRampStepTime) {        //execute if pwm ramp needs to go down and step time has passed
                pwmCurrentPower = pwmCurrentPower - pwmRampStepValue;
                if (pwmCurrentPower <= 0) {    //check if bottom of ramp is reached
                    pwmCurrentPower = 0;    //make sure there is no overshoot
                    pwmRampUp = true;    //set ramp direction to up
                }

                controlPwm(pwmCurrentPower, pwmArray[pwmRampCount]);
                //this.logInfo(this.log, "Set power for " + pwmArray[pwmRampCount] + " to " + pwmCurrentPower + "%, currently ramping down.");

                if (pwmCurrentPower == 0) {
                    pwmRampCount = (pwmRampCount + 1) % pwmArray.length;    //change to next pwm
                   // this.logInfo(this.log, "Done ramping " + pwmArray[(pwmRampCount - 1 + pwmArray.length) % pwmArray.length] + ". Next is " + pwmArray[pwmRampCount]);
                }

                timestampPwmRamping = System.currentTimeMillis();
            }
        } else {
            for (PwmPowerLevelChannel entry : pwmArray) {       // turn off all initialized pwm
                controlPwm(0, entry);
            }
        }
        //end of pwm ramping part


    }


    private void allocate_Component(String[] id, String type) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        switch (type) {
            case "Relay":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof ActuatorRelaysChannel) {
                        relayArray[index] = cpm.getComponent(id[index]);
                        if (relayIsOpenerInversion) {
                            relayArray[index].getRelaysChannel().setNextWriteValue(!relayArray[index].isCloser().getNextValue().get());        // set relay to "off" state upon initialization
                        } else {
                            relayArray[index].getRelaysChannel().setNextWriteValue(false);        // set relay to "off" state upon initialization
                        }
                    } else {
                        throw new ConfigurationException(id[index], "Allocated relay is not a (configured) relay.");
                    }
                }
                break;

            case "Dac":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof PowerLevel) {
                        dacArray[index] = cpm.getComponent(id[index]);
                        dacArray[index].getPowerLevelChannel().setNextWriteValue(0);            // set dac output to 0 upon initialization.
                    } else {
                        throw new ConfigurationException(id[index], "Allocated DAC is not a (configured) DAC.");
                    }
                }
                break;

            case "Pwm":
                for (int index = 0; index < id.length; index++) {
                    if (cpm.getComponent(id[index]) instanceof PwmPowerLevelChannel) {
                        pwmArray[index] = cpm.getComponent(id[index]);
                        pwmArray[index].getPwmPowerLevelChannel().setNextWriteValue(0.0f);            // set pwm output to 0 upon initialization.
                    } else {
                        throw new ConfigurationException(id[index], "Allocated PWM is not a (configured) PWM.");
                    }
                }
                break;
        }
    }

    private void controlRelay(boolean activate, ActuatorRelaysChannel relay) {
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
