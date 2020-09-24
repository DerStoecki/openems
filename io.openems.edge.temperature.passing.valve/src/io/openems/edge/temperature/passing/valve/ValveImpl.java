package io.openems.edge.temperature.passing.valve;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.manager.valve.api.ManagerValve;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Passing.Valve",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true
)
public class ValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, Valve {

    private ActuatorRelaysChannel closing;
    private ActuatorRelaysChannel opens;
    private double secondsPerPercentage;
    private long timeStampValveInitial;
    private long timeStampValveCurrent = -1;
    //if true updatePowerlevel
    private boolean isChanging = false;
    //if true --> subtraction in updatePowerLevel else add
    private boolean isClosing = false;
    private boolean wasAlreadyReset = false;


    @Reference
    ComponentManager cpm;


    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    ManagerValve managerValve;

    public ValveImpl() {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        if (cpm.getComponent(config.closing_Relays()) instanceof ActuatorRelaysChannel) {
            closing = cpm.getComponent(config.closing_Relays());
        }
        if (cpm.getComponent(config.opening_Relays()) instanceof ActuatorRelaysChannel) {
            opens = cpm.getComponent(config.opening_Relays());
        }
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getLastPowerLevel().setNextValue(0);
        this.secondsPerPercentage = ((double) config.valve_Time() / 100.d);
        this.managerValve.addValve(super.id(), this);
        this.getTimeNeeded().setNextValue(0);
        this.setPowerLevelPercent().setNextValue(-1);
        this.setGoalPowerLevel().setNextValue(0);
        if (config.shouldCloseOnActivation()) {
            this.forceClose();
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            super.deactivate();
            //in case somethings happening; the Valve will be closed.
            closing.getRelaysChannel().setNextWriteValue(closing.isCloser().getNextValue().get());
            opens.getRelaysChannel().setNextWriteValue(!opens.isCloser().getNextValue().get());
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        this.managerValve.removeValve(super.id());
    }

    /**
     * Closes the valve and sets a time stamp.
     * DO NOT CALL DIRECTLY! Might not work if called directly as the timer for "readyToChange()" is not
     * set properly. Use either "changeByPercentage()" or forceClose / forceOpen.
     *
     * @param wasForced indicates if the valve was Forced to close or not.
     */
    private void valveClose(boolean wasForced) {
        if ((this.getIsBusy().value().isDefined() && !this.getIsBusy().value().get()) || wasForced) {
            controlRelays(false, "Open");
            controlRelays(true, "Closed");
            timeStampValveInitial = getMilliSecondTime();
            this.isClosing = true;
        }
    }

    /**
     * Opens the valve and sets a time stamp.
     * DO NOT CALL DIRECTLY! Might not work if called directly as the timer for "readyToChange()" is not
     * set properly. Use either "changeByPercentage()" or forceClose / forceOpen.
     *
     * @param wasForced indicates if the valve was forced to Open.
     */
    private void valveOpen(boolean wasForced) {
        //opens will be set true when closing is done

        if ((this.getIsBusy().value().isDefined() && !this.getIsBusy().value().get()) || wasForced) {
            controlRelays(false, "Closed");
            controlRelays(true, "Open");
            //            this.getIsBusy().setNextValue(true);
            timeStampValveInitial = getMilliSecondTime();
            this.isClosing = false;

        }
    }

    /**
     * get Current Time in Ms.
     *
     * @return currentTime in Ms.
     */

    private long getMilliSecondTime() {
        long time = System.nanoTime();
        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }


    /**
     * Controls the relays by typing either activate or not and what relays should be called.
     * DO NOT USE THIS !!!! Exception: ValveManager --> Needs this method if Time is up to set Valve Relays off.
     * If ExceptionHandling --> use forceClose or forceOpen!
     *
     * @param activate    activate or deactivate.
     * @param whichRelays opening or closing relays ?
     *                    <p>Writes depending if the relays is an opener or closer, the correct boolean.
     *                    if the relays was set false (no power) busy will be false.</p>
     */
    private void controlRelays(boolean activate, String whichRelays) {
        try {
            switch (whichRelays) {
                case "Open":

                    this.opens.getRelaysChannel().setNextWriteValue(activate);
                    break;

                case "Closed":
                    this.closing.getRelaysChannel().setNextWriteValue(activate);
                    break;
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Tells if the Time to set the Valve position is up.
     */
    @Override
    public boolean readyToChange() {
        long currentTime = getMilliSecondTime();
        if ((currentTime - timeStampValveInitial)
                >= ((this.getTimeNeeded().getNextValue().get() * 1000))) {
            this.getIsBusy().setNextValue(false);

            controlRelays(false, "Open");
            controlRelays(false, "Closed");
            this.wasAlreadyReset = false;
            this.isChanging = false;
            return true;
        }
        return false;
    }

    /**
     * Changes Valve Position by incoming percentage.
     * Warning, only executes if valve is not busy!
     * Depending on + or - it changes the current State to open/close it more. Switching the relays on/off does
     * not open/close the valve instantly but slowly. The time it takes from completely closed to completely
     * open is entered in the config. Partial open state of x% is then archived by switching the relay on for
     * time-to-open * x%, or the appropriate amount of time depending on initial state.
     * Sets the Future PowerLevel; ValveManager calls further Methods to refresh true % state
     *
     * @param percentage adjusting the current powerlevel in % points. Meaning if current state is 10%, requesting
     *                   changeByPercentage(20) will change the state to 30%.
     *                   <p>
     *                   If the Valve is busy (already changing by a previous percentagechange. return false
     *                   otherwise: save the current PowerLevel to the old one and overwrite the new one.
     *                   Then it will check how much time is needed to adjust the position of the valve.
     *                   If percentage is neg. valve needs to be closed (further)
     *                   else it needs to open (further).
     *                   </p>
     */
    @Override
    public boolean changeByPercentage(double percentage) {
        double currentPowerLevel;

        //opens / closes valve by a certain percentage value
        if ((this.getIsBusy().value().isDefined() && this.getIsBusy().value().get()) || percentage == 0) {
            return false;
        } else {
            currentPowerLevel = this.getPowerLevel().value().get();
            this.getLastPowerLevel().setNextValue(currentPowerLevel);
            currentPowerLevel += percentage;
            if (currentPowerLevel >= 100) {
                currentPowerLevel = 100;
            } else if (currentPowerLevel <= 0) {
                currentPowerLevel = 0;
            }
            //Set goal Percentage for future reference
            this.setGoalPowerLevel().setNextValue(currentPowerLevel);
            //if same power level do not change and return --> relays is not always powered
            if (getLastPowerLevel().getNextValue().get() == currentPowerLevel) {
                this.isChanging = false;
                return false;
            }
            if (Math.abs(percentage) >= 100) {
                this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
            } else {
                this.getTimeNeeded().setNextValue(Math.abs(percentage) * secondsPerPercentage);
            }
            if (percentage < 0) {
                isChanging = true;
                valveClose(false);
            } else {
                isChanging = true;
                valveOpen(false);
            }
            this.getIsBusy().setNextValue(true);
            return true;
        }
    }


    /**
     * Closes the valve completely, overriding any current valve operation.
     * If a closed valve is all you need, better use this instead of changeByPercentage(-100) as you do not need
     * to check if the valve is busy or not.
     */
    @Override
    public void forceClose() {

        this.setGoalPowerLevel().setNextValue(0);
        this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
        valveClose(true);
        this.getIsBusy().setNextValue(true);
        this.isChanging = true;
        this.timeStampValveCurrent = -1;

    }

    /**
     * Opens the valve completely, overriding any current valve operation.
     * If an open valve is all you need, better use this instead of changeByPercentage(100) as you do not need
     * to check if the valve is busy or not.
     */
    @Override
    public void forceOpen() {

        this.setGoalPowerLevel().setNextValue(100);
        this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
        valveOpen(true);
        this.getIsBusy().setNextValue(true);
        this.isChanging = true;
        this.timeStampValveCurrent = -1;

    }

    //----------------
    //UPDATE POWERLEVEL AND POWER LEVEL REACHED

    /**
     * <p>Update Powerlevel by getting elapsed Time and check how much time has passed by either initial Valve timestamp
     * or last time.
     * Current PowerLevel and new Percentage is added together and rounded to 3 decimals.
     */
    @Override
    public void updatePowerLevel() {
        if (this.isChanging()) {
            long elapsedTime = getMilliSecondTime();

            if (this.timeStampValveCurrent == -1) {
                elapsedTime -= timeStampValveInitial;
            } else {
                elapsedTime -= timeStampValveCurrent;
            }
            timeStampValveCurrent = getMilliSecondTime();
            double percentIncrease = elapsedTime / (this.secondsPerPercentage * 1000);
            if (this.isClosing) {
                percentIncrease *= -1;
            }

            double truncatedDouble = BigDecimal.valueOf(this.getPowerLevel().value().get() + percentIncrease)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();
            if (truncatedDouble > 100) {
                truncatedDouble = 100;
            } else if (truncatedDouble < 0) {
                truncatedDouble = 0;
            }
            this.getPowerLevel().setNextValue(truncatedDouble);
        }
    }

    /**
     * Check if Valve is opened enough.
     *
     * @return is powerLevelReached
     */
    @Override
    public boolean powerLevelReached() {
        boolean reached = false;
        if (this.getPowerLevel().value().isDefined() && this.setGoalPowerLevel().getNextValue().isDefined()) {
            if (this.isClosing) {
                reached = this.getPowerLevel().value().get() <= this.setGoalPowerLevel().getNextValue().get();
            } else {
                reached = this.getPowerLevel().value().get() >= this.setGoalPowerLevel().getNextValue().get();

            }
        }
        if (reached) {
            this.isChanging = false;
            this.timeStampValveCurrent = -1;
        }
        return reached;
    }

    // -----------------------

    /**
     * IS Changing --> Is closing/Opening.
     *
     * @return isChanging
     */
    @Override
    public boolean isChanging() {
        return this.isChanging;
    }

    //---------------------RESET-------------------------

    /**
     * Resets the Valve and forces to close.
     * Was Already Reset prevents multiple forceCloses if Channel not refreshed in time.
     */
    @Override
    public void reset() {
        if (this.wasAlreadyReset == false) {
            this.forceClose();
            this.wasAlreadyReset = true;
        }

    }

    /**
     * Called by ValveManager to check if this Valve should be reset.
     *
     * @return shouldReset.
     */
    @Override
    public boolean shouldReset() {
        AtomicBoolean shouldReset = new AtomicBoolean(false);
        if (this.shouldForceClose().getNextWriteValue().isPresent()) {
            return this.shouldForceClose().getNextWriteValue().get();
        }
        return shouldReset.get();
    }

    // --------------------------------------
    @Override
    public String debugLog() {
        if (this.getPowerLevel().value().isDefined()) {
            String name = "";
            if (!super.alias().equals("")) {
                name = super.alias();
            } else {
                name = super.id();
            }
            return "Valve: " + name + ": " + this.getPowerLevel().value().toString() + "\n";
        } else {
            return "\n";
        }
    }

//    @Override
//    public void handleEvent(Event event) {
//
////        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
////           this.updatePowerLevel();
////           boolean reached = powerLevelReached();
////           if (reached) {
////               this.readyToChange();
////           }
////        }
//        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_CONTROLLERS)) {
//            if (this.setPowerLevelPercent().value().isDefined() && this.setPowerLevelPercent().value().get() >= 0) {
//
//                if (this.changeByPercentage(changeByPercent)) {
//                    try {
//                        this.setPowerLevelPercent().setNextWriteValue(-1);
//                    } catch (OpenemsError.OpenemsNamedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        }
//    }
}

