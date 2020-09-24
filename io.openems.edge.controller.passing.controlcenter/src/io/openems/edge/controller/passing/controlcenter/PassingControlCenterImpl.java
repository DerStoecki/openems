package io.openems.edge.controller.passing.controlcenter;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.controller.passing.controlcenter.api.PassingControlCenterChannel;
import io.openems.edge.controller.passing.heatingcurveregulator.api.HeatingCurveRegulatorChannel;
import io.openems.edge.controller.pid.passing.api.PidForPassingNature;
import io.openems.edge.controller.warmup.passing.api.ControllerWarmupChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Consolinno Passing Control Center module. It manages the hierarchy of heating controllers.
 * - The output of a heating controller is a temperature and a boolean to signal if the controller wants to heat or not.
 * - This controller polls three heating controllers by hierarchy and passes on the result (heating or not heating plus
 * the temperature) to the next module(s).
 */

@Designate(ocd = Config.class, factory = true)
@Component(name = "PassingControlCenter", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PassingControlCenterImpl extends AbstractOpenemsComponent implements OpenemsComponent, PassingControlCenterChannel, Controller {

    private final Logger log = LoggerFactory.getLogger(PassingControlCenterImpl.class);

    @Reference
    protected ComponentManager cpm;

    private PidForPassingNature pidControllerChannel;
    private ControllerWarmupChannel warmupControllerChannel;
    private HeatingCurveRegulatorChannel heatingCurveRegulatorChannel;
    private ActuatorRelaysChannel pump;

    // Variables for channel readout
    private boolean activateTemperatureOverride;
    private int overrideTemperature;
    private boolean warmupControllerIsOn;
    private boolean warmupControllerNoError;
    private int warmupControllerTemperature;
    private boolean heatingCurveRegulatorAskingForHeating;
    private boolean heatingCurveRegulatorNoError;
    private int heatingCurveRegulatorTemperature;


    public PassingControlCenterImpl() {
        super(OpenemsComponent.ChannelId.values(),
                PassingControlCenterChannel.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        //allocate components
        if (cpm.getComponent(config.allocated_Pid_Controller()) instanceof PidForPassingNature) {
            pidControllerChannel = cpm.getComponent(config.allocated_Pid_Controller());
        } else {
            throw new ConfigurationException(config.allocated_Pid_Controller(),
                    "Allocated Passing Controller not a Pid for Passing Controller; Check if Name is correct and try again.");
        }
        if (cpm.getComponent(config.allocated_Warmup_Controller()) instanceof ControllerWarmupChannel) {
            this.warmupControllerChannel = cpm.getComponent(config.allocated_Warmup_Controller());
        } else {
            throw new ConfigurationException(config.allocated_Warmup_Controller(),
                    "Allocated Warmup Controller not a WarmupPassing Controller; Check if Name is correct and try again.");
        }
        if (cpm.getComponent(config.allocated_Heating_Curve_Regulator()) instanceof HeatingCurveRegulatorChannel) {
            this.heatingCurveRegulatorChannel = cpm.getComponent(config.allocated_Heating_Curve_Regulator());
        } else {
            throw new ConfigurationException(config.allocated_Warmup_Controller(),
                    "Allocated Heating Controller not a Heating Curve Regulator; Check if Name is correct and try again.");
        }
        if (cpm.getComponent(config.allocated_Pump()) instanceof ActuatorRelaysChannel) {
            this.pump = cpm.getComponent(config.allocated_Pump());
        } else {
            throw new ConfigurationException(config.allocated_Warmup_Controller(),
                    "Allocated Heating Controller not a Heating Curve Regulator; Check if Name is correct and try again.");
        }

        this.activateHeater().setNextValue(false);

        // This allows to start the warmupController from this module.
        if (config.run_warmup_program()) {
            warmupControllerChannel.playPauseWarmupController().setNextWriteValue(true);
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        // For the Overrides, copy values from the WriteValue to the NextValue fields.
        if (this.activateTemperatureOverride().getNextWriteValue().isPresent()) {
            this.activateTemperatureOverride().setNextValue(this.activateTemperatureOverride().getNextWriteValue().get());
        }
        if (this.setOverrideTemperature().getNextWriteValue().isPresent()) {
            this.setOverrideTemperature().setNextValue(this.setOverrideTemperature().getNextWriteValue().get());
        }

        // Check all channels if they have values in them.
        boolean overrideChannelHasValues = this.activateTemperatureOverride().value().isDefined()
                && this.setOverrideTemperature().value().isDefined();

        boolean warmupControllerChannelHasValues = warmupControllerChannel.playPauseWarmupController().value().isDefined()
                && warmupControllerChannel.getWarmupTemperature().value().isDefined()
                && warmupControllerChannel.noError().value().isDefined();

        boolean heatingCurveRegulatorChannelHasValues = heatingCurveRegulatorChannel.signalTurnOnHeater().value().isDefined()
                && heatingCurveRegulatorChannel.getHeatingTemperature().value().isDefined()
                && heatingCurveRegulatorChannel.noError().value().isDefined();

        // Transfer channel data to local variables for better readability of logic code.
        if (overrideChannelHasValues) {
            activateTemperatureOverride = this.activateTemperatureOverride().value().get();
            overrideTemperature = this.setOverrideTemperature().value().get();
        }
        if (warmupControllerChannelHasValues) {
            warmupControllerIsOn = warmupControllerChannel.playPauseWarmupController().value().get();
            warmupControllerNoError = warmupControllerChannel.noError().value().get();
            warmupControllerTemperature = warmupControllerChannel.getWarmupTemperature().value().get();
        }
        if (heatingCurveRegulatorChannelHasValues) {
            // The HeatingCurveRegulator is asking for heating based on outside temperature. Heating in winter, no
            // heating in summer.
            heatingCurveRegulatorAskingForHeating = heatingCurveRegulatorChannel.signalTurnOnHeater().value().get();
            heatingCurveRegulatorNoError = heatingCurveRegulatorChannel.noError().value().get();
            heatingCurveRegulatorTemperature = heatingCurveRegulatorChannel.getHeatingTemperature().value().get();
        }

        // Control logic. Execute controllers by priority. From high to low: override, warmup, heatingCurve
        if (overrideChannelHasValues && activateTemperatureOverride) {
            turnOnHeater(overrideTemperature);
        } else if (warmupControllerChannelHasValues && warmupControllerIsOn && warmupControllerNoError) {
            turnOnHeater(warmupControllerTemperature);
        } else if (heatingCurveRegulatorChannelHasValues && heatingCurveRegulatorAskingForHeating
                && heatingCurveRegulatorNoError) {
            turnOnHeater(heatingCurveRegulatorTemperature);
        } else {
            turnOffHeater();
        }
    }


    private void turnOnHeater(int temperatureInDezidegree) throws OpenemsError.OpenemsNamedException {
        this.activateHeater().setNextValue(true);
        pidControllerChannel.turnOn().setNextWriteValue(true);
        pidControllerChannel.setMinTemperature().setNextWriteValue(temperatureInDezidegree);
        pump.getRelaysChannel().setNextWriteValue(true);
    }

    private void turnOffHeater() throws OpenemsError.OpenemsNamedException {
        this.activateHeater().setNextValue(false);
        pidControllerChannel.turnOn().setNextWriteValue(false);
        pump.getRelaysChannel().setNextWriteValue(false);
    }

}
