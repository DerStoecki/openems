package io.openems.edge.controller.multipleheatercombined;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.biomassheater.api.BioMassHeater;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.gasboiler.device.api.GasBoilerData;
import io.openems.edge.heatmeter.api.HeatMeterMbus;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

public class MultipleHeaterCombinedController extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

    @Reference
    protected ComponentManager cpm;


    private PowerLevel chp;
    private BioMassHeater bioMassHeater;
    private GasBoilerData gasBoiler;
    private Thermometer temperatureSensorHeater1Off;
    private Thermometer temperatureSensorHeater1On;
    private Thermometer temperatureSensorHeater2Off;
    private Thermometer temperatureSensorHeater2On;
    private Thermometer temperatureSensorHeater3Off;
    private Thermometer temperatureSensorHeater3On;

    private HeatMeterMbus heatMeter;

    private double minTemperatureBufferValue;
    private double maxTemperatureBufferValue;
    private double bufferInBetweenFactor;
    private int bufferMinTemperature;
    private int bufferMaxTemperature;

    private int chpTemperatureMax;
    private int chpTemperatureMin;
    private int woodChipTemperatureMax;
    private int woodChipTemperatureMin;
    private int gasBoilerTemperatureMax;
    private int gasBoilerTemperatureMin;
    private int maxGasBoilerPower;
    private int maxWoodChipPower;
    private int maxChpWarmPower;


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

        this.chpTemperatureMax = config.chp_Temperature_max();
        this.chpTemperatureMin = config.chp_Temperature_min();
        this.woodChipTemperatureMax = config.woodChip_Temperature_max();
        this.woodChipTemperatureMin = config.woodChip_Temperature_min();
        this.gasBoilerTemperatureMax = config.gasBoiler_Temperature_max();
        this.gasBoilerTemperatureMin = config.gasBoiler_Temperature_min();
        this.maxChpWarmPower = config.heater_1_max_performance();
        this.maxWoodChipPower = config.heater_2_max_performance();
        this.maxGasBoilerPower = config.heater_3_max_performance();


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

    // gonna be easier in future (when Heater becomes an interface
    private void allocateHeater(String device, String concreteType) throws OpenemsError.OpenemsNamedException {

        switch (concreteType) {
            case "Heater1":
                if (cpm.getComponent(device) instanceof PowerLevel) {
                    this.chp = cpm.getComponent(device);
                }
                break;
            case "Heater2":
                if (cpm.getComponent(device) instanceof BioMassHeater) {
                    this.bioMassHeater = cpm.getComponent(device);
                }

                break;
            case "Heater3":
                if (cpm.getComponent(device) instanceof GasBoilerData) {
                    this.gasBoiler = cpm.getComponent(device);
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

    /*
     * In Future --> Set bhkw not to 100% but a certain value --> Depending on Kind of chp
     * */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        float performanceNeededByGasBoiler = 0;

        if (this.temperatureSensorHeater1Off.getTemperature().getNextValue().get() > this.chpTemperatureMax) {
            this.chp.getPowerLevelChannel().setNextWriteValue(0);
        } else if (this.temperatureSensorHeater1On.getTemperature().getNextValue().get() < this.chpTemperatureMin) {
            this.chp.getPowerLevelChannel().setNextWriteValue(100);
        }
        if (this.temperatureSensorHeater2Off.getTemperature().getNextValue().get() > this.woodChipTemperatureMax) {
            //TODO SET OFF!!!

        } else if (this.temperatureSensorHeater2On.getTemperature().getNextValue().get() < this.woodChipTemperatureMin) {
            // TODO SET WOODCHIP ON and calc performance via WMZ and CHP
            float performanceNeededByWoodChip = (getHeatMeterAverageConsumption() - maxChpWarmPower) * getCorrectBufferValue();
            performanceNeededByGasBoiler = performanceNeededByWoodChip - maxWoodChipPower;
            //SET WoodChip Power ---> correct ch?
            float percentValue = performanceNeededByWoodChip / maxWoodChipPower;
            this.bioMassHeater.getSlideInPercentageValue().setNextWriteValue((int) percentValue);
        }
        if (this.temperatureSensorHeater3Off.getTemperature().getNextValue().get() > this.gasBoilerTemperatureMax) {
            this.gasBoiler.getHeatBoilerPerformanceSetPointValue().setNextWriteValue(0);

        }
        if (this.temperatureSensorHeater3On.getTemperature().getNextValue().get() < this.gasBoilerTemperatureMin) {
            float calculatedPower = calculateGasBoilerPower(performanceNeededByGasBoiler);
            if (calculatedPower > 0) {
                this.gasBoiler.getHeatBoilerPerformanceSetPointValuePercent().setNextValue(calculatedPower);
                this.gasBoiler.getHeatBoilerPerformanceSetPointValue().setNextWriteValue((int) (calculatedPower * 2));
            }

        }

    }

    private double getCorrectBufferValue() {
        double averageTemperature = 0;
        averageTemperature += this.temperatureSensorHeater1On.getTemperature().getNextValue().get();
        averageTemperature += this.temperatureSensorHeater1Off.getTemperature().getNextValue().get();
        averageTemperature += this.temperatureSensorHeater2On.getTemperature().getNextValue().get();
        averageTemperature += this.temperatureSensorHeater2Off.getTemperature().getNextValue().get();
        averageTemperature += this.temperatureSensorHeater3On.getTemperature().getNextValue().get();
        averageTemperature += this.temperatureSensorHeater3Off.getTemperature().getNextValue().get();
        averageTemperature = averageTemperature / 6;

        if (averageTemperature >= bufferMaxTemperature) {
            return maxTemperatureBufferValue;
        } else if (averageTemperature <= bufferMinTemperature) {
            return minTemperatureBufferValue;
        } else {
            return bufferInBetweenFactor;
        }
    }

    private float calculateGasBoilerPower(float performanceNeeded) {
        return performanceNeeded / maxGasBoilerPower;
    }
}
