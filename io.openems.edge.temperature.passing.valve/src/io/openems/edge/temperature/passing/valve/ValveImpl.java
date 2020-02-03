package io.openems.edge.temperature.passing.valve;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.manager.valve.api.ManagerValve;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "PassingValve")
public class ValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, Valve {

    private ActuatorRelaysChannel closing;
    private ActuatorRelaysChannel opens;
    private double secondsPerPercentage;
    private boolean percentageWasSet = false;
    private long timeStampValve;

    @Reference
    ComponentManager cpm;


    @Reference
    ManagerValve managerValve;

    public ValveImpl() {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.closing_Relais()) instanceof ActuatorRelaysChannel) {
                closing = cpm.getComponent(config.closing_Relais());
            }
            if (cpm.getComponent(config.opening_Relais()) instanceof ActuatorRelaysChannel) {
                opens = cpm.getComponent(config.opening_Relais());
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
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
     * closes the valve and set a time stamp. as well as setting a timestamp.
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
     * Opens the valve, sets a timestamp and make the valve busy.
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
     *
     * @param activate    activate or deactivate.
     * @param whichRelays opening or closing relays ?
     *                    <p>Writes depending if the relays is an opener or closer, the correct boolean.
     *                    if the relays was set false (no power) busy will be false.</p>
     */
    @Override
    public void controlRelays(boolean activate, String whichRelays) {
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
            percentageWasSet = false;
            this.getIsBusy().setNextValue(false);
            return true;
        }

        return false;

    }

    /**
     * Changes Valve Position by incoming percentage
     * Depending on + or - it changes the current State to open/close it more.
     *
     * @param percentage adjusting the current powerlevel in %.
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
            percentageWasSet = true;
            this.getIsBusy().setNextValue(true);
            return true;
        }
    }


}
