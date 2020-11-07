package io.openems.edge.controller.heatnetwork.performancebooster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.heatnetwork.performancebooster.api.HeatnetworkPerformanceBooster;
import io.openems.edge.heater.api.Buffer;
import io.openems.edge.lucidcontrol.device.api.LucidControlDeviceOutput;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.module.signalsensor.api.SignalSensorSpi;
import io.openems.edge.temperature.passing.api.PassingActivateNature;
import io.openems.edge.temperature.passing.valve.api.Valve;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Heatnetwork.Performancebooster")
public class HeatnetworkPerformanceBoosterImpl extends AbstractOpenemsComponent implements OpenemsComponent, HeatnetworkPerformanceBooster, Controller, PassingActivateNature, Buffer {

    @Reference
    ComponentManager cpm;

    private List<Thermometer> thermometerList = new ArrayList<>();
    private List<SignalSensorSpi> heaterFallbackSignalSensors = new ArrayList<>();
    private List<SignalSensorSpi> heaterPrimarySignalSensors = new ArrayList<>();
    private List<LucidControlDeviceOutput> heaterControl = new ArrayList<>();
    private List<ActuatorRelaysChannel> heaterControlRelay = new ArrayList<>();
    private Valve heatMixer;
    private Thermometer referenceThermometer;
    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secondaryForward;
    private Thermometer secondaryRewind;
    private int deltaT;
    private boolean primaryForwardDefined;
    private boolean primaryRewindDefined;
    private boolean secondaryForwardDefined;
    private boolean secondaryRewindDefined;
    private boolean referenceDefined;
    private long sleepTime;
    private long activationTime = 0;


    public HeatnetworkPerformanceBoosterImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values(),
                HeatnetworkPerformanceBooster.ChannelId.values(),
                PassingActivateNature.ChannelId.values(),
                Buffer.ChannelId.values());

    }

    @Activate
    void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        this.temperatureSetPointMin().setNextValue(config.minTemp());
        this.temperatureSetPointMax().setNextValue(config.maxTemp());
        this.valveSetPointStandard().setNextValue(config.valvePercent());
        this.valveSetPointAddition().setNextValue(config.valvePercentAdditional());
        this.valveSetPointSubtraction().setNextValue(config.backUpPercentAdditionalHeater2Error());
        this.heaterSetPointStandard().setNextValue(config.backUpPercent());
        this.heaterSetPointAddition().setNextValue(config.backUpPercentAdditionalHeater1Error());
        this.temperatureSetPoint().setNextValue(config.activationTemp());
        this.storageLitreMax().setNextValue(config.litres());
        this.bufferSetPointMaxPercent().setNextValue(config.maxBufferThreshold());

        allocatePrimaryAndSecondary(config.primaryAndSecondary());
        allocateComponents(config.thermometer(), "Thermometer");
        allocateComponent(config.referenceThermometer(), "ref");
        allocateComponents(config.errorInputHeater1(), "Heater1");
        allocateComponents(config.backUpPercentHeater2Error(), "Heater2");
        allocateComponent(config.valve(), "Valve");
        allocateComponents(config.heaters(), "LucidOrRelay");

        this.getOnOff().setNextValue(false);
        this.deltaT = config.maxTemp() - config.minTemp();
        this.sleepTime = config.sleepTime() * 1000;

    }

    /**
     * Allocates Primary and Secondary Forward and Rewind.
     *
     * @param primaryAndSecondary Usually from Config; Contains TemperatureSensors for Primary/Secondary Forward/Rewind
     *                            <p>
     *                            Allocate TemperatureSensors to pF/pR/sF/sR.
     *                            </p>
     * @throws io.openems.common.exceptions.OpenemsError.OpenemsNamedException if there's something wrong with cpm at allocateComponent
     * @throws ConfigurationException                                          if the TemperatureSensor s wrong.
     */
    private void allocatePrimaryAndSecondary(String[] primaryAndSecondary) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        if (primaryAndSecondary.length > 0 && (primaryAndSecondary[0].equals("NotDefined") || primaryAndSecondary[0].equals(""))) {
            return;
        }
        for (int x = 0; x < primaryAndSecondary.length || x < 4; x++) {
            String identifier = "";
            switch (x) {
                case 0:
                    identifier = "pF";
                    break;
                case 1:
                    identifier = "pR";
                    break;
                case 2:
                    identifier = "sF";
                    break;
                case 3:
                    identifier = "sR";
                    break;
            }
            this.allocateComponent(primaryAndSecondary[x], identifier);
        }
    }

    /**
     * Allocate All Components depending on their type; Components usually from Config.
     *
     * @param components configured Components Usually from Config.
     * @param type       Identifier for Allocate Components. Coded in @Activate
     * @throws ConfigurationException                                          on wrong Component
     * @throws io.openems.common.exceptions.OpenemsError.OpenemsNamedException if somethings wrong with cpm.
     */
    private void allocateComponents(String[] components, String type) throws ConfigurationException, OpenemsError.OpenemsNamedException {
        ConfigurationException[] ex = {null};
        OpenemsError.OpenemsNamedException[] exNamed = {null};
        Arrays.stream(components).forEach(comp -> {
                    try {
                        switch (type) {
                            case "Thermometer":
                                if (cpm.getComponent(comp) instanceof Thermometer) {
                                    thermometerList.add(cpm.getComponent(comp));
                                } else {
                                    ex[0] = new ConfigurationException(comp, "Not a TemperatureSensor");
                                }
                                break;
                            case "Heater1":
                                if (cpm.getComponent(comp) instanceof SignalSensorSpi) {
                                    heaterPrimarySignalSensors.add(cpm.getComponent(comp));
                                } else {
                                    ex[0] = new ConfigurationException(comp, "Not A SignalSensor");
                                }
                                break;
                            case "Heater2":
                                if (cpm.getComponent(comp) instanceof SignalSensorSpi) {
                                    this.heaterFallbackSignalSensors.add(cpm.getComponent(comp));
                                } else {
                                    ex[0] = new ConfigurationException(comp, "Not A SignalSensor");
                                }
                                break;
                            case "LucidOrRelay":
                                if (cpm.getComponent(comp) instanceof LucidControlDeviceOutput) {
                                    this.heaterControl.add(cpm.getComponent(comp));
                                } else if (cpm.getComponent(comp) instanceof ActuatorRelaysChannel) {
                                    this.heaterControlRelay.add(cpm.getComponent(comp));
                                } else {
                                    ex[0] = new ConfigurationException(comp, "Not A LucidControlDevice");
                                }
                                break;
                            default:
                                ex[0] = new ConfigurationException("This shouldn't occur", "Shouldn't occure");
                        }
                    } catch (OpenemsError.OpenemsNamedException e) {
                        exNamed[0] = e;
                    }
                }
        );
        if (ex[0] != null) {
            throw ex[0];
        } else if (exNamed[0] != null) {
            throw exNamed[0];
        }
    }

    private void allocateComponent(String component, String identifier) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        if (component.equals("NotDefined") || component.equals("")) {
            return;
        }
        if (cpm.getComponent(component) instanceof Thermometer) {
            Thermometer th = cpm.getComponent(component);
            switch (identifier) {
                case "pF":
                    this.primaryForward = th;
                    this.primaryForwardDefined = true;
                    break;
                case "pR":
                    this.primaryRewind = th;
                    this.primaryRewindDefined = true;
                    break;
                case "sF":
                    this.secondaryForward = th;
                    this.secondaryForwardDefined = true;
                    break;
                case "sR":
                    this.secondaryRewind = th;
                    this.secondaryRewindDefined = true;
                    break;
                case "ref":
                    this.referenceThermometer = th;
                    this.referenceDefined = true;
                    break;
                default:
                    throw new ConfigurationException(component, "Not a Thermometer");
            }
        } else if (cpm.getComponent(component) instanceof Valve) {
            this.heatMixer = cpm.getComponent(component);
        } else {
            throw new ConfigurationException(component, "Not a correct Component");
        }


    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        averageTemperatureCalculation();
        assignCurrentTemperature();
        //Reference < SetPoint Temperature
        boolean shouldActivate = this.referenceThermometer.getTemperature().value().get() < this.temperatureSetPoint().value().get();
        boolean timeIsOver = this.sleepTime < (this.activationTime - System.currentTimeMillis());
        //next Value bc of averageTemperatureCalculation
        boolean shouldDeactivate = (this.storagePercent().getNextValue().get() <= this.bufferSetPointMaxPercent().value().get())
                && (this.referenceThermometer.getTemperature().value().get() > this.temperatureSetPointMax().value().get());
        this.getOnOff().setNextValue(shouldActivate);

        //Reference < SetPoint
        if (shouldActivate == true && timeIsOver == false) {

            //Init basic Percentage for Valve and FallbackHEater
            AtomicInteger percentIncreaseValve = new AtomicInteger(this.valveSetPointStandard().value().get());
            AtomicInteger percentIncreaseFallbackHeater = new AtomicInteger(this.heaterSetPointStandard().value().get());

            //Calculate Valve SetPoint and FallbackHeater SetPoints

            //Check if Primary Sensors got Error --> Increase Percentage Valve and 0-10V
            this.heaterPrimarySignalSensors.forEach(signalSensorSpi -> {
                if (signalSensorSpi.signalActive().value().get() == true) {
                    percentIncreaseValve.getAndAdd(this.valveSetPointAddition().value().get());
                    percentIncreaseFallbackHeater.getAndAdd(this.heaterSetPointAddition().value().get());
                }
            });
            //If FallbackHeater (e.g. Gasboiler) got an error --> Rewind Valve;
            this.heaterFallbackSignalSensors.forEach(signalSensorSpi -> {
                if (signalSensorSpi.signalActive().value().get() == true) {
                    percentIncreaseValve.getAndAdd(this.valveSetPointSubtraction().value().get());
                }
            });
            //Set Heatmixer e.g. Valve to calculated %
            this.heatMixer.changeByPercentage(percentIncreaseValve.get());
            //Set Each Lucid to Percentage and Relay to true
            this.heaterControl.forEach(lucid -> lucid.getPercentageChannel().setNextValue(percentIncreaseFallbackHeater.get()));
            this.heaterControlRelay.forEach(relay -> {
                try {
                    relay.getRelaysChannel().setNextWriteValue(true);
                } catch (OpenemsError.OpenemsNamedException e) {
                    e.printStackTrace();
                }
            });
            this.activationTime = System.currentTimeMillis();

        }


        if (shouldDeactivate == true || timeIsOver == true) {

            //Deactivate and force heatmixer e.g. Valve to close
            this.getOnOff().setNextValue(false);
            this.heatMixer.forceClose();
            //Set all Secondary / Fallback heater to 0 / deactivate them
            this.heaterControl.forEach(lucid -> {
                lucid.getPercentageChannel().setNextValue(0);
            });
            this.heaterControlRelay.forEach(relay -> {
                try {
                    relay.getRelaysChannel().setNextWriteValue(false);
                } catch (OpenemsError.OpenemsNamedException e) {
                    e.printStackTrace();
                }
            });
            this.activationTime = 0;
        }


    }

    private void assignCurrentTemperature() {
        if (primaryForwardDefined) {
            this.getPrimaryForward().setNextValue(this.primaryForward.getTemperature().value().get());
        }
        if (primaryRewindDefined) {
            this.getPrimaryRewind().setNextValue(this.primaryRewind.getTemperature().value().get());
        }
        if (secondaryForwardDefined) {
            this.getSecondaryForward().setNextValue(this.secondaryForward.getTemperature().value().get());
        }
        if (secondaryRewindDefined) {
            this.getSecondaryRewind().setNextValue(this.secondaryRewind.getTemperature().value().get());
        }
    }

    /**
     * Calculates Avg. Temperature depending on the TemperatureSensors.
     */
    private void averageTemperatureCalculation() {

        AtomicInteger tempAverage = new AtomicInteger(0);

        AtomicInteger minTemp = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger maxTemp = new AtomicInteger(Integer.MIN_VALUE);
        //Get Temperature of All Thermometer and Add to tempAverage; Also Check for Min and Max Temp
        if (this.thermometerList.size() > 0) {
            this.thermometerList.forEach(thermometer -> {
                if (thermometer.getTemperature().value().isDefined()) {
                    int temperature = thermometer.getTemperature().value().get();
                    tempAverage.getAndAdd(temperature);
                    if (temperature > maxTemp.get()) {
                        maxTemp.set(temperature);
                    }
                    if (temperature < minTemp.get()) {
                        minTemp.set(temperature);
                    }
                }
            });
            //Calculate averageTemperature; Set Min and Max Temperature
            tempAverage.set(tempAverage.get() / this.thermometerList.size());
            this.maxTemperature().setNextValue(maxTemp.get());
            this.minTemperature().setNextValue(minTemp.get());
        }
        //Set AverageTemperature to calculated temp.
        this.averageTemperature().setNextValue(tempAverage.get());
        // w / p = G/100
        int w = tempAverage.get() - this.temperatureSetPointMin().value().get();
        //can change during runtime
        deltaT = this.temperatureSetPointMax().value().get() - this.temperatureSetPointMin().value().get();
        //Calculate Performance etc
        int percentage = ((100 * w) / deltaT);
        this.storagePercent().setNextValue(percentage);
        this.storageEnergy().setNextValue(this.storageLitreMax().value().get() * deltaT * percentage);
        this.storageLitresCurrent().setNextValue(percentage * this.storageLitreMax().value().get() / 100);
    }
}
