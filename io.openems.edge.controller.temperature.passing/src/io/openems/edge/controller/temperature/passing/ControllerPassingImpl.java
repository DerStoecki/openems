package io.openems.edge.controller.temperature.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.temperature.passing.api.ControllerPassingChannel;
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
@Component(name = "Temperature Passing Controller ")
public class ControllerPassingImpl extends AbstractOpenemsComponent implements OpenemsComponent, ControllerPassingChannel, Controller {

    @Reference
    ComponentManager cpm;

    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secundaryForward;
    private Thermometer secundaryRewind;
    private RelaisActuator valveOpen;
    private RelaisActuator valveClose;
    private RelaisActuator pump;

    private boolean isOpen = false;
    private boolean isClosed = true;
    private boolean opens = false;
    private boolean closing = false;
    private boolean timeSetOpener = false;
    private boolean timeSetCloser = false;
    private boolean noError = true;

    //for Tpv> minTemp + toleranceTemp
    private int toleranceTemp;
    private int timePump;
    private int heatingTime;

    //30 seconds * 1000 = 30 000 mS

    private int timeValveNeedsToOpenAndClose = 30 * 1000;
    //ty
    private int timeStampHeating;
    //tx
    private long timeStampValve;


    public ControllerPassingImpl() {

        super(OpenemsComponent.ChannelId.values(),
                ControllerPassingChannel.ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.primary_Forward_Sensor()) instanceof Thermometer) {
                this.primaryForward = cpm.getComponent(config.primary_Forward_Sensor());
            } else {
                throw new ConfigurationException(config.primary_Forward_Sensor(), "The primary forward sensor " + config.primary_Forward_Sensor() + "Not a (configured) temperature sensor");
            }
            if (cpm.getComponent(config.primary_Rewind_Sensor()) instanceof Thermometer) {
                this.primaryRewind = cpm.getComponent(config.primary_Rewind_Sensor());
            } else {
                throw new ConfigurationException(config.primary_Rewind_Sensor(), "The primary rewind sensor " + config.primary_Rewind_Sensor() + "Not a (configured) temperature sensor");
            }
            if (cpm.getComponent(config.secundary_Forward_Sensor()) instanceof Thermometer) {
                this.secundaryForward = cpm.getComponent(config.secundary_Forward_Sensor());
            } else {
                throw new ConfigurationException(config.secundary_Forward_Sensor(), "The secundary forward sensor " + config.secundary_Forward_Sensor() + "Not a (configured) temperature sensor");
            }
            if (cpm.getComponent(config.secundary_Rewind_Sensor()) instanceof Thermometer) {
                this.secundaryRewind = cpm.getComponent(config.secundary_Rewind_Sensor());
            } else {
                throw new ConfigurationException(config.secundary_Rewind_Sensor(), "The secundary rewind sensor " + config.secundary_Rewind_Sensor() + "Not a (configured) temperature sensor");
            }

            if (cpm.getComponent(config.pump_id()) instanceof RelaisActuator) {
                this.pump = cpm.getComponent(config.pump_id());
            } else {
                throw new ConfigurationException(config.pump_id(), "The relais Id " + config.pump_id() + "Not a (configured) relais");
            }

            if (cpm.getComponent(config.valve_Open_Relais()) instanceof RelaisActuator) {
                this.valveOpen = cpm.getComponent(config.valve_Open_Relais());
            } else {
                throw new ConfigurationException(config.valve_Open_Relais(), "The relais Id " + config.valve_Open_Relais() + "Not a (configured) relais");
            }

            if (cpm.getComponent(config.valve_Close_Relais()) instanceof RelaisActuator) {
                this.valveClose = cpm.getComponent(config.valve_Close_Relais());
            } else {
                throw new ConfigurationException(config.pump_id(), "The relais Id " + config.valve_Close_Relais() + "Not a (configured) relais");
            }

        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }

    }

    @Deactivate
    public void deactivate() {
        //TODO How to deactivate Properly? --> Via REST / Overseer ?
        //TODO
        // closePump();
        // closeValve();
        super.deactivate();
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        //TODO --> getNextWriteValue or getNextValue
        while (noError && this.getOnOff_PassingController().getNextValue().get()) {

            if (!isOpen && valveOpen()) {
                if (System.currentTimeMillis() - timeStampValve > timeValveNeedsToOpenAndClose) {
                    isOpen = true;
                    controlValve(false, "Open");
                    this.timeSetOpener = false;
                }
            }
            
        }

    }

    private boolean valveOpen() {
        //opens will be set true when closing is done
        if (!opens) {
            controlValve(true, "Open");
            isClosed = false;
            timeStampValve = System.currentTimeMillis();
            opens = true;

            return false;
        }
        return true;
    }


    private void controlValve(boolean activate, String whichValve) {
        try {
            switch (whichValve) {
                case "Open":
                    if (this.valveOpen.isCloser()) {
                        this.valveOpen.getRelaisChannelValue().setNextWriteValue(activate);
                    } else {
                        this.valveOpen.getRelaisChannelValue().setNextWriteValue(!activate);
                    }
                    break;
                case "Close":
                    if (this.valveClose.isCloser()) {
                        this.valveClose.getRelaisChannelValue().setNextWriteValue(activate);
                    } else {
                        this.valveClose.getRelaisChannelValue().setNextWriteValue(!activate);
                    }
                    break;
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
