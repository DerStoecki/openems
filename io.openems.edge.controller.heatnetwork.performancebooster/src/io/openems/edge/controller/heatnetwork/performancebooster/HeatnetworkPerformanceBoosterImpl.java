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
    private Valve heatMixer;
    private Thermometer referenceThermometer;
    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secondaryForward;
    private Thermometer secondaryRewind;


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
        this.heaterSetPointStandard().setNextValue(config.backUpPercent());
        this.heaterSetPointAddition().setNextValue(config.backUpPercentAdditional());
        this.temperatureSetPoint().setNextValue(config.activationTemp());


        allocateComponents(config.thermometer(), "Thermometer");
        allocateComponent(config.primaryForward(), "pF");
        allocateComponent(config.secondaryForward(), "sF");
        allocateComponent(config.primaryRewind(), "pR");
        allocateComponent(config.secondaryRewind(), "sR");
        allocateComponent(config.referenceThermometer(), "ref");
        allocateComponents(config.errorInputHeater1(), "Heater1");
        allocateComponents(config.errorInputHeater2(), "Heater2");
        allocateComponent(config.valve(), "Valve");
        allocateComponents(config.heaters(), "Lucid");
        this.getOnOff().setNextValue(false);
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

                            case "Lucid":
                                if (cpm.getComponent(comp) instanceof LucidControlDeviceOutput) {
                                    this.heaterControl.add(cpm.getComponent(comp));
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

        int avg = averageTemperatureCalculation();



    }

    private int averageTemperatureCalculation() {
        AtomicInteger temp = new AtomicInteger(0);
        this.thermometerList.forEach(thermometer -> {
            if (thermometer.getTemperature().value().isDefined()) {
                temp.getAndAdd(thermometer.getTemperature().value().get());
            }
        });

        temp.set(temp.get() / this.thermometerList.size());
        return temp.get();
    }
}
