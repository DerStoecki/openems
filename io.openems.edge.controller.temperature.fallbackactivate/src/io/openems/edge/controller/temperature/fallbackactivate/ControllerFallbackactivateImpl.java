package io.openems.edge.controller.temperature.fallbackactivate;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name = "temperature.controller.fallbackactivate")
public class ControllerFallbackactivateImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

    private final Logger log = LoggerFactory.getLogger(ControllerFallbackactivateImpl.class);

    @Reference
    protected ComponentManager cpm;

    private Thermometer tempSensor;
    private ActuatorRelaysChannel relay;
    private int minTemp;

    public ControllerFallbackactivateImpl() {

        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        try {
            allocate_Component(config.temp_Sensor(), "Thermometer");
            allocate_Component(config.relay_id(), "Relay");
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
            throw e;
        }
        minTemp = config.min_temp();

    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        if (tempSensor.getTemperature().getNextValue().get() < minTemp) {
            controlRelay(true);
            //this.logInfo(this.log, "Fallback heater active");
        } else {
			controlRelay(false);
		}
        //		this.logInfo(this.log, "Temperature sensor getTemperature().getNextValue().get(): " + tempSensor.getTemperature().getNextValue().get());

    }


    private void allocate_Component(String id, String type) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        switch (type) {
            case "Thermometer":
                if (cpm.getComponent(id) instanceof Thermometer) {
                    tempSensor = cpm.getComponent(id);
                } else {
                    throw new ConfigurationException(id, "The temperature-sensor " + id + " Is not a (configured) temperature sensor.");
                }
                break;
            case "Relay":
                if (cpm.getComponent(id) instanceof ActuatorRelaysChannel) {
                    relay = cpm.getComponent(id);
                    relay.getRelaysChannel().setNextWriteValue(!relay.isCloser().getNextValue().get());        // set relay to "off" state upon initialization
                } else {
                    throw new ConfigurationException(id, "Allocated relay is not a (configured) relay.");
                }
                break;
        }
    }

    public void controlRelay(boolean activate) {
        try {
            if (relay.isCloser().value().get()) {
                relay.getRelaysChannel().setNextWriteValue(activate);
            } else {
                relay.getRelaysChannel().setNextWriteValue(!activate);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

}
