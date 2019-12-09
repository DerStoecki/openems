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
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Temperature.Controller.Passing")
public class ControllerPassingImpl extends AbstractOpenemsComponent implements OpenemsComponent, ControllerPassingChannel, Controller {

    @Reference
    protected ComponentManager cpm;

    private Thermometer primaryForward;
    private Thermometer primaryRewind;
    private Thermometer secundaryForward;
    private Thermometer secundaryRewind;
    private ActuatorRelaisChannel valveOpen;
    private ActuatorRelaisChannel valveClose;
    private ActuatorRelaisChannel pump;

    private boolean isOpen = false;
    private boolean isClosed = true;
    private boolean opens = false;
    private boolean closing = false;

    private boolean timeSetHeating = false;


    //for Tpv> minTemp + toleranceTemp
    private static int TOLERANCE_TEMPERATURE = 20;
    private int timeToHeatUp;

    private int extraBufferTime = 2 * 1000;

    //for errorHandling
    private int startingTemperature;
    private static int ROUND_ABOUT_TEMP = 20;
    //30 seconds * 1000 = 30 000 mS

    private int timeValveNeedsToOpenAndClose;
    //ty
    private long timeStampHeating;
    //tx
    private long timeStampValve;


    public ControllerPassingImpl() {

        super(OpenemsComponent.ChannelId.values(),
                ControllerPassingChannel.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        //just to make sure; (for the Overseer Controller)
        this.noError().setNextValue(true);
        this.isOpen = false;
        this.opens = false;
        this.isClosed = true;
        this.closing = false;
        //if user doesn't know ; default == 5 min
        if (config.heating_Time() == 0) {
            this.timeToHeatUp = 5 * 1000;
        }
        this.timeToHeatUp = config.heating_Time() * 1000;

        this.timeValveNeedsToOpenAndClose = config.valve_Time() * 1000;
        this.valveTime().setNextValue(config.valve_Time());


        try {
            allocate_Component(config.primary_Forward_Sensor(), "Thermometer", "PF");
            allocate_Component(config.primary_Rewind_Sensor(), "Thermometer", "PR");
            allocate_Component(config.secundary_Forward_Sensor(), "Thermometer", "SF");
            allocate_Component(config.secundary_Rewind_Sensor(), "Thermometer", "SR");
            allocate_Component(config.pump_id(), "Relais", "Pump");
            allocate_Component(config.valve_Open_Relais(), "Relais", "Open");
            allocate_Component(config.valve_Close_Relais(), "Relais", "Close");
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
            throw e;

        }
        //later for error Handling
        this.startingTemperature = this.primaryRewind.getTemperature().getNextValue().get();

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.getOnOff_PassingController().setNextValue(false);
        controlRelais(false, "Open");
        controlRelais(true, "Closed");
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        if (this.getMinTemperature().getNextValue().isDefined() && this.getOnOff_PassingController().getNextWriteValue().isPresent()) {
            if (this.noError().getNextValue().get() && this.getOnOff_PassingController().getNextWriteValue().get()) {
                try {
                    if (!isOpen) {
                        //change to valve Busy
                        if (valveOpen()) {
                            if (readyToChangeValve()) {
                                isOpen = true;
                                controlRelais(false, "Open");
                            } else {
                                return;
                            }
                            // should only occur once
                        } else {
                            controlRelais(true, "Open");
                            controlRelais(false, "Closed");
                            isOpen = false;
                            isClosed = false;
                            return;
                        }
                    }
                    if (primaryForwardReadyToHeat()) {

                        timeSetHeating = false;

                        controlRelais(true, "Pump");

                        if (tooHot()) {
                            controlRelais(false, "Pump");
                            this.noError().setNextValue(false);
                            throw new NoHeatNeededException("Heat is not needed; Shutting down pump and Valves");
                        }

                    } else {
                        if (isOpen && !timeSetHeating) {
                            timeStampHeating = System.currentTimeMillis();
                            timeSetHeating = true;
                            return;
                        }
                        if (shouldBeHeatingByNow()) {

                            this.noError().setNextValue(false);

                            if (Math.abs(primaryRewind.getTemperature().getNextValue().get() - startingTemperature) <= ROUND_ABOUT_TEMP) {
                                throw new ValveDefectException("Temperature barely Changed --> Valve Defect!");

                            } else {
                                throw new HeatToLowException("Heat is too low; Min Temperature will not be reached; Closing Valve");

                            }
                        }
                    }

                } catch (ValveDefectException | NoHeatNeededException | HeatToLowException e) {
                    this.noError().setNextValue(false);
                    controlRelais(false, "Open");
                    valveClose();
                    throw e;
                }


            } else {
                valveClose();
                if (!isClosed) {
                    if (readyToChangeValve()) {
                        isOpen = false;
                        isClosed = true;
                        timeSetHeating = false;
                        controlRelais(false, "Closed");
                    }
                }
            }
        }
    }

    private boolean shouldBeHeatingByNow() {
        return System.currentTimeMillis() - timeStampHeating > timeToHeatUp + this.extraBufferTime;
    }

    private boolean primaryForwardReadyToHeat() {
        return primaryForward.getTemperature().getNextValue().get()
                >= this.getMinTemperature().getNextValue().get() + TOLERANCE_TEMPERATURE;
    }

    private void allocate_Component(String id, String type, String exactType) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        if (type.equals("Thermometer")) {
            if (cpm.getComponent(id) instanceof Thermometer) {
                Thermometer th = cpm.getComponent(id);
                switch (exactType) {
                    case "PF":
                        this.primaryForward = th;
                        break;
                    case "PR":
                        this.primaryRewind = th;
                        break;
                    case "SF":
                        this.secundaryForward = th;
                        break;
                    case "SR":
                        this.secundaryRewind = th;
                        break;
                }
            } else {
                throw new ConfigurationException(id, "The temperaturesensor " + id + "Not a (configured) temperature sensor.");
            }

        } else if (type.equals("Relais")) {
            if (cpm.getComponent(id) instanceof ActuatorRelaisChannel) {
                ActuatorRelaisChannel r = cpm.getComponent(id);
                switch (exactType) {
                    case "Pump":
                        this.pump = r;
                        break;
                    case "Open":
                        this.valveOpen = r;
                        break;
                    case "Close":
                        this.valveClose = r;
                        break;
                }

            } else {
                throw new ConfigurationException(id, "The Relais" + id + "Not a (configured) relais.");
            }
        }

    }

