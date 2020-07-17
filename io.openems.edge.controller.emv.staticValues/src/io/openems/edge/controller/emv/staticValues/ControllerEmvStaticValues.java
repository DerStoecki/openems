package io.openems.edge.controller.emv.staticValues;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;

import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.nature.Sc16Nature;
import io.openems.edge.controller.api.Controller;
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


    private List<Sc16Nature> uartList = new ArrayList<>();
    private List<Integer> gpioList = new ArrayList<>();
    private boolean[] uartValues;

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
        allocateComponents(config.sc16ChoiceList(), "DoubleUart");
        //contains String --> boolean values --> bc of bug from OSGi / Apache Felix
        if (this.relaysList.size() > 0) {
            allocateValues(config.relaysValues(), "Relays");
        }
        if (this.uartList.size() > 0) {
            allocateUart(config.sc16ChoiceList(), config.sc16Values());
        }
        this.dacValues = config.dacValues();
        this.pwmValues = config.pwmValues();
    }

    private void allocateUart(String[] sc16ChoiceList, String sc16Values) {
        for (String s : sc16ChoiceList) {
            switch (s) {
                case "LED-RED":
                    this.gpioList.add(0);
                    break;
                case "LED-YELLOW":
                    this.gpioList.add(1);
                    break;
                case "ENABLE-OUTPUT":
                    this.gpioList.add(6);
                    break;
            }

        }
        allocateValues(sc16Values, "DoubleUart");
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

            case "DoubleUart":
            default:
                tempTrueValues = new boolean[this.gpioList.size()];
                break;
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
            case "DoubleUart":
                this.uartValues = tempTrueValues;
                break;
        }
    }

    /**
     * Allocate given Components and checks if they're correct.
     *
     * @param deviceList is the DeviceList configured by the User.
     * @param identifier is needed for switch case and shows if devices have the correct nature.
     * @throws ConfigurationException             if the Component exists but is the wrong instance
     * @throws OpenemsError.OpenemsNamedException if the component isn't loaded yet.
     */
    private void allocateComponents(String[] deviceList, String identifier) throws
            ConfigurationException, OpenemsError.OpenemsNamedException {

        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        OpenemsError.OpenemsNamedException[] openemsNamedExceptions = {null};
        ConfigurationException[] configurationExceptions = {null};
        Arrays.stream(deviceList).forEach(string -> {
            try {
                if (!string.equals("")) {
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

                        case "DoubleUart":
                            if (cpm.getComponent(string) instanceof Sc16Nature) {
                                this.uartList.add(counter.intValue(), cpm.getComponent(string));
                            } else {
                                throw new ConfigurationException("Could not allocate Component: Dac " + string,
                                        "Config error; Check your Config --> Dac");
                            }
                            break;
                    }
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
        IntStream.range(0, this.uartValues.length).forEach(x -> this.uartValues[x] = false);
        //IntStream.range(0, this.gpioValues.length).forEach(x -> this.gpioValues[x] = false);
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
        this.uartList.forEach(uart -> {
            try {
                switch (this.gpioList.get(counter.intValue())) {
                    case 0:
                        uart.ledRedStatus().setNextWriteValue(this.uartValues[counter.getAndIncrement()]);
                        break;
                    case 1:
                        uart.ledYellowStatus().setNextWriteValue(this.uartValues[counter.getAndIncrement()]);
                        break;
                    case 6:
                        uart.enableOutput().setNextWriteValue(this.uartValues[counter.getAndIncrement()]);
                        break;
                }

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
