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
        try {
            // Fill all containers of the channels with values. This is needed since "run()" tests the "value" container
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

            changeControlMode();
            startStopPump();
            pumpFlashLed();
            sendSetpoint();

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

    private void sendSetpoint() throws OpenemsError.OpenemsNamedException {
        switch (controlModeSetting) {
            case MIN_MOTOR_CURVE:
            case MAX_MOTOR_CURVE:
            case AUTO_ADAPT:
                break;
            case CONST_FREQUENCY:
                frequencySetpoint = setFrequencySetpoint().value().get();
                if (frequencySetpoint < 0) {
                    frequencySetpoint = 0;
                    setFrequencySetpoint().setNextWriteValue(0.0);  // Update both containers to have correct values next cycle.
                    setFrequencySetpoint().setNextValue(0.0);
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
                pressureSetpoint = setPressureSetpoint().value().get();
                double intervalHrange = pumpChannels.getPumpDevice().getPressureSensorRangeBar();
                double intervalHmin = pumpChannels.getPumpDevice().getPressureSensorMinBar();

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
                break;
        }
    }

    /**
     * Gets the Commands usually from config; or REST/JSON Request and writes ReferenceValues in channels.
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        // Puts the pump in remote mode. Send every second.
        this.pumpChannels.setRemote().setNextWriteValue(true);

        // Copy "write" to "nextValue".
        boolean writeChannelsHaveValues = setControlMode().getNextWriteValue().isPresent()
                && setStopPump().getNextWriteValue().isPresent()
                && setFlashLed().getNextWriteValue().isPresent()
                && setPressureSetpoint().getNextWriteValue().isPresent()
                && setFrequencySetpoint().getNextWriteValue().isPresent();
        if (writeChannelsHaveValues) {
            setControlMode().setNextValue(setControlMode().getNextWriteValue().get());
            setStopPump().setNextValue(setStopPump().getNextWriteValue().get());
            setFlashLed().setNextValue(setFlashLed().getNextWriteValue().get());
            setPressureSetpoint().setNextValue(setPressureSetpoint().getNextWriteValue().get());
            setFrequencySetpoint().setNextValue(setFrequencySetpoint().getNextWriteValue().get());
        }

        boolean channelsHaveValues =  setControlMode().value().isDefined()
                && setStopPump().value().isDefined()
                && setFlashLed().value().isDefined()
                && setPressureSetpoint().value().isDefined()
                && setFrequencySetpoint().value().isDefined()
                && pumpChannels.isConnectionOk().value().isDefined();
        if (channelsHaveValues) {

            // In case of a connection loss, all commands and configuration values need to be sent again.
            // Because connection loss can also mean pump was turned off and restarted, or it may even be a different
            // pump at this address.
            if (pumpChannels.isConnectionOk().value().get()) {

                // Compare pump status with controller settings. Resend commands if there is a difference.
                if (stopPump) {
                    if (pumpChannels.getMotorFrequency().value().orElse(0.0) > 0) {
                        this.logInfo(this.log, "Pump should be stopped, but it is not.");
                        startStopPump();
                    }
                } else {
                    if (pumpChannels.getMotorFrequency().value().orElse(0.0) <= 0) {
                        this.logInfo(this.log, "Pump should be running, but it is not.");
                        startStopPump();
                    }
                }
                switch (controlModeSetting) {
                    case CONST_PRESSURE:
                        if (pumpChannels.getActualControlMode().value().asEnum() != ControlMode.CONST_PRESS) {
                            changeControlMode();
                            this.logInfo(this.log, "Pump not in mode that it should be.");
                        }
                        break;
                    case CONST_FREQUENCY:
                    case MIN_MOTOR_CURVE:
                    case MAX_MOTOR_CURVE:
                        if (pumpChannels.getActualControlMode().value().asEnum() != ControlMode.CONST_FREQ) {
                            changeControlMode();
                            this.logInfo(this.log, "Pump not in mode that it should be.");
                        }
                        break;
                    case AUTO_ADAPT:
                        if (pumpChannels.getActualControlMode().value().asEnum() != ControlMode.AUTO_ADAPT) {
                            changeControlMode();
                            this.logInfo(this.log, "Pump not in mode that it should be.");
                        }
                        break;
                }

                // Check if values in channels have changed. In case there is a REST/JSON write to a channel.
                if (stopPump != setStopPump().value().get()) {
                    stopPump = setStopPump().value().get();
                    startStopPump();
                }
                if (controlModeSetting != setControlMode().value().asEnum()) {
                    controlModeSetting = setControlMode().value().asEnum();
                    changeControlMode();
                }
                if (pumpWink != setFlashLed().value().get()) {
                    pumpWink = setFlashLed().value().get();
                    pumpFlashLed();
                }
                if (pumpWink) { // Resend command in case pump was turned off.
                    pumpFlashLed();
                }

                // Send setpoint to pump, depending on control mode.
                switch (controlModeSetting) {
                    case MIN_MOTOR_CURVE:
                    case MAX_MOTOR_CURVE:
                    case AUTO_ADAPT:
                        break;
                    case CONST_FREQUENCY:
                        if (setpoint != setFrequencySetpoint().value().get()) {
                            setpoint = setFrequencySetpoint().value().get();
                            if (setpoint < 0) {
                                setpoint = 0;
                                setFrequencySetpoint().setNextWriteValue(0.0);  // Update both containers to have correct values next cycle.
                                setFrequencySetpoint().setNextValue(0.0);
                            }
                            if (setpoint > 100) {
                                setpoint = 100;
                                setFrequencySetpoint().setNextWriteValue(100.0);
                                setFrequencySetpoint().setNextValue(100.0);
                            }
                            this.pumpChannels.setRefRem().setNextWriteValue(setpoint / 100.0);
                        }
                        break;
                    case CONST_PRESSURE:
                        if (setpoint != setPressureSetpoint().value().get()) {
                            setpoint = setPressureSetpoint().value().get();
                            double intervalHrange = pumpChannels.getPumpDevice().getPressureSensorRangeBar();
                            double intervalHmin = pumpChannels.getPumpDevice().getPressureSensorMinBar();

                            if (setpoint > intervalHrange + intervalHmin) {
                                this.logWarn(this.log, "Value for pressure setpoint = " + setpoint + " bar is above the interval range. "
                                        + "Resetting to maximum valid value " + intervalHrange + intervalHmin + " bar.");
                                setpoint = intervalHrange + intervalHmin;
                                setPressureSetpoint().setNextWriteValue(setpoint);
                                setPressureSetpoint().setNextValue(setpoint);   // Need to set this, otherwise warn message is displayed twice.
                            }
                            if (setpoint < intervalHmin) {
                                this.logWarn(this.log, "Value for pressure setpoint = " + setpoint + " bar is below the interval range. "
                                        + "Resetting to minimum valid value " + intervalHmin + " bar.");
                                setpoint = intervalHmin;
                                setPressureSetpoint().setNextWriteValue(setpoint);
                                setPressureSetpoint().setNextValue(setpoint);
                            }

                            // Don't need to convert to 0-254. The GENIbus bridge does that.
                            // ref_rem is a percentage value and you write the percentage in the channel. To send 100%, write 1.00
                            // to the channel.
                            double percentRefRem = (setpoint - intervalHmin) / intervalHrange;
                            this.pumpChannels.setRefRem().setNextWriteValue(percentRefRem);
                        }
                        break;
                }
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
        this.logInfo(this.log, "Actual setpoint (pump internal): " + formatter2.format(pumpChannels.getRefAct().value().orElse(0.0) * 100) + "% of interval range.");
        switch (controlModeSetting) {
            case MIN_MOTOR_CURVE:
            case MAX_MOTOR_CURVE:
            case AUTO_ADAPT:
                break;
            case CONST_FREQUENCY:
                this.logInfo(this.log, "Maximum motor frequency: " + formatter2.format(pumpChannels.setFupper().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFupper().value().orElse(0.0) * 60) + " rpm");
                this.logInfo(this.log, "Motor frequency setpoint maximum: " + formatter2.format(pumpChannels.setFnom().value().orElse(0.0)) + " Hz or "
                        + formatter2.format(pumpChannels.setFnom().value().orElse(0.0) * 60) + " rpm");
                this.logInfo(this.log, "Minimum pump speed: " + formatter2.format(pumpChannels.getRmin().value().orElse(0.0) * 100) + "% of maximum.");
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: " + setpoint + "% of setpoint maximum.");
                this.logInfo(this.log, "");
                break;
            case CONST_PRESSURE:
                //this.logInfo(this.log, "Interval min (internal): " + formatter2.format(pumpChannels.getRmin().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
                //this.logInfo(this.log, "Interval max (internal): " + formatter2.format(pumpChannels.getRmax().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
                this.logInfo(this.log, "Maximum pressure setpoint: " + (pumpChannels.getPumpDevice().getPressureSensorMinBar()
                        + pumpChannels.getPumpDevice().getPressureSensorRangeBar()) + " bar / "
                        + (pumpChannels.getPumpDevice().getPressureSensorMinBar() + pumpChannels.getPumpDevice().getPressureSensorRangeBar()) * 10 + " m.");
                this.logInfo(this.log, "Minimum pressure setpoint: " + pumpChannels.getPumpDevice().getPressureSensorMinBar()
                        + " bar / " + pumpChannels.getPumpDevice().getPressureSensorMinBar() * 10 + " m.");
                this.logInfo(this.log, "");
                this.logInfo(this.log, "Controller setpoint: " + setpoint + " bar / " + setpoint * 10 + " m or "
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


    }
}
