package io.openems.edge.temperature.passing.valve;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.manager.valve.api.ManagerValve;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Passing.Valve")
public class ValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, Valve {

    private ActuatorRelaysChannel closing;
    private ActuatorRelaysChannel opens;
    private double secondsPerPercentage;
    private long timeStampValve;

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
     */
    private void valveClose() {
        if (!this.getIsBusy().getNextValue().get()) {
            controlRelays(false, "Open");
            controlRelays(true, "Closed");
            //            this.getIsBusy().setNextValue(true);
            timeStampValve = System.currentTimeMillis();
        }
    }

    /**
     * Opens the valve and sets a time stamp.
     * DO NOT CALL DIRECTLY! Might not work if called directly as the timer for "readyToChange()" is not
     * set properly. Use either "changeByPercentage()" or forceClose / forceOpen.
     */
    private void valveOpen() {
        //opens will be set true when closing is done
        if (!this.getIsBusy().getNextValue().get()) {
            controlRelays(false, "Closed");
            controlRelays(true, "Open");
            //            this.getIsBusy().setNextValue(true);
            timeStampValve = System.currentTimeMillis();
        }
    }


    /**
     * Controls the relays by typing either activate or not and what relays should be called.
     * DO NOT USE THIS !!!! Exception: ValveManager --> Needs this method if Time is up to set Valve Relays off.
     * If ExceptionHandling --> use forceClose or forceOpen!
     * @param activate    activate or deactivate.
     * @param whichRelays opening or closing relays ?
     *                    <p>Writes depending if the relays is an opener or closer, the correct boolean.
     *                    if the relays was set false (no power) busy will be false.</p>
     */
    private void controlRelays(boolean activate, String whichRelays) {
        try {
            switch (whichRelays) {
                case "Open":
                    if (this.opens.isCloser().value().get()) {
                        this.opens.getRelaysChannel().setNextWriteValue(activate);
                    } else {
                        this.opens.getRelaysChannel().setNextWriteValue(!activate);
                    }
                    break;
                case "Closed":
                    if (this.closing.isCloser().value().get()) {
                        this.closing.getRelaysChannel().setNextWriteValue(activate);
                    } else {
                        this.closing.getRelaysChannel().setNextWriteValue(!activate);
                    }
                    break;
            }
            //            if (!activate) {
            //                this.getIsBusy().setNextValue(false);
            //            }

        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Tells if the Time to set the Valve position is up.
     */
    @Override
    public boolean readyToChange() {

        if ((System.currentTimeMillis() - timeStampValve)
                >= ((this.getTimeNeeded().getNextValue().get() * 1000))) {
            this.getIsBusy().setNextValue(false);
            controlRelays(false, "Open");
            controlRelays(false, "Closed");
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
        if ((this.getIsBusy().getNextValue().get()) || percentage == 0) {
            return false;
        } else {
            currentPowerLevel = this.getPowerLevel().getNextValue().get();
            this.getLastPowerLevel().setNextValue(currentPowerLevel);
            currentPowerLevel += percentage;
            if (currentPowerLevel >= 100) {
                currentPowerLevel = 100;
            } else if (currentPowerLevel <= 0) {
                currentPowerLevel = 0;
            }

            this.getPowerLevel().setNextValue(currentPowerLevel);
            //if same power level do not change and return --> relays is not always powered
            if (getLastPowerLevel().getNextValue().get() == currentPowerLevel) {
                return false;
            }
            if (Math.abs(percentage) >= 100) {
                this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
            } else {
                this.getTimeNeeded().setNextValue(Math.abs(percentage) * secondsPerPercentage);
            }
            if (percentage < 0) {
                valveClose();
            } else {
                valveOpen();
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
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
        valveClose();
        this.getIsBusy().setNextValue(true);
    }

    /**
     * Opens the valve completely, overriding any current valve operation.
     * If an open valve is all you need, better use this instead of changeByPercentage(100) as you do not need
     * to check if the valve is busy or not.
     */
    @Override
    public void forceOpen() {
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(100);
        this.getTimeNeeded().setNextValue(100 * secondsPerPercentage);
        valveOpen();
        this.getIsBusy().setNextValue(true);
    }

    @Override
    public String debugLog() {
        if (this.getPowerLevel().getNextValue().isDefined()) {
            String name = "";
            if (!super.alias().equals("")) {
                name = super.alias();
            } else {
                name = super.id();
            }
            return "Valve: " + name + ": " + this.getPowerLevel().getNextValue().toString() + "\n";
        } else {
            return "\n";
        }
    }

}
