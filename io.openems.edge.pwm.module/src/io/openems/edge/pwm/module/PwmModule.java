package io.openems.edge.pwm.module;

import java.math.BigDecimal;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.edge.bridge.i2c.api.I2cBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.module.api.Pca9685GpioProvider;
import io.openems.edge.pwm.module.api.AbstractPcaGpioProvider;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Module.Pwm",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class PwmModule extends AbstractOpenemsComponent implements OpenemsComponent {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    I2cBridge refI2cBridge;

    private BigDecimal frequency;
    private BigDecimal frequencyCorrectionFactor;
    private I2CBus i2CBus;
    private AbstractPcaGpioProvider provider;

    public PwmModule() {
        super(OpenemsComponent.ChannelId.values());
    }


    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateBus(config.bus_address());
        this.frequency = new BigDecimal(config.max_frequency());
        float correction = Float.parseFloat(config.actual_frequency()) / Float.parseFloat(config.max_frequency());
        this.frequencyCorrectionFactor = new BigDecimal(Float.toString(correction));
        allocateGpioProvider(config);
    }

    @Deactivate
    public void deactivate() {
        refI2cBridge.removeGpioDevice(super.id());
        super.deactivate();
    }

    /**
     * Depending on Module Version a GpioProvides is created. ATM only one version available; more to come in future.
     *
     * @param config config from Osgi. Method use the version and pwm_address.
     */
    private void allocateGpioProvider(Config config) {
        try {
            int address;
            if (config.pwm_address().contains("0x")) {
                String[] usedAddress = config.pwm_address().split("0x");
                address = Integer.parseInt(usedAddress[1],16);
            } else {
                address = Integer.parseInt(config.pwm_address(), 16);
            }
            //more to come with further versions
            switch (config.version()) {
                case "1":
                    provider = new Pca9685GpioProvider(this.i2CBus, address,
                            this.frequency, this.frequencyCorrectionFactor);
                    this.refI2cBridge.addGpioDevice(super.id(), provider);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void allocateBus(int config) {
        try {

            switch (config) {

                case 0:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_0);
                    break;
                case 1:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_1);
                    break;
                case 2:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_2);
                    break;
                case 3:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_3);
                    break;
                case 4:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_4);
                    break;
                case 5:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_5);
                    break;
                case 6:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_6);
                    break;
                case 7:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_7);
                    break;
                case 8:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_8);
                    break;
                case 9:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_9);
                    break;
                case 10:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_10);
                    break;
                case 11:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_11);
                    break;
                case 12:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_12);
                    break;
                case 13:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_13);
                    break;
                case 14:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_14);
                    break;
                case 15:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_15);
                    break;
                case 16:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_16);
                    break;
                case 17:
                    this.i2CBus = I2CFactory.getInstance(I2CBus.BUS_17);
                    break;

            }

        } catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        }

    }
}
