package io.openems.edge.temperature.module.signalsensor;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.spi.mcp.api.Adc;
import io.openems.edge.temperature.module.signalsensor.api.SignalSensorSpi;
import io.openems.edge.temperature.module.signalsensor.task.SpiSignalTask;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Device.Signal.Sensor.Spi", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SignalSensorSpiImpl extends AbstractOpenemsComponent implements OpenemsComponent, Thermometer, SignalSensorSpi {


    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;

    @Reference
    ComponentManager cpm;

    private String temperatureBoardId;
    private int spiChannel;
    private int pinPosition;
    private Adc adcForTemperature;


    public SignalSensorSpiImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Thermometer.ChannelId.values(),
                SignalSensorSpi.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException, OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.temperatureBoardId = config.temperatureBoardId();
        this.spiChannel = config.spiChannel();
        this.pinPosition = config.pinPosition();
       this.getSignalType().setNextValue(config.signalType());
        this.getSignalMessage().setNextValue(config.signalDescription());
        createSpiSignalTask();
    }


    /**
     * Checks if the SpiSignalTask is allowed to be created.
     * It Checks if the adc is correct (Spi Channel)
     * Checks if the Pin position is correct and if it's already used by another device.
     * If everything's okay, the task will be created and added to the spiTasks.
     *
     * @throws ConfigurationException             if the User configured something wrong.
     * @throws OpenemsError.OpenemsNamedException if cpm got an error.
     */
    private void createSpiSignalTask() throws ConfigurationException, OpenemsError.OpenemsNamedException {

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
                        SpiSignalTask task = new SpiSignalTask(this.getTemperature(), this.signalActive(),
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

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        bridgeSpi.removeSpiTask(super.id());
        adcForTemperature.getPins().get(this.pinPosition).setUnused();
    }

    @Override
    public String debugLog() {
        String message = super.alias().equals(super.id())?super.id():super.id() + " " + super.alias();
        message += " is ";
        if (this.signalActive().getNextValue().get()) {
            message += "active";
        } else {
            message += "not active";
        }

        if (bridgeSpi.getTasks().containsKey(super.id())) {
            return "The Signal Sensor " + message + "\nSignalType: "
                    + this.getSignalType().getNextValue().get()+ "\nSignalMessage: " + this.getSignalMessage().getNextValue().get();
        } else {
            return "\n";
        }
    }

}
