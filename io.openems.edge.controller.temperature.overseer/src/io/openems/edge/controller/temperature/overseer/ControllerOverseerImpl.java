package io.openems.edge.controller.temperature.overseer;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.temperature.passing.api.ControllerPassing;
import io.openems.edge.relais.api.RelaisActuator;
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

    private RelaisActuator closeValve;
    private ControllerPassing passing;
    private Thermometer temperatureSensor;
    private int tolerance;
    private int waitingTimeValveToClose;
    private long timeSet;
    private boolean waitingTimeSet = false;
    //2 Seconds in Miliseconds
    private int buffer = 2 * 1000;

    public ControllerOverseerImpl() {
        super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values());
    }

    @Reference
    ComponentManager cpm;

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        allocateComponents(config.allocated_Passing_Controller(), config.allocated_Temperature_Sensor());

        closeValve = passing.getValveClose();
        passing.minTemperature().setNextValue(config.min_Temperature());
        this.tolerance = config.tolerated_Temperature_Range();
        this.waitingTimeValveToClose = passing.getTimeValveNeedsToOpenAndClose();

    }

    @Deactivate
    public void deactivate() {

        super.deactivate();
        try {
            this.passing.onOrOffChannel().setNextWriteValue(false);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    private void allocateComponents(String controller, String temperatureSensor) {
        try {
            if (cpm.getComponent(controller) instanceof ControllerPassing) {
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
        }
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        if (!heatingReached() && passing.isNoError()) {
            this.passing.onOrOffChannel().setNextWriteValue(true);

        } else if (passing.isNoError()) {
            this.passing.onOrOffChannel().setNextWriteValue(false);
            //shouldnt be needed but code will remain here till it's tested
            /*
            passing.valveClose();
            if (passing.readyToChangeValve()) {
                passing.controlRelais(false, "Closed");
            */
        } else {
            //deactivate the Relais in this Object; Error Occured
            if (!waitingTimeSet) {
                changeRelaisValue(true);
                timeSet = System.currentTimeMillis();
                waitingTimeSet = true;
            }
            if (System.currentTimeMillis() - timeSet > waitingTimeValveToClose + buffer) {
                changeRelaisValue(false);
            }


        }

    }

    private boolean heatingReached() {
        return this.temperatureSensor.getTemperature().value().get() + tolerance
                >= passing.minTemperature().value().get();
    }

    private void changeRelaisValue(boolean closer) {
        try {
            if (this.closeValve.isCloser()) {
                this.closeValve.getRelaisChannelValue().setNextWriteValue(closer);
            } else {
                this.closeValve.getRelaisChannelValue().setNextWriteValue(!closer);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
