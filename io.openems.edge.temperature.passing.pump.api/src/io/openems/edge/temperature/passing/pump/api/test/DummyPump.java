package io.openems.edge.temperature.passing.pump.api.test;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.pump.api.Pump;

public class DummyPump extends AbstractOpenemsComponent implements OpenemsComponent, Pump {

    private ActuatorRelaysChannel relais;
    //private PwmPowerLevelChannel pwm;
    private boolean isRelais = false;
    private boolean isPwm = false;
    private PwmPowerLevelChannel pwm;

    public DummyPump(String id, ActuatorRelaysChannel relais, PwmPowerLevelChannel pwm, String type) {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());

        super.activate(null, id, "", true);

        this.relais = relais;
        this.pwm = pwm;

        switch (type) {
            case "Relais":
                isRelais = true;
                break;

            case "Pwm":
                isPwm = true;
                break;

            default:
                isRelais = true;
                isPwm = true;
        }

        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getLastPowerLevel().setNextValue(0);

    }

    @Override
    public boolean readyToChange() {
        return true;
    }

    @Override
    public boolean changeByPercentage(double percentage) {
        if (this.isRelais) {
            if (percentage <= 0) {
                controlRelays(false, "");
            } else {
                controlRelays(true, "");
            }
        }
        if (this.isPwm) {
            double currentPowerLevel;
            this.getLastPowerLevel().setNextValue(this.getPowerLevel().getNextValue().get());
            currentPowerLevel = this.getPowerLevel().getNextValue().get();
            currentPowerLevel += percentage;
            currentPowerLevel = currentPowerLevel > 100 ? 100 : currentPowerLevel;
            currentPowerLevel = currentPowerLevel < 0 ? 0 : currentPowerLevel;
            System.out.println("Set Next Write Value to " + currentPowerLevel + "in " + pwm.id());
            this.getPowerLevel().setNextValue(currentPowerLevel);
        }
        return true;
    }

    @Override
    public void controlRelays(boolean activate, String whichRelais) {
        if (this.relais.isCloser().getNextValue().get()) {
            System.out.println("Relais is " + activate);
        } else {
            System.out.println("Relais is " + !activate);
        }
    }
}
