package io.openems.edge.temperature.sensor;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.temperature.sensor.task.TemperatureDigitalReadTask;
import io.openems.edge.spi.mcp.api.Adc;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Device.Temperature.Sensor", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)

public class TemperatureSensorImpl extends AbstractOpenemsComponent implements OpenemsComponent, Thermometer {
    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;

    @Reference
    ComponentManager cpm;

    private String temperatureBoardId;
    private int spiChannel;
    private int pinPosition;
    private String alias;
    private Adc adcForTemperature;
    private final Logger log = LoggerFactory.getLogger(TemperatureSensorImpl.class);


    public TemperatureSensorImpl() {
        super(OpenemsComponent.ChannelId.values(), Thermometer.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.temperatureBoardId = config.temperatureBoardId();
        this.spiChannel = config.spiChannel();
        this.pinPosition = config.pinPosition();
        this.alias = config.alias();
        createTemperatureDigitalReadTask();

    }

    /**
     * Checks if the DigitalReadTask is allowed to be created.
     * It Checks if the adc is correct (Spi Channel)
     * Checks if the Pin position is correct and if it's already used by another device.
     * If everything's okay, the task will be created and added to the spiTasks.
     *
     * @throws ConfigurationException if the User configured something wrong.
     */
    private void createTemperatureDigitalReadTask() throws ConfigurationException {
        try {
            ConfigurationException[] ex = {null};
            if (cpm.getComponent(temperatureBoardId).isEnabled()) {
                bridgeSpi.getAdcs().stream().filter(
                        allocate -> allocate.getSpiChannel() == this.spiChannel
                ).findFirst().ifPresent(value -> {
                    adcForTemperature = value;
                    value.getPins().stream().filter(
                            allocate -> allocate.getPosition() == this.pinPosition
                    ).findFirst().ifPresent(pinValue -> {
                        if (pinValue.setUsedBy(super.id()) || pinValue.getUsedBy().equals(super.id())) {
                            TemperatureDigitalReadTask task = new TemperatureDigitalReadTask(this.getTemperature(),
                                    adcForTemperature.getVersionId(), adcForTemperature, this.pinPosition);
                            try {
                                bridgeSpi.addSpiTask(super.id(), task);
                            } catch (ConfigurationException e) {
                                ex[0] = e;
                            }
                        } else {
                            ex[0] = new ConfigurationException("Something went wrong! Check your config file",
                                    "Couldn't Create Task, check your Config file");
                        }
                    });
                });

                if (ex[0] != null) {
                    throw ex[0];
                }

            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        bridgeSpi.removeSpiTask(super.id());
        adcForTemperature.getPins().get(this.pinPosition).setUnused();
        adcForTemperature = null;
    }

    @Override
    public String debugLog() {

        if (bridgeSpi.getTasks().containsKey(super.id())) {
            return "T:" + this.getTemperature().value().asString() + " of TemperatureSensor: " + super.id() + " " + this.alias
                    + "\n";
        } else {
            return "\n";
        }
    }
}
