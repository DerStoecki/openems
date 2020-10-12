package io.openems.edge.controller.pump.grundfos;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.pump.grundfos.api.ControlModeSetting;
import io.openems.edge.controller.pump.grundfos.api.PumpGrundfosControllerChannels;
import io.openems.edge.pump.grundfos.api.ControlMode;
import io.openems.edge.pump.grundfos.api.PumpGrundfosChannels;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;


/**
 * A controller to operate a Grundfos pump via GENIbus in constant pressure mode.
 */
@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.PumpGrundfos", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PumpGrundfosControllerImpl extends AbstractOpenemsComponent implements Controller, OpenemsComponent, PumpGrundfosControllerChannels {


    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    PumpGrundfosChannels needsThisToBeActive;

    @Reference
    ComponentManager cpm;

    private final Logger log = LoggerFactory.getLogger(PumpGrundfosControllerImpl.class);
    private final DecimalFormat formatter2 = new DecimalFormat("#0.00");
    private final DecimalFormat formatter1 = new DecimalFormat("#0.0");

    private double setpoint = 0;
    double pressureSetpoint;
    double frequencySetpoint;
    private ControlModeSetting controlModeSetting;
    private boolean stopPump;
    private boolean pumpWink;
    private PumpGrundfosChannels pumpChannels;
    private boolean verbose;
    private boolean onlyRead;
    private int testCounter = 0;

    public PumpGrundfosControllerImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values(),
                PumpGrundfosControllerChannels.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        try {
            if (cpm.getComponent(config.pumpId()) instanceof PumpGrundfosChannels) {
                this.pumpChannels = cpm.getComponent(config.pumpId());
            } else {
                throw new ConfigurationException("Pump not correct instance, check Id!", "Incorrect Id in Config");
            }
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }
        controlModeSetting = config.controlMode();
        stopPump = config.stopPump();
        pumpWink = config.pumpWink();
        pressureSetpoint = config.pressureSetpoint();
        frequencySetpoint = config.frequencySetpoint();
        onlyRead = config.onlyRead();
        try {
            // Fill all containers of the channels with values. This is needed since "run()" takes the "value" container
            // of the channels.
            setControlMode().setNextWriteValue(controlModeSetting.getValue());
            setControlMode().setNextValue(controlModeSetting.getValue());
            setControlMode().nextProcessImage();
            setStopPump().setNextWriteValue(stopPump);
            setStopPump().setNextValue(stopPump);
            setStopPump().nextProcessImage();
            setFlashLed().setNextWriteValue(pumpWink);
            setFlashLed().setNextValue(pumpWink);
            setFlashLed().nextProcessImage();
            setPressureSetpoint().setNextWriteValue(pressureSetpoint);
            setPressureSetpoint().setNextValue(pressureSetpoint);
            setPressureSetpoint().nextProcessImage();
            setFrequencySetpoint().setNextWriteValue(frequencySetpoint);
            setFrequencySetpoint().setNextValue(frequencySetpoint);
            setFrequencySetpoint().nextProcessImage();
            setOnlyRead().setNextWriteValue(onlyRead);
            setOnlyRead().setNextValue(onlyRead);
            setOnlyRead().nextProcessImage();

            if (onlyRead == false) {
                changeControlMode();
                startStopPump();
                pumpFlashLed();
            }

        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }

        verbose = config.printPumpStatus();

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    private void changeControlMode() throws OpenemsError.OpenemsNamedException {
        switch (controlModeSetting) {
            case MIN_MOTOR_CURVE:
                this.pumpChannels.setMinMotorCurve().setNextWriteValue(true);
                break;
            case MAX_MOTOR_CURVE:
                this.pumpChannels.setMaxMotorCurve().setNextWriteValue(true);
                break;
            case AUTO_ADAPT:
                this.pumpChannels.setAutoAdapt().setNextWriteValue(true);
                break;
            case CONST_FREQUENCY:
                this.pumpChannels.setConstFrequency().setNextWriteValue(true);
                // Set interval to maximum. Change this if more precision is needed. Fmin minimum is 52% for MAGNA3.
                // You can set Fmin lower than that, but this will have no effect. Motor can't run slower than 52%.
                this.pumpChannels.setFmin().setNextWriteValue(0.52);
                this.pumpChannels.setFmax().setNextWriteValue(1.0);
                break;
            case CONST_PRESSURE:
                this.pumpChannels.setConstPressure().setNextWriteValue(true);
                // Set interval to sensor interval. Change this if more precision is needed.
                this.pumpChannels.setConstRefMinH().setNextWriteValue(0.0);
                this.pumpChannels.setConstRefMaxH().setNextWriteValue(1.0);
                break;
        }
    }

    private void startStopPump() throws OpenemsError.OpenemsNamedException {
        if (stopPump) {
            this.pumpChannels.setStop().setNextWriteValue(true);
        } else {
            this.pumpChannels.setStart().setNextWriteValue(true);
        }
    }

    private void pumpFlashLed() throws OpenemsError.OpenemsNamedException {
        if (pumpWink) {
            this.pumpChannels.setWinkOn().setNextWriteValue(true);
        } else {
            this.pumpChannels.setWinkOff().setNextWriteValue(true);
        }
    }

    /**
     * Gets the Commands usually from config; or REST/JSON Request and writes ReferenceValues in channels.
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        // Copy "write" to "nextValue".
        boolean writeChannelsHaveValues = setControlMode().getNextWriteValue().isPresent()
                && setStopPump().getNextWriteValue().isPresent()
                && setFlashLed().getNextWriteValue().isPresent()
                && setPressureSetpoint().getNextWriteValue().isPresent()
                && setFrequencySetpoint().getNextWriteValue().isPresent()
                && setOnlyRead().getNextWriteValue().isPresent();
        if (writeChannelsHaveValues) {
            setControlMode().setNextValue(setControlMode().getNextWriteValue().get());
            setStopPump().setNextValue(setStopPump().getNextWriteValue().get());
            setFlashLed().setNextValue(setFlashLed().getNextWriteValue().get());
            setPressureSetpoint().setNextValue(setPressureSetpoint().getNextWriteValue().get());
            setFrequencySetpoint().setNextValue(setFrequencySetpoint().getNextWriteValue().get());
            setOnlyRead().setNextValue(setOnlyRead().getNextWriteValue().get());
        }

        if (onlyRead == false) {
            // Puts the pump in remote mode. Send every second.
            this.pumpChannels.setRemote().setNextWriteValue(true);

            boolean channelsHaveValues =  setControlMode().value().isDefined()
                    && setStopPump().value().isDefined()
                    && setFlashLed().value().isDefined()
                    && setPressureSetpoint().value().isDefined()
                    && setFrequencySetpoint().value().isDefined()
                    && pumpChannels.isConnectionOk().value().isDefined();
            if (channelsHaveValues) {

                // Copy values from channels
                controlModeSetting = setControlMode().value().asEnum();
                stopPump = setStopPump().value().get();
                pumpWink = setFlashLed().value().get();
                pressureSetpoint = setPressureSetpoint().value().get();
                frequencySetpoint = setFrequencySetpoint().value().get();

                // In case of a connection loss, all commands and configuration values need to be sent again.
                // Because connection loss can also mean pump was turned off and restarted, or it may even be a different
                // pump at this address.
                if (pumpChannels.isConnectionOk().value().get()) {

                    // Compare pump status with controller settings. Send commands if there is a difference.
                    if (stopPump) {
                        if (pumpChannels.getMotorFrequency().value().orElse(0.0) > 0) {
                            startStopPump();
                        }
                    } else {
                        if (pumpChannels.getMotorFrequency().value().orElse(0.0) <= 0) {
                            startStopPump();
                        }
                    }
                    switch (controlModeSetting) {
                        case CONST_PRESSURE:
                            if (pumpChannels.getActualControlMode().value().asEnum() != ControlMode.CONST_PRESS) {
                                changeControlMode();
                            }
                            break;
                        case CONST_FREQUENCY:
                            double frequency = pumpChannels.getMotorFrequency().value().orElse(0.0);
                            double maxFrequency = pumpChannels.setFnom().value().orElse(0.0);

                            // Need to allow some leeway since 8 bit conversion introduces inaccuracies.
                            boolean frequencyOk = frequency > (setpoint - 0.02) * maxFrequency && frequency < (setpoint + 0.02) * maxFrequency;
                            if (frequencyOk == false || pumpChannels.getActualControlMode().value().asEnum() != ControlMode.CONST_FREQ) {
                                changeControlMode();

                            }
                            break;
                        case MIN_MOTOR_CURVE:
                            // Min motor curve does not have a control mode display. Control mode readout stays at whatever
                            // it was set to before.
                            frequency = pumpChannels.getMotorFrequency().value().orElse(0.0);
                            double minFrequency = pumpChannels.setFnom().value().orElse(0.0)
                                    * pumpChannels.setFmin().value().orElse(0.0);

                            // Need to allow some leeway since 8 bit conversion introduces inaccuracies.
                            frequencyOk = frequency > minFrequency - 1 && frequency < minFrequency + 1;
                            if (frequencyOk == false) {
                                changeControlMode();
                            }
                            break;
                        case MAX_MOTOR_CURVE:
                            // Max motor curve does not have a control mode display. Control mode readout stays at whatever
                            // it was set to before.
                            frequency = pumpChannels.getMotorFrequency().value().orElse(0.0);
                            maxFrequency = pumpChannels.setFnom().value().orElse(0.0);

                            // Need to allow some leeway since 8 bit conversion introduces inaccuracies.
                            frequencyOk = frequency > maxFrequency - 1 && frequency < maxFrequency + 1;
                            if (frequencyOk == false) {
                                changeControlMode();
                            }
                            break;
                        case AUTO_ADAPT:
                            if (pumpChannels.getActualControlMode().value().asEnum() != ControlMode.AUTO_ADAPT) {
                                changeControlMode();
                            }
                            break;
                    }

                    // Send setpoint to pump, depending on control mode. Do this every cycle.
                    switch (controlModeSetting) {
                        case MIN_MOTOR_CURVE:
                        case MAX_MOTOR_CURVE:
                        case AUTO_ADAPT:
                            break;
                        case CONST_FREQUENCY:
                            double minFrequencySetpoint = pumpChannels.setFmin().value().orElse(0.0);;
                            if (frequencySetpoint < minFrequencySetpoint) {
                                frequencySetpoint = minFrequencySetpoint;
                                setFrequencySetpoint().setNextWriteValue(frequencySetpoint);  // Update both containers to have correct values next cycle.
                                setFrequencySetpoint().setNextValue(frequencySetpoint);
                            }
                            if (frequencySetpoint > 100) {
                                frequencySetpoint = 100;
                                setFrequencySetpoint().setNextWriteValue(100.0);
                                setFrequencySetpoint().setNextValue(100.0);
                            }
                            setpoint = frequencySetpoint / 100.0;
                            this.pumpChannels.setRefRem().setNextWriteValue(setpoint);
                            break;
                        case CONST_PRESSURE:
                            double intervalHrange = pumpChannels.getPumpDevice().getPressureSensorRangeBar();
                            double intervalHmin = pumpChannels.getPumpDevice().getPressureSensorMinBar();

                            // Test if INFO of pressure sensor is available. If yes, range is not 0.
                            if (intervalHrange > 0) {
                                if (pressureSetpoint > intervalHrange + intervalHmin) {
                                    this.logWarn(this.log, "Value for pressure setpoint = " + pressureSetpoint + " bar is above the interval range. "
                                            + "Resetting to maximum valid value " + intervalHrange + intervalHmin + " bar.");
                                    pressureSetpoint = intervalHrange + intervalHmin;
                                    setPressureSetpoint().setNextWriteValue(pressureSetpoint);
                                    setPressureSetpoint().setNextValue(pressureSetpoint);   // Need to set this, otherwise warn message is displayed twice.
                                }
                                if (pressureSetpoint < intervalHmin) {
                                    this.logWarn(this.log, "Value for pressure setpoint = " + pressureSetpoint + " bar is below the interval range. "
                                            + "Resetting to minimum valid value " + intervalHmin + " bar.");
                                    pressureSetpoint = intervalHmin;
                                    setPressureSetpoint().setNextWriteValue(pressureSetpoint);
                                    setPressureSetpoint().setNextValue(pressureSetpoint);
                                }

                                // Don't need to convert to 0-254. The GENIbus bridge does that.
                                // ref_rem is a percentage value and you write the percentage in the channel. To send 100%, write 1.00
                                // to the channel.
                                setpoint = (pressureSetpoint - intervalHmin) / intervalHrange;
                                this.pumpChannels.setRefRem().setNextWriteValue(setpoint);
                            } else {
                                this.logWarn(this.log, "Can't send pressure setpoint to pump. INFO of pressure "
                                        + "sensor not yet available, but needed to calculate setpoint.");
                            }
                            break;
                    }
                    if (verbose) {
                        channelOutput();
                    }
                } else {

                }
            }
        } else {
            boolean pumpOnline = pumpChannels.isConnectionOk().value().isDefined() && pumpChannels.isConnectionOk().value().get();
            if (pumpOnline) {
                if (verbose) {
                    channelOutput();
                }
            } else {
                this.logWarn(this.log, "Warning: Pump " + pumpChannels.getPumpDevice().getPumpDeviceId()
                        + " at GENIbus address " + pumpChannels.getPumpDevice().getGenibusAddress() + " has no connection.");
            }
        }

    }

    private void channelOutput() {
        this.logInfo(this.log, "--Status of pump " + pumpChannels.getPumpDevice().getPumpDeviceId() + "--");
        this.logInfo(this.log, "GENIbus address: " + pumpChannels.getPumpDevice().getGenibusAddress()
                + ", product number: " + pumpChannels.getProductNumber().value().get() + ", "
                + "serial number: " + pumpChannels.getSerialNumber().value().get());
        this.logInfo(this.log, "Power consumption: " + formatter2.format(pumpChannels.getPowerConsumption().value().orElse(0.0)) + " W");
        this.logInfo(this.log, "Motor frequency: " + formatter2.format(pumpChannels.getMotorFrequency().value().orElse(0.0)) + " Hz or "
                + formatter2.format(pumpChannels.getMotorFrequency().value().orElse(0.0) * 60) + " rpm");
        this.logInfo(this.log, "Pump pressure: " + formatter2.format(pumpChannels.getCurrentPressure().value().orElse(0.0)) + " bar or "
                + formatter2.format(pumpChannels.getCurrentPressure().value().orElse(0.0) * 10) + " m");
        //this.logInfo(this.log, "Pump max pressure: " + formatter2.format(pumpChannels.setMaxPressure().value().orElse(0.0)) + " bar or " + formatter2.format(pumpChannels.setMaxPressure().value().orElse(0.0) * 10) + " m");
        this.logInfo(this.log, "Pump flow: " + formatter2.format(pumpChannels.getCurrentPumpFlow().value().orElse(0.0)) + " m³/h");
        //this.logInfo(this.log, "Pump flow max: " + formatter2.format(pumpChannels.setPumpMaxFlow().value().orElse(0.0)) + " m³/h");
        this.logInfo(this.log, "Pumped medium temperature: " + formatter1.format(pumpChannels.getPumpedWaterMediumTemperature().value().orElse(0.0) / 10) + "°C");
        this.logInfo(this.log, "Control mode: " + pumpChannels.getActualControlMode().value().asEnum().getName());
        this.logInfo(this.log, pumpChannels.getControlSource().value().orElse("Command source:"));
        //this.logInfo(this.log, "Buffer length: " + pumpChannels.getBufferLength().value().get());
        this.logInfo(this.log, "AlarmCode: " + pumpChannels.getAlarmCode().value().get());
        this.logInfo(this.log, "WarnCode: " + pumpChannels.getWarnCode().value().get());
        this.logInfo(this.log, "Warn message: " + pumpChannels.getWarnMessage().value().get());
        switch (controlModeSetting) {
            case MIN_MOTOR_CURVE:
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: min motor curve = "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)
                        * pumpChannels.setFmin().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)
                        * pumpChannels.setFmin().value().orElse(0.0) * 60) + " rpm.");
                this.logInfo(this.log, "");
                break;
            case MAX_MOTOR_CURVE:
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: max motor curve = "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0) * 60) + " rpm.");
                this.logInfo(this.log, "");
                break;
            case AUTO_ADAPT:
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: auto adapt");
                this.logInfo(this.log, "");
                break;
            case CONST_FREQUENCY:
                this.logInfo(this.log, "Actual setpoint (pump internal): " + formatter2.format(pumpChannels.getRefAct().value().orElse(0.0) * 100) + "% of interval range.");
                this.logInfo(this.log, "Maximum motor frequency: " + formatter2.format(pumpChannels.setFupper().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFupper().value().orElse(0.0) * 60) + " rpm");
                this.logInfo(this.log, "Motor frequency setpoint maximum: " + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0) * 60) + " rpm");
                this.logInfo(this.log, "Minimum pump speed: " + formatter2.format(pumpChannels.getRmin().value().orElse(0.0) * 100) + "% of maximum.");
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: " + frequencySetpoint + "% of " + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)) + " Hz.");
                this.logInfo(this.log, "");
                break;
            case CONST_PRESSURE:
                this.logInfo(this.log, "Actual setpoint (pump internal): " + formatter2.format(pumpChannels.getRefAct().value().orElse(0.0) * 100) + "% of interval range.");
                //this.logInfo(this.log, "Interval min (internal): " + formatter2.format(pumpChannels.getRmin().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
                //this.logInfo(this.log, "Interval max (internal): " + formatter2.format(pumpChannels.getRmax().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
                this.logInfo(this.log, "Maximum pressure setpoint: " + (pumpChannels.getPumpDevice().getPressureSensorMinBar()
                        + pumpChannels.getPumpDevice().getPressureSensorRangeBar()) + " bar / "
                        + (pumpChannels.getPumpDevice().getPressureSensorMinBar() + pumpChannels.getPumpDevice().getPressureSensorRangeBar()) * 10 + " m.");
                this.logInfo(this.log, "Minimum pressure setpoint: " + pumpChannels.getPumpDevice().getPressureSensorMinBar()
                        + " bar / " + pumpChannels.getPumpDevice().getPressureSensorMinBar() * 10 + " m.");
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: " + pressureSetpoint + " bar / " + pressureSetpoint * 10 + " m or "
                        + formatter2.format(pumpChannels.setRefRem().value().orElse(0.0) * 100) + "% of interval range.");
                this.logInfo(this.log, "");
                break;
        }

        /*
        this.logInfo(this.log, "Sensor:");
        this.logInfo(this.log, "ana_in_1_func: " + pumpChannels.setSensor1Func().value().get());
        this.logInfo(this.log, "ana_in_1_applic: " + pumpChannels.setSensor1Applic().value().get());
        this.logInfo(this.log, "ana_in_1_unit: " + pumpChannels.setSensor1Unit().value().get());
        this.logInfo(this.log, "ana_in_1_min: " + pumpChannels.setSensor1Min().value().get());
        this.logInfo(this.log, "ana_in_1_max: " + pumpChannels.setSensor1Max().value().get());

         */

        this.logInfo(this.log, "ref_norm: " + pumpChannels.getRefNorm().value().get());
        this.logInfo(this.log, "f_upper: " + pumpChannels.setFupper().value().get());
        this.logInfo(this.log, "f_nom: " + pumpChannels.setFnom().value().get());
        this.logInfo(this.log, "f_min: " + pumpChannels.setFmin().value().get());
        this.logInfo(this.log, "f_max: " + pumpChannels.setFmax().value().get());
        this.logInfo(this.log, "h_const_ref_min: " + pumpChannels.setConstRefMinH().value().get());
        this.logInfo(this.log, "h_const_ref_max: " + pumpChannels.setConstRefMaxH().value().get());
        this.logInfo(this.log, "h_range: " + pumpChannels.setHrange().value().get());
        this.logInfo(this.log, "ref_rem: " + pumpChannels.setRefRem().value().get());
        //this.logInfo(this.log, "ref_rem write: " + pumpChannels.setRefRem().getNextWriteValue().get());


        // Motor übertakten.
        /*
        try {
            pumpChannels.setFupper().setNextWriteValue(48.0);   // Motor max Frequenz
            pumpChannels.setFnom().setNextWriteValue(48.0);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }

         */


        // Just for testing
        /*
        if (testCounter == 10) {
            try {
                pumpChannels.setFnom().setNextWriteValue(40.0);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        }

        if (testCounter == 20) {
            try {
                pumpChannels.setFnom().setNextWriteValue(48.0);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        }

        testCounter++;
        */

    }
}