    private boolean readyToChangeValve() {
        return ((System.currentTimeMillis() - timeStampValve) > (timeValveNeedsToOpenAndClose + this.extraBufferTime));
    }

    private boolean valveOpen() {
        //opens will be set true when closing is done
        if (!opens) {
            controlRelais(true, "Open");
            controlRelais(false, "Closed");
            isOpen = false;
            isClosed = false;
            closing = false;
            opens = true;
            timeStampValve = System.currentTimeMillis();
            return false;
        }
        return true;
    }

    private void valveClose() {
        if (!closing) {
            controlRelais(true, "Closed");
            controlRelais(false, "Open");
            controlRelais(false, "Pump");
            isClosed = false;
            isOpen = false;
            opens = false;
            closing = true;
            timeStampValve = System.currentTimeMillis();
        }

    }

    private boolean tooHot() {
        return this.secundaryRewind.getTemperature().getNextValue().get() + TOLERANCE_TEMPERATURE
                > this.secundaryForward.getTemperature().getNextValue().get();
    }

    private void controlRelais(boolean activate, String whichRelais) {
        try {
            switch (whichRelais) {
                case "Open":
                    if (this.valveOpen.isCloser().value().get()) {
                        this.valveOpen.getRelaisChannel().setNextWriteValue(activate);
                    } else {
                        this.valveOpen.getRelaisChannel().setNextWriteValue(!activate);
                    }
                    break;
                case "Closed":
                    if (this.valveClose.isCloser().value().get()) {
                        this.valveClose.getRelaisChannel().setNextWriteValue(activate);
                    } else {
                        this.valveClose.getRelaisChannel().setNextWriteValue(!activate);
                    }
                    break;

                case "Pump":
                    if (this.pump.isCloser().value().get()) {
                        this.pump.getRelaisChannel().setNextWriteValue(activate);
                    } else {
                        this.pump.getRelaisChannel().setNextWriteValue(!activate);
                    }
                    break;
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }


}