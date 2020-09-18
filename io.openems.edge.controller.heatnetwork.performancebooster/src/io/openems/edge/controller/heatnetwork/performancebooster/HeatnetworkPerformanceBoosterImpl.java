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
        this.valveSetPointSubtraction().setNextValue(config.backUpPercentHeater2Error());
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

    }

    private void allocatePrimaryAndSecondary(String[] primaryAndSecondary) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        for (int x = 0; x < primaryAndSecondary.length; x++) {
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
        if (cpm.getComponent(component) instanceof Thermometer) {
            Thermometer th = cpm.getComponent(component);
            switch (identifier) {
                case "pF":
                    this.primaryForward = th;
                    break;
                case "pR":
                    this.primaryRewind = th;
                    break;
                case "sF":
                    this.secondaryForward = th;
                    break;
                case "sR":
                    this.secondaryRewind = th;
                    break;
                case "ref":
                    this.referenceThermometer = th;
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
        boolean shouldActivate = this.referenceThermometer.getTemperature().value().get() < this.temperatureSetPoint().value().get();
        //next Value bc of averageTemperatureCalculation
        boolean shouldDeactivate = (this.storagePercent().getNextValue().get() <= this.bufferSetPointMaxPercent().value().get())
                && (this.referenceThermometer.getTemperature().value().get() > this.temperatureSetPointMax().value().get());
        this.getOnOff().setNextValue(shouldActivate);
        if (shouldActivate == true) {

            //open/close Valve if there are error signals
            AtomicInteger percentIncreaseValve = new AtomicInteger(this.valveSetPointStandard().value().get());
            AtomicInteger percentIncreaseFallbackHeater = new AtomicInteger(this.heaterSetPointStandard().value().get());
            //primaryheatererrors also increases Energy of second
            this.heaterPrimarySignalSensors.forEach(signalSensorSpi -> {
                if (signalSensorSpi.signalActive().value().get() == true) {
                    percentIncreaseValve.getAndAdd(this.valveSetPointAddition().value().get());
                    percentIncreaseFallbackHeater.getAndAdd(this.heaterSetPointAddition().value().get());
                }
            });
            this.heaterFallbackSignalSensors.forEach(signalSensorSpi -> {
                if (signalSensorSpi.signalActive().value().get() == true) {
                    percentIncreaseValve.getAndAdd(this.valveSetPointSubtraction().value().get());
                }
            });
            this.heatMixer.changeByPercentage(percentIncreaseValve.get());
            this.heaterControl.forEach(lucid -> lucid.getPercentageChannel().setNextValue(percentIncreaseFallbackHeater.get()));
            this.heaterControlRelay.forEach(relay -> {
                try {
                    relay.getRelaysChannel().setNextWriteValue(true);
                } catch (OpenemsError.OpenemsNamedException e) {
                    e.printStackTrace();
                }
            });

        }
        if (shouldDeactivate == true) {

            this.getOnOff().setNextValue(false);
            this.heatMixer.forceClose();
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
        }


    }

    private void assignCurrentTemperature() {
        this.getPrimaryForward().setNextValue(this.primaryForward.getTemperature().value().get());
        this.getPrimaryRewind().setNextValue(this.primaryRewind.getTemperature().value().get());
        this.getSecondaryForward().setNextValue(this.secondaryForward.getTemperature().value().get());
        this.getSecondaryRewind().setNextValue(this.secondaryRewind.getTemperature().value().get());
    }

    private void averageTemperatureCalculation() {

        AtomicInteger tempAverage = new AtomicInteger(0);

        AtomicInteger minTemp = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger maxTemp = new AtomicInteger(Integer.MIN_VALUE);

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

        tempAverage.set(tempAverage.get() / this.thermometerList.size());
        this.maxTemperature().setNextValue(maxTemp.get());
        this.minTemperature().setNextValue(minTemp.get());
        this.averageTemperature().setNextValue(tempAverage.get());
        int w = tempAverage.get() - this.temperatureSetPointMin().value().get();
        //can change during runtime
        deltaT = this.temperatureSetPointMax().value().get() - this.temperatureSetPointMin().value().get();
        int percentage = ((100 * w) / deltaT);
        this.storagePercent().setNextValue(percentage);
        this.storageEnergy().setNextValue(this.storageLitreMax().value().get() * deltaT * percentage);
        this.storageLitresCurrent().setNextValue(percentage * this.storageLitreMax().value().get() / 100);
    }
}
