package io.openems.edge.controller.emv.staticValues;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.PcaDevice;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.gpio.device.api.GpioDevice;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Designate(ocd = Config.class, factory = true)
@Component(name = "ControllerEmvStaticValues")
public class ControllerEmvStaticValues extends AbstractOpenemsComponent implements OpenemsComponent, Controller {
    @Reference
    ComponentManager cpm;

    private final Logger log = LoggerFactory.getLogger(ControllerEmvStaticValues.class);

    private List<ActuatorRelaysChannel> relaysList = new ArrayList<>();
    private List<PowerLevel> dacList = new ArrayList<>();
    private List<PwmPowerLevelChannel> pwmList = new ArrayList<>();
    private boolean[] relaysValues;
    private double[] dacValues;
    private float[] pwmValues;
    private List<PcaDevice> pcaList = new ArrayList<>();
    private boolean[] pcaValue;
    private List<GpioDevice> gpioList = new ArrayList<>();
    private boolean[] gpioValues;

    public ControllerEmvStaticValues() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponents(config.relaysDeviceList(), "Relays");
        allocateComponents(config.DacDeviceList(), "Dac");
        allocateComponents(config.PwmDeviceList(), "Pwm");
        allocateComponents(config.pcaDevice(), "Pca");
        allocateComponents(config.gpioDevices(), "Gpio");
        //contains String --> boolean values --> bc of bug from OSGi / Apache Felix
        allocateValues(config.relaysValues(), "Relays");
        allocateValues(config.gpioDeviceValues(), "Gpio");
        allocateValues(config.pcaDeviceValue(), "Pca");
        //allocateRelaysValues(config.relaysValues());
        this.dacValues = config.dacValues();
        this.pwmValues = config.pwmValues();
    }

    /**
     * Allocates Values depending on the identifier.
     *
     * @param values     usually from Config; contains Values.
     * @param identifier usually from Constructor, used to identify.
     */
    private void allocateValues(String values, String identifier) {


        char[] tempCharValues = values.toCharArray();
        boolean[] tempTrueValues;
        switch (identifier) {

            case "Relays":
                tempTrueValues = new boolean[this.relaysList.size()];

                break;
            case "Pca":
                tempTrueValues = new boolean[this.pcaList.size()];
                break;
            default:
            case "Gpio":
                tempTrueValues = new boolean[this.gpioList.size()];
        }
        if (tempTrueValues.length > tempCharValues.length) {
            this.logInfo(this.log, "Attention Not enough Values for: " + identifier
                    + "Missing Values: " + (tempTrueValues.length - tempCharValues.length)
                    + " Following " + identifier + "values will be 0 == false == deactivate");
        }

        if (this.relaysList.size() > tempCharValues.length) {
            this.logInfo(this.log, "Attention! Not enough RelaysValues! Missing Values: " + (this.relaysList.size() - tempCharValues.length)
                    + " Following Relays Values will be 0 == false == deactivate");
        }
        for (int counter = 0; counter < tempCharValues.length && counter < tempTrueValues.length; counter++) {
            tempTrueValues[counter] = tempCharValues[counter] == '1';
        }
        switch (identifier) {
            case "Relays":
                this.relaysValues = tempTrueValues;
                break;
            case "Pca":
                this.pcaValue = tempTrueValues;
                break;

            case "Gpio":
                this.gpioValues = tempTrueValues;
        }
        //        this.relaysValues = new boolean[this.relaysList.size()];
        //        for (int counter = 0; counter < tempCharValues.length && counter < this.relaysList.size(); counter++) {
        //            this.relaysValues[counter] = tempCharValues[counter] == '1';
        //        }
    }

    //    private void allocatePcaValue(String pcaDeviceValue) {
    //
    //        AtomicInteger counter = new AtomicInteger(0);
    //
    //        Arrays.stream(pcaDeviceValue).forEach(pca -> {
    //            this.pcaValue[counter.intValue()] = pca == 1;
    //            counter.getAndIncrement();
    //        });
    //        //ensures Values
    //        if (counter.intValue() < pcaList.size()) {
    //            for (counter.intValue(); counter.intValue() < pcaList.size(); ) {
    //                this.pcaValue[counter.intValue()] = false;
    //                counter.getAndIncrement();
    //            }
    //        }
    //    }


    /*
     * Due to problems with a boolean array; a new function with int array needed to be implemented.
     *
     * @param relaysValues usually from config ; 1 == ACTIVATE; 0 == DEACTIVATE
     */
    //    private void allocateRelaysValues(String relaysValues) {
    //
    //        char[] tempRelaysValues = relaysValues.toCharArray();
    //
    //        if (this.relaysList.size() > tempRelaysValues.length) {
    //            this.logInfo(this.log, "Attention! Not enough RelaysValues! Missing Values: " + (this.relaysList.size() - tempRelaysValues.length)
    //                    + " Following Relays Values will be 0 == false == deactivate");
    //        }
    //        this.relaysValues = new boolean[this.relaysList.size()];
    //        for (int counter = 0; counter < tempRelaysValues.length && counter < this.relaysList.size(); counter++) {
    //            this.relaysValues[counter] = tempRelaysValues[counter] == '1';
    //        }
    //
    //    }


    /**
     * Allocate given Components and checks if they're correct.
     *
     * @param deviceList is the DeviceList configured by the User.
     * @param identifier is needed for switch case and shows if devices have the correct nature.
     * @throws ConfigurationException             if the Component exists but is the wrong instance
     * @throws OpenemsError.OpenemsNamedException if the component isn't loaded yet.
     */
    private void allocateComponents(String[] deviceList, String identifier) throws ConfigurationException, OpenemsError.OpenemsNamedException {

        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        OpenemsError.OpenemsNamedException[] openemsNamedExceptions = {null};
        ConfigurationException[] configurationExceptions = {null};
        Arrays.stream(deviceList).forEach(string -> {
            try {
                switch (identifier) {
                    case "Relays":
                        if (cpm.getComponent(string) instanceof ActuatorRelaysChannel) {
                            this.relaysList.add(counter.intValue(), cpm.getComponent(string));

                        } else {
                            throw new ConfigurationException("Could not allocate Component: Relays " + string,
                                    "Config error; Check your Config --> Relays");
                        }
                        break;
                    case "Dac":
                        if (cpm.getComponent(string) instanceof PowerLevel) {
                            this.dacList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Dac " + string,
                                    "Config error; Check your Config --> Dac");
                        }
                        break;
                    case "Pwm":
                        if (cpm.getComponent(string) instanceof PwmPowerLevelChannel) {
                            this.pwmList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Pwm " + string,
                                    "Config error; Check your Config --> Pwm Device");
                        }
                        break;

                    case "Pca":
                        if (cpm.getComponent(string) instanceof PcaDevice) {
                            this.pcaList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: Dac " + string,
                                    "Config error; Check your Config --> Dac");
                        }
                        break;

                    case "Gpio":
                        if (cpm.getComponent(string) instanceof GpioDevice) {
                            this.gpioList.add(counter.intValue(), cpm.getComponent(string));
                        } else {
                            throw new ConfigurationException("Could not allocate Component: GPIO : " + string,
                                    "Config error; Check your Config for GPIO");
                        }
                        break;
                }
                counter.getAndIncrement();
            } catch (ConfigurationException e) {
                configurationExceptions[0] = e;
            } catch (OpenemsError.OpenemsNamedException e) {
                openemsNamedExceptions[0] = e;
            }
        });
        if (configurationExceptions[0] != null) {
            throw configurationExceptions[0];
        }
        if (openemsNamedExceptions[0] != null) {
            throw openemsNamedExceptions[0];
        }
    }

    @Deactivate
    public void deactivate() {

        super.deactivate();
        IntStream.range(0, this.relaysValues.length).forEach(x -> this.relaysValues[x] = false);
        Arrays.stream(this.dacValues).forEach(value -> value = 0);
        IntStream.range(0, this.pwmValues.length).forEach(x -> this.pwmValues[x] = 0.f);
        IntStream.range(0, this.pcaValue.length).forEach(x -> this.pcaValue[x] = false);
        IntStream.range(0, this.gpioValues.length).forEach(x -> this.gpioValues[x] = false);
        writeValueToChannel();
    }

    private void writeValueToChannel() {
        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        this.relaysList.forEach(relay -> {
            try {
                relay.getRelaysChannel().setNextWriteValue(relaysValues[counter.getAndIncrement()]);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }

        });
        counter.set(0);
        this.dacList.forEach(dac -> {
            try {
                dac.getPowerLevelChannel().setNextWriteValue(calculateAmpereToPercent(dacValues[counter.getAndIncrement()]));
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }

        });
        counter.set(0);

        this.pwmList.forEach(pwm -> {
            try {
                pwm.getPwmPowerLevelChannel().setNextWriteValue(pwmValues[counter.getAndIncrement()]);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        });
        this.pcaList.forEach(pca -> {
            try {
                pca.getOnOff().setNextWriteValue(this.pcaValue[counter.getAndIncrement()]);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        });
        counter.set(0);
        this.gpioList.forEach(pca -> {
            try {
                pca.getWriteError().setNextWriteValue(gpioValues[counter.getAndIncrement()]);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
            }
        });


    }

    private Integer calculateAmpereToPercent(double dacValue) {
        return (int) (dacValue * 100 / 20.d);
    }


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        writeValueToChannel();
    }
}
