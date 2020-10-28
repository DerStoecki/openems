package io.openems.edge.controller.multipleheatercombined;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.debug.detailedlog.DebugDetailedLog;
import io.openems.edge.heater.api.Heater;
import io.openems.edge.meter.heatmeter.api.HeatMeterMbus;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Designate(ocd = Config.class, factory = true)
@Component(name = "MultipleHeaterCombined",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class MultipleHeaterCombinedController extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

    private final Logger log = LoggerFactory.getLogger(DebugDetailedLog.class);
    @Reference
    protected ComponentManager cpm;


    private Heater heaterPrimary;
    private Heater heaterSecondary;
    private Heater heaterBackup;
    private Thermometer temperatureSensorHeater1Off;
    private Thermometer temperatureSensorHeater1On;
    private Thermometer temperatureSensorHeater2Off;
    private Thermometer temperatureSensorHeater2On;
    private Thermometer temperatureSensorHeater3Off;
    private Thermometer temperatureSensorHeater3On;

    private HeatMeterMbus heatMeter;
    //maximum number value (min Temperature --> Max Buffer)
    private float minTemperatureBufferValue;
    //minimum --> max Temperature --> min Buffer
    private float maxTemperatureBufferValue;
    private float bufferInBetweenFactor;
    private int bufferMinTemperature;
    private int bufferMaxTemperature;

    private int heaterPrimaryMax;
    private int heaterPrimaryMin;
    private int heaterSecondaryMax;
    private int heaterSecondaryMin;
    private int heaterBackupMax;
    private int heaterBackupMin;
    private float currentBuffer = 1.0f;


    public MultipleHeaterCombinedController() {

        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());

    }

    @Activate
    public void activate(ComponentContext context, Config config) {

        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponent(config.chp_Id(), "Heater", "Heater1");
        allocateComponent(config.woodChip_Id(), "Heater", "Heater2");
        allocateComponent(config.gasBoiler_Id(), "Heater", "Heater3");
        if (config.communicating_mbus()) {
            allocateComponent(config.heatMeter_Id(), "HeatMeterMbus", "");
        }
        allocateComponent(config.chp_TemperatureSensor_min(), "Temperature", "THeater_1_ḾIN");
        allocateComponent(config.chp_TemperatureSensor_max(), "Temperature", "THeater_1_ḾAX");

        allocateComponent(config.woodChip_TemperatureSensor_min(), "Temperature", "THeater_2_ḾIN");
        allocateComponent(config.woodChip_TemperatureSensor_max(), "Temperature", "THeater_2_ḾAX");

        allocateComponent(config.gasBoiler_TemperatureSensor_min(), "Temperature", "THeater_3_ḾIN");
        allocateComponent(config.gasBoiler_TemperatureSensor_max(), "Temperature", "THeater_3_ḾAX");

        this.minTemperatureBufferValue = config.minTemperatureBufferValue();
        this.maxTemperatureBufferValue = config.maxTemperatureBufferValue();
        this.bufferInBetweenFactor = config.inBetweenBufferValue();
        this.bufferMinTemperature = config.minTemperatureForBuffer();
        this.bufferMaxTemperature = config.maxTemperatureForBuffer();

        this.heaterPrimaryMax = config.chp_Temperature_max();
        this.heaterPrimaryMin = config.chp_Temperature_min();
        this.heaterSecondaryMax = config.woodChip_Temperature_max();
        this.heaterSecondaryMin = config.woodChip_Temperature_min();
        this.heaterBackupMax = config.gasBoiler_Temperature_max();
        this.heaterBackupMin = config.gasBoiler_Temperature_min();
    }

    private void allocateComponent(String device, String type, String concreteType) {

        try {
            switch (type) {
                case "Heater":
                    allocateHeater(device, concreteType);
                    break;
                case "Temperature":
                    allocateTemperatureSensor(device, concreteType);
                    break;

                case "HeatMeterMbus":
                    allocateHeatMeterMbus(device, concreteType);
            }


        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }


    }

    private void allocateHeatMeterMbus(String device, String concreteType) throws OpenemsError.OpenemsNamedException {
        if (cpm.getComponent(device) instanceof HeatMeterMbus) {
            this.heatMeter = cpm.getComponent(device);
        }
    }


    private void allocateHeater(String device, String concreteType) throws OpenemsError.OpenemsNamedException {

        switch (concreteType) {
            case "Heater1":
                if (cpm.getComponent(device) instanceof Heater) {
                    this.heaterPrimary = cpm.getComponent(device);
                }
                break;
            case "Heater2":
                if (cpm.getComponent(device) instanceof Heater) {
                    this.heaterSecondary = cpm.getComponent(device);
                }

                break;
            case "Heater3":
                if (cpm.getComponent(device) instanceof Heater) {
                    this.heaterBackup = cpm.getComponent(device);
                }
                break;
        }
    }

    private void allocateTemperatureSensor(String device, String concreteType) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        if (cpm.getComponent(device) instanceof Thermometer) {
            Thermometer th = cpm.getComponent(device);
            switch (concreteType) {
                case "THeater_1_MIN":
                    this.temperatureSensorHeater1On = th;
                    break;
                case "THeater_1_MAX":
                    this.temperatureSensorHeater1Off = th;
                    break;
                case "THeater_2_MIN":
                    this.temperatureSensorHeater2On = th;
                    break;
                case "THeater_2_MAX":
                    this.temperatureSensorHeater2Off = th;
                    break;
                case "THeater_3_MIN":
                    this.temperatureSensorHeater3On = th;
                    break;
                case "THeater_3_MAX":
                    this.temperatureSensorHeater3Off = th;
                    break;


            }
        } else {
            throw new ConfigurationException("The Device " + device
                    + " is not a TemperatureSensor", "Configuration is wrong of TemperatureSensor");
        }

    }

    /**
     * MultipleHeaterCombined logic to activate Primrary-->secondary--->Fallback Heater depending on demand.
     * <p>
     * Via AverageConsumption from the HeatMeter a performance demand is calculated.
     * Each Heater got a Temperature where they should activate and deactivate (Saving energy).
     * If the temperature threshold is met either deactivate (above max temperature) or activate (below min Temperature)
     * the Heater. In Order -> Primary --> secondary --> fallback.
     * In Addition a BufferValue is needed and calculated; depending on the Average Temperature in the System.
     * Meaning either More or Less Performance than needed is calculated/provided.
     * The Buffer Values are set via Config. As Well as all the TemperatureSensors Heaters etc etc.
     *
     * </p>
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        //in kW
        if (heatMeter.getAverageHourConsumption().getNextValue().isDefined()) {
            int thermicalPerformanceDemand = heatMeter.getAverageHourConsumption().getNextValue().get();
            this.currentBuffer = getCorrectBufferValue();
            if (this.temperatureSensorHeater1Off.getTemperature().getNextValue().get() > this.heaterPrimaryMax) {
                heaterPrimary.setOffline();
            } else if (this.temperatureSensorHeater1On.getTemperature().getNextValue().get() < this.heaterPrimaryMin) {
                thermicalPerformanceDemand -= this.heaterPrimary.calculateProvidedPower(thermicalPerformanceDemand, this.currentBuffer);
            }

            if (this.temperatureSensorHeater2Off.getTemperature().getNextValue().get() > this.heaterSecondaryMax || thermicalPerformanceDemand <= 0) {
                heaterSecondary.setOffline();

            } else if (this.temperatureSensorHeater2On.getTemperature().getNextValue().get() < this.heaterSecondaryMin) {

                thermicalPerformanceDemand -= this.heaterSecondary.calculateProvidedPower(thermicalPerformanceDemand, this.currentBuffer);
            }


            if (this.temperatureSensorHeater3Off.getTemperature().getNextValue().get() > this.heaterBackupMax || thermicalPerformanceDemand <= 0) {
                this.heaterBackup.setOffline();

            }
            if (this.temperatureSensorHeater3On.getTemperature().getNextValue().get() < this.heaterBackupMin) {
                thermicalPerformanceDemand -= this.heaterBackup.calculateProvidedPower(thermicalPerformanceDemand, this.currentBuffer);
            }

            if (thermicalPerformanceDemand > 0) {
                logInfo(this.log, "Performance demand that cannot be compensated: " + thermicalPerformanceDemand);
            }

        }

    }

    /**
     * <p>Calculates the Average Temperature of all T.Sensors and compares it to the threshold.
     * If its below the minTemperature --> A higher Buffer Value is placed;
     * Above MaxTemperature --> Lower Buffer Value is placed
     * Neither case --> inBetweenBufferFactor.
     * </p>
     */
    private float getCorrectBufferValue() {
        float averageTemperature = 0;
        if (temperatureSensorHeater1On.getTemperature().getNextValue().isDefined()) {
            averageTemperature += this.temperatureSensorHeater1On.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater1On.getTemperature().getNextValue().get() : 0;
            averageTemperature += this.temperatureSensorHeater1Off.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater1Off.getTemperature().getNextValue().get() : 0;
            averageTemperature += this.temperatureSensorHeater2On.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater2On.getTemperature().getNextValue().get() : 0;
            averageTemperature += this.temperatureSensorHeater2Off.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater2Off.getTemperature().getNextValue().get() : 0;
            averageTemperature += this.temperatureSensorHeater3On.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater3On.getTemperature().getNextValue().get() : 0;
            averageTemperature += this.temperatureSensorHeater3Off.getTemperature().getNextValue().isDefined() ? this.temperatureSensorHeater3Off.getTemperature().getNextValue().get() : 0;
            averageTemperature = averageTemperature / 6;

            if (averageTemperature >= bufferMaxTemperature) {
                return maxTemperatureBufferValue;
            } else if (averageTemperature <= bufferMinTemperature) {
                return minTemperatureBufferValue;
            } else {
                return bufferInBetweenFactor;
            }
        } else {
            return minTemperatureBufferValue;
        }
    }
}
