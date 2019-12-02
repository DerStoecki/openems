package io.openems.edge.controller.temperature.passing;

import io.openems.common.exceptions.HeatToLowException;
import io.openems.common.exceptions.NoHeatNeededException;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.ValveDefectException;
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

    private boolean timeSetHeating = false;
    private boolean noError = true;


    //for Tpv> minTemp + toleranceTemp
    private int toleranceTemp = 20;
    private int timeToHeatUp;

    private int extraBufferTime = 2 * 1000;

    //for errorHandling
    private int startingTemperature;
    private int roundAboutTemp = 20;
    //30 seconds * 1000 = 30 000 mS

    private int timeValveNeedsToOpenAndClose = 30 * 1000;
    //ty
    private long timeStampHeating;
    //tx
    private long timeStampValve;


    public ControllerPassingImpl() {

        super(OpenemsComponent.ChannelId.values(),
                ControllerPassingChannel.ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        //just to make sure; (for the Overseer Controller)
        this.noError = true;
        this.isOpen = false;
        this.opens = false;
        this.isClosed = true;
        this.closing = false;
        //if user doesn't know ; default == 5 min
        if (config.heating_Time() == 0) {
            this.timeToHeatUp = 5 * 60 * 1000;
        }
        this.timeToHeatUp = config.heating_Time() * 1000;

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
        //later for error Handling
        this.startingTemperature = this.primaryRewind.getTemperature().getNextValue().get();


    }

    @Deactivate
    public void deactivate() {
        this.getOnOff_PassingController().setNextValue(false);
        super.deactivate();
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        //TODO --> getNextWriteValue or getNextValue
        while (noError && this.getOnOff_PassingController().getNextValue().get() && this.getMinTemperature().getNextValue().isDefined()) {
            try {
                if (!isOpen) {
                    if (valveOpen()) {
                        if (System.currentTimeMillis() - timeStampValve > timeValveNeedsToOpenAndClose + this.extraBufferTime) {
                            isOpen = true;
                            controlRelais(false, "Open");
                        } else {
                            return;
                        }
                        //should only occur once
                    } else {
                        return;
                    }

                }
                if (primaryForward.getTemperature().getNextValue().get()
                        >= this.getMinTemperature().getNextValue().get() + toleranceTemp) {

                    timeSetHeating = false;

                    controlRelais(true, "Pump");
                    if (!tooHot()) {
                        return;
                    } else {
                        controlRelais(false, "Pump");
                        noError = false;
                        throw new NoHeatNeededException("Heat is not needed; Shutting down pump and Valves");
                    }


                } else {
                    if (isOpen && !timeSetHeating) {
                        timeStampHeating = System.currentTimeMillis();
                        timeSetHeating = true;
                        return;
                    }
                    if (System.currentTimeMillis() - timeStampHeating > timeToHeatUp + this.extraBufferTime) {

                        this.noError = false;

                        if (Math.abs(primaryRewind.getTemperature().getNextValue().get() - startingTemperature) <= roundAboutTemp) {
                            throw new ValveDefectException("Temperature barely Changed --> Valve Defect!");

                        } else {
                            throw new HeatToLowException("Heat is too low; Min Temperature will not be reached; Closing Valve");

                        }
                    } else {
                        return;
                    }
                }

            } catch (ValveDefectException | HeatToLowException | NoHeatNeededException e) {
                e.printStackTrace();

            } finally {
                valveClose();
            }
        }
        valveClose();
        if (!isClosed) {
            if (System.currentTimeMillis() - timeStampValve > timeValveNeedsToOpenAndClose) {
                isOpen = false;
                isClosed = true;
                timeSetHeating = false;
                controlRelais(false, "Closed");
            }
        }
    }


    private boolean valveOpen() {
        //opens will be set true when closing is done
        if (!opens) {
            controlRelais(true, "Open");
            isClosed = false;
            closing = false;
            timeStampValve = System.currentTimeMillis();
            opens = true;
            return false;
        }
        return true;
    }

    private void valveClose() {
        if (!closing) {
            controlRelais(true, "Closed");
            isOpen = false;
            opens = false;
            timeStampValve = System.currentTimeMillis();
            closing = true;
        }

    }

    private boolean tooHot() {

        return this.secundaryRewind.getTemperature().getNextValue().get() - toleranceTemp
                > this.secundaryForward.getTemperature().getNextValue().get();
    }

    public void controlRelais(boolean activate, String whichRelais) {
        try {
            switch (whichRelais) {
                case "Open":
                    if (this.valveOpen.isCloser()) {
                        this.valveOpen.getRelaisChannelValue().setNextWriteValue(activate);
                    } else {
                        this.valveOpen.getRelaisChannelValue().setNextWriteValue(!activate);
                    }
                    break;
                case "Closed":
                    if (this.valveClose.isCloser()) {
                        this.valveClose.getRelaisChannelValue().setNextWriteValue(activate);
                    } else {
                        this.valveClose.getRelaisChannelValue().setNextWriteValue(!activate);
                    }
                    break;

                case "Pump":
                    if (this.pump.isCloser()) {
                        this.pump.getRelaisChannelValue().setNextWriteValue(activate);
                    } else {
                        this.pump.getRelaisChannelValue().setNextWriteValue(!activate);
                    }
                    break;
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }


    //For the Overseer
    public RelaisActuator getValveClose() {
        return valveClose;
    }

    public int getTimeValveNeedsToOpenAndClose() {
        return timeValveNeedsToOpenAndClose;
    }

    public long getTimeStampValve() {
        return timeStampValve;
    }

    public boolean isNoError() {
        return noError;
    }
}
