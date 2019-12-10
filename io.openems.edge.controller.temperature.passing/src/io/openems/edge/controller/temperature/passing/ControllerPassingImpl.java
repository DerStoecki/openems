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
import io.openems.edge.temperature.passing.pump.api.Pump;
import io.openems.edge.temperature.passing.valve.api.Valve;
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
    private Valve valve;
    private Pump pump;

    private boolean isOpen = false;
    private boolean isClosed = true;

    private boolean timeSetHeating = false;


    //for Tpv> minTemp + toleranceTemp
    private static int TOLERANCE_TEMPERATURE = 20;
    private int timeToHeatUp;

    private static int EXTRA_BUFFER_TIME = 2 * 1000;

    //for errorHandling
    private int startingTemperature;
    //T in dC
    private static int ROUND_ABOUT_TEMP = 20;
    //ty
    private long timeStampHeating;


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
        this.isClosed = true;
        //if user doesn't know ; default == 5 min
        if (config.heating_Time() == 0) {
            this.timeToHeatUp = 5 * 1000 * 60;
        }
        this.timeToHeatUp = config.heating_Time() * 1000;
        try {
            allocate_Component(config.primary_Forward_Sensor(), "Thermometer", "PF");
            allocate_Component(config.primary_Rewind_Sensor(), "Thermometer", "PR");
            allocate_Component(config.secundary_Forward_Sensor(), "Thermometer", "SF");
            allocate_Component(config.secundary_Rewind_Sensor(), "Thermometer", "SR");
            allocate_Component(config.valve_id(), "Valve", "Valve");
            allocate_Component(config.pump_id(), "Pump", "Pump");
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
        this.valve.changeByPercentage(-100);

    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        if (this.getMinTemperature().getNextValue().isDefined()
                && this.getOnOff_PassingController().getNextWriteValue().isPresent()) {
            if (this.noError().getNextValue().get()
                    && this.getOnOff_PassingController().getNextWriteValue().get()) {
                try {
                    if (!isOpen) {
                        if (!valve.getIsBusy().getNextValue().get()) {
                            if (valve.changeByPercentage(100)) {
                                isClosed = false;
                                return;
                            }
                        } else if (valve.readyToChange()) {
                            valve.controlRelais(false, "Open");
                            isOpen = true;
                        } else {
                            return;
                        }
                    }
                    if (primaryForwardReadyToHeat()) {

                        timeSetHeating = false;

                        pump.changeByPercentage(100);


                        if (tooHot()) {
                            pump.changeByPercentage(-100);
                            this.noError().setNextValue(false);
                            throw new NoHeatNeededException("Heat is not needed;"
                                    + "Shutting down pump and Valves");
                        }

                    } else { //Check if there's something wrong with Valve or Heat to low
                        if (isOpen && !timeSetHeating) {
                            timeStampHeating = System.currentTimeMillis();
                            timeSetHeating = true;
                            return;
                        }
                        if (shouldBeHeatingByNow()) {

                            this.noError().setNextValue(false);

                            if (Math.abs(primaryRewind.getTemperature().getNextValue().get()
                                    - startingTemperature) <= ROUND_ABOUT_TEMP) {
                                throw new ValveDefectException("Temperature barely Changed --> Valve Defect!");

                            } else {
                                throw new HeatToLowException("Heat is too low; Min Temperature will not be reached; "
                                        + "Closing Valve");

                            }
                        }
                    }

                } catch (ValveDefectException | NoHeatNeededException | HeatToLowException e) {
                    this.noError().setNextValue(false);
                    valve.controlRelais(false, "Open");
                    valve.controlRelais(true, "Closed");
                    throw e;
                }


            } else {

                if (!isClosed) {
                    if (!valve.getIsBusy().getNextValue().get()) {
                        if (valve.changeByPercentage(-100)) {
                            isOpen = false;
                        } else if (valve.readyToChange()) {
                            isClosed = true;
                            timeSetHeating = false;
                            valve.controlRelais(false, "Closed");
                        }
                    }
                }
            }

        }
    }

    private boolean shouldBeHeatingByNow() {
        return System.currentTimeMillis() - timeStampHeating > timeToHeatUp + EXTRA_BUFFER_TIME;
    }

    private boolean primaryForwardReadyToHeat() {
        return primaryForward.getTemperature().getNextValue().get()
                >= this.getMinTemperature().getNextValue().get() + TOLERANCE_TEMPERATURE;
    }

    private void allocate_Component(String id, String type, String exactType) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        switch (type) {
            case "Thermometer":
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
                    throw new ConfigurationException(id, "The temperaturesensor " + id + " Is Not a (configured) temperature sensor.");
                }

                break;
            case "Pump":
                if (cpm.getComponent(id) instanceof Pump) {
                    this.pump = cpm.getComponent(id);
                } else {
                    throw new ConfigurationException(id, "The Pump " + id + " Is Not a (configured) Pump.");
                }
                break;
            case "Valve":
                if (cpm.getComponent(id) instanceof Valve) {
                    this.valve = cpm.getComponent(id);
                } else {
                    throw new ConfigurationException(id, "The Valve " + id + " Is Not a (configured) Valve");
                }
                break;
        }
    }


    private boolean tooHot() {
        return this.secundaryRewind.getTemperature().getNextValue().get() + TOLERANCE_TEMPERATURE
                > this.secundaryForward.getTemperature().getNextValue().get();
    }


}