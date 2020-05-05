package io.openems.edge.controller.emv.staticValues;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.ChpPowerPercentage;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.PcaDevice;
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
    private List<ChpPowerPercentage> dacList = new ArrayList<>();
    private List<PwmPowerLevelChannel> pwmList = new ArrayList<>();
    private boolean[] relaysValues;
    private double[] dacValues;
    private float[] pwmValues;
    private List<PcaDevice> pcaList = new ArrayList<>();
    private boolean[] pcaValue;

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
        allocateRelaysValues(config.relaysValues());
        allocatePcaValue(config.pcaDeviceValue());
        this.dacValues = config.dacValues();
        this.pwmValues = config.pwmValues();
    }

    private void allocatePcaValue(int[] pcaDeviceValue) {
        AtomicInteger counter = new AtomicInteger(0);

        Arrays.stream(pcaDeviceValue).forEach(pca -> {
            this.pcaValue[counter.intValue()] = pca == 1;
            counter.getAndIncrement();
        });
    }


    /**
     * Due to problems with a boolean array; a new function with int array needed to be implemented.
     *
     * @param relaysValues usually from config ; 1 == ACTIVATE; 0 == DEACTIVATE
     */
    private void allocateRelaysValues(String relaysValues) {

        char[] tempRelaysValues = relaysValues.toCharArray();

        if (this.relaysList.size() > tempRelaysValues.length) {
            this.logInfo(this.log, "Attention! Not enough RelaysValues! Missing Values: " + (this.relaysList.size() - tempRelaysValues.length)
                    + " Following Relays Values will be 0 == false == deactivate");
        }
        this.relaysValues = new boolean[this.relaysList.size()];
        for (int counter = 0; counter < tempRelaysValues.length && counter < this.relaysList.size(); counter++) {
            this.relaysValues[counter] = tempRelaysValues[counter] == '1';
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
                        if (cpm.getComponent(string) instanceof ChpPowerPercentage) {
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

    private void allocatePcaDevice(String pcaDevice) throws ConfigurationException, OpenemsError.OpenemsNamedException {

    }

    @Deactivate
    public void deactivate() {

        super.deactivate();
        IntStream.range(0, this.relaysValues.length).forEach(x -> this.relaysValues[x] = false);
        Arrays.stream(this.dacValues).forEach(value -> value = 0);
        IntStream.range(0, this.pwmValues.length).forEach(x -> this.pwmValues[x] = 0.f);
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


    }

    private Integer calculateAmpereToPercent(double dacValue) {
        return (int) (dacValue * 100 / 20.d);
    }


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        writeValueToChannel();
    }
}
