package io.openems.edge.controller.pump.grundfos;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.GenibusChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.pump.grundfos.api.PumpGrundfosControllerChannels;
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

    private final int bitRange = 254;
    private int maxPumpingHeadMeters;

    // Give these variables values to prevent null pointer exception in channelOutput()
    private double range = 10;
    private double intervalMin = 0;
    private double intervalMax = 10;
    private double setpoint = 0;

    private PumpGrundfosChannels pumpChannels;

    private boolean verbose;

    private boolean start;
    private boolean stop;
    private boolean remote;
    private boolean minMotorCurve;
    private boolean maxMotorCurve;
    private boolean constFrequency;
    private boolean constPressure;
    private boolean autoAdapt;

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
        maxPumpingHeadMeters = config.maxPumpingHead();
        if (maxPumpingHeadMeters < 2) {
            maxPumpingHeadMeters = 2;
            this.logError(this.log, "Pumping head setting unreasonably low (below 2 m). Resetting to 2 m. Check configuration!");
        }
        if (maxPumpingHeadMeters > 100) {
            maxPumpingHeadMeters = 100;
            this.logError(this.log, "Pumping head setting unreasonably high (above 100 m). Resetting to 100 m. Check configuration!");
        }

        try {
            sethIntervalMin().setNextWriteValue(config.hIntervalMin());
            sethIntervalMax().setNextWriteValue(config.hIntervalMax());
            setPumpingHead().setNextWriteValue(config.setpoint());
            this.pumpChannels.setConstPressure().setNextWriteValue(true);
            if (config.stopPump()) {
                this.pumpChannels.setStop().setNextWriteValue(true);
                this.pumpChannels.setStart().setNextWriteValue(false);
            } else {
                this.pumpChannels.setStart().setNextWriteValue(true);
                this.pumpChannels.setStop().setNextWriteValue(false);
            }

        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }

        verbose = config.printPumpStatus();


        /*
        String[] commands = config.commands();
        Arrays.stream(commands).forEach(string -> {
            switch (string) {
                case "Remote":
                    this.remote = true;
                    break;
                case "Start":
                    this.start = true;
                    break;
                case "Stop":
                    this.stop = true;
                    break;
                case "MinMotorCurve":
                    this.minMotorCurve = true;
                    break;
                case "MaxMotorCurve":
                    this.maxMotorCurve = true;
                    break;
                case "ConstFrequency":
                    this.constFrequency = true;
                    break;
                case "ConstPressure":
                    this.constPressure = true;
                    break;
                case "AutoAdapt":
                    this.autoAdapt = true;
                    break;
            }
        });
        */

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    /**
     * Gets the Commands usually from config; or REST/JSON Request and writes ReferenceValues in channels.
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        // Puts the pump in remote mode. Send every second.
        this.pumpChannels.setRemote().setNextWriteValue(true);

        /*
        if (this.remote) {
            this.autoAdapt = false;
        }
        this.pumpChannels.setAutoAdapt().setNextWriteValue(this.autoAdapt);
        this.pumpChannels.setStart().setNextWriteValue(this.start);
        this.pumpChannels.setStop().setNextWriteValue(this.stop);
        this.pumpChannels.setMaxMotorCurve().setNextWriteValue(this.maxMotorCurve);
        this.pumpChannels.setMinMotorCurve().setNextWriteValue(this.minMotorCurve);
        this.pumpChannels.setConstFrequency().setNextWriteValue(this.constFrequency);
        this.pumpChannels.setConstPressure().setNextWriteValue(this.constPressure);
        this.pumpChannels.setRemote().setNextWriteValue(this.remote);
        */

        // Fetch write values from channels and put them in the correct containers.
        boolean writeChannelsHaveValues = sethIntervalMin().getNextWriteValue().isPresent() && sethIntervalMax().getNextWriteValue().isPresent()
                && setPumpingHead().getNextWriteValue().isPresent();
        if (writeChannelsHaveValues) {
            sethIntervalMin().setNextValue(sethIntervalMin().getNextWriteValue().get());
            sethIntervalMax().setNextValue(sethIntervalMax().getNextWriteValue().get());
            setPumpingHead().setNextValue(setPumpingHead().getNextWriteValue().get());
        }

        boolean channelsHaveValues = sethIntervalMin().value().isDefined() && sethIntervalMax().value().isDefined() &&
                setPumpingHead().value().isDefined();
        if (channelsHaveValues) {
            intervalMin = sethIntervalMin().value().get();
            intervalMax = sethIntervalMax().value().get();
            setpoint = setPumpingHead().value().get();

            // Check validity of values. Check every cycle, as new values can come from Json/Rest at any time.
            if (intervalMax > maxPumpingHeadMeters || intervalMax <= 0) {
                intervalMax = maxPumpingHeadMeters;
                sethIntervalMax().setNextWriteValue(intervalMax);
                this.logError(this.log, "Bad value for interval max setting. Resetting to maximum valid value " + maxPumpingHeadMeters + ".");
            }
            if (intervalMin > intervalMax || intervalMin < 0) {
                intervalMin = 0;
                sethIntervalMin().setNextWriteValue(intervalMin);
                this.logError(this.log, "Bad value for interval min setting. Resetting to 0.");
            }
            if (setpoint > intervalMax || setpoint < intervalMin) {
                setpoint = intervalMax;
                setPumpingHead().setNextWriteValue(setpoint);
                this.logError(this.log, "Value for pumping head setpoint (Förderhöhe) outside of interval range. "
                        + "Resetting to maximum valid value " + intervalMax + ".");
            }
            range = intervalMax - intervalMin;
            double byteValueRefRem = Math.round((bitRange / range)*(setpoint - intervalMin));
            double byteValueHmin = Math.round((intervalMin / maxPumpingHeadMeters) * bitRange);
            double byteValueHmax = Math.round((intervalMax / maxPumpingHeadMeters) * bitRange);
            this.pumpChannels.setConstRefMinH().setNextWriteValue(byteValueHmin);
            this.pumpChannels.setConstRefMaxH().setNextWriteValue(byteValueHmax);
            this.pumpChannels.setRefRem().setNextWriteValue(byteValueRefRem);
        }

        if (verbose) {
            channelOutput();
        }
    }

    private void channelOutput() {
        this.logInfo(this.log, "--Pump status--");
        this.logInfo(this.log, "Power consumption: " + formatter2.format(pumpChannels.getPowerConsumption().value().orElse(0.0)) + " W");
        this.logInfo(this.log, "Motor frequency: " + formatter2.format(pumpChannels.getMotorFrequency().value().orElse(0.0)) + " Hz");
        this.logInfo(this.log, "Pump pressure: " + formatter2.format(pumpChannels.getCurrentPressure().value().orElse(0.0)) + " bar or "
                + formatter2.format(pumpChannels.getCurrentPressure().value().orElse(0.0) * 10) + " m");
        this.logInfo(this.log, "Pump flow: " + formatter2.format(pumpChannels.getCurrentPumpFlow().value().orElse(0.0)) + " m³/h");
        this.logInfo(this.log, "Pumped medium temperature: " + formatter1.format(pumpChannels.getPumpedWaterMediumTemperature().value().orElse(0.0) / 10) + "°C");
        this.logInfo(this.log, "Control mode: " + pumpChannels.getActualControlMode().value().asEnum().getName());
        this.logInfo(this.log, pumpChannels.getControlSource().value().orElse("Command source:"));
        this.logInfo(this.log, "AlarmCode: " + pumpChannels.getAlarmCode().value().get());
        this.logInfo(this.log, "WarnCode: " + pumpChannels.getWarnCode().value().get());
        this.logInfo(this.log, "Warn message: " + pumpChannels.getWarnMessage().value().get());
        this.logInfo(this.log, "Actual setpoint (pump internal): " + formatter2.format(pumpChannels.getRefAct().value().orElse(0.0) * 100) + "% of interval range.");
        this.logInfo(this.log, "Interval min (internal): " + formatter2.format(pumpChannels.getRmin().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
        this.logInfo(this.log, "Interval max (internal): " + formatter2.format(pumpChannels.getRmax().value().orElse(0.0) * 100) + "% of maximum pumping head (Förderhöhe).");
        this.logInfo(this.log, "");
        this.logInfo(this.log, "Controller setpoint: " + setpoint + " m or " + formatter2.format((100.0 / range)*(setpoint - intervalMin)) + "% of interval range.");
        this.logInfo(this.log, "");

    }
}
