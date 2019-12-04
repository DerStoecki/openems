package io.openems.edge.controller.temperature.overseer;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.temperature.passing.api.ControllerPassingChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "TemperatureControllerOverseer")
public class ControllerOverseerImpl extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

    protected ControllerPassingChannel passing;
    protected Thermometer temperatureSensor;
    private int tolerance;
    protected int waitingTimeValveToClose;

    public ControllerOverseerImpl() {
        super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values());
    }

    @Reference
    ComponentManager cpm;

    @Activate
    void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        allocateComponents(config.allocated_Passing_Controller(), config.allocated_Temperature_Sensor());

        passing.getMinTemperature().setNextValue(config.min_Temperature());
        this.tolerance = config.tolerated_Temperature_Range();
        this.waitingTimeValveToClose = passing.valveTime().getNextValue().get();

    }

    @Deactivate
    public void deactivate() {

        super.deactivate();
        try {
            this.passing.getOnOff_PassingController().setNextWriteValue(false);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    private void allocateComponents(String controller, String temperatureSensor) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        try {
            if (cpm.getComponent(controller) instanceof ControllerPassingChannel) {
                passing = cpm.getComponent(controller);

            } else {
                throw new ConfigurationException(controller,
                        "Allocated Passing Controller not a Passing Controller; Check if Name is correct and try again");
            }
            if (cpm.getComponent(temperatureSensor) instanceof Thermometer) {
                this.temperatureSensor = cpm.getComponent(temperatureSensor);
            } else {
                throw new ConfigurationException(temperatureSensor,
                        "Allocated Temperature Sensor is not Correct; Check Name and try again.");
            }
        } catch (ConfigurationException | OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        if (passing == null) {
            throw new RuntimeException("The Allocated Passing Controller is not active, please Check.");
        } else if (!heatingReached() && passing.noError().getNextValue().get()) {
            this.passing.getOnOff_PassingController().setNextWriteValue(true);
        } else if (heatingReached() && passing.noError().getNextValue().get()) {
            this.passing.getOnOff_PassingController().setNextWriteValue(false);
        } else {
            throw new OpenemsException("The Passing Controller got an Error!");
        }
    }

    private boolean heatingReached() {
        return this.temperatureSensor.getTemperature().value().get() + tolerance
                >= passing.getMinTemperature().value().get();
    }
}
