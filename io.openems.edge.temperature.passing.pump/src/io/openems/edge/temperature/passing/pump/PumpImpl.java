package io.openems.edge.temperature.passing.pump;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.pump.api.Pump;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Passing.Pump")
public class PumpImpl extends AbstractOpenemsComponent implements OpenemsComponent, Pump {

    private ActuatorRelaysChannel relays;
    private PwmPowerLevelChannel pwm;
    private boolean isRelays = false;
    private boolean isPwm = false;

    @Reference
    ComponentManager cpm;

    public PumpImpl() {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponents(config.pump_Type(), config.pump_Relays(), config.pump_Pwm());
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getLastPowerLevel().setNextValue(0);
    }


    /**
     * Allocates the components.
     *
     * @param pump_type   is the pump controlled via realys, pwm or both.
     * @param pump_relays the unique id of the relays controlling the pump.
     * @param pump_pwm    unique id of the pwm controlling the pump.
     *
     *                    <p>Depending if it's a relays/pwm/both the Components will be fetched by the component-manager and allocated.
     *                    The relays and or pump will be off (just in case).
     *                    </p>
     */
    private void allocateComponents(String pump_type, String pump_relays, String pump_pwm) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        switch (pump_type) {
            case "Relays":
                isRelays = true;
                break;
            case "Pwm":
                isPwm = true;
                break;

            case "Both":
            default:
                isRelays = true;
                isPwm = true;
                break;
        }

        if (isRelays) {
            if (cpm.getComponent(pump_relays) instanceof ActuatorRelaysChannel) {
                this.relays = cpm.getComponent(pump_relays);
                this.relays.getRelaysChannel().setNextWriteValue(!this.relays.isCloser().getNextValue().get());
            } else {
                throw new ConfigurationException(pump_relays, "Allocated relays not a (configured) relays.");
            }
        }
        if (isPwm) {
            if (cpm.getComponent(pump_pwm) instanceof PwmPowerLevelChannel) {
                this.pwm = cpm.getComponent(pump_pwm);
                //reset pwm to 0; so pump is on activation off
                this.pwm.getPwmPowerLevelChannel().setNextWriteValue(0.f);
            } else {
                throw new ConfigurationException(pump_pwm, "Allocated Pwm, not a (configured) pwm-device.");
            }
        }

    }


    /**
     * Deactivates the pump.
     * if the relays is a closer --> false is written --> open
     * if the relays is an opener --> true is written --> open
     * --> no voltage.
     */
    @Deactivate
    public void deactivate() {
        super.deactivate();
        try {
            if (this.isRelays) {
                this.relays.getRelaysChannel().setNextWriteValue(!this.relays.isCloser().getNextValue().get());
            }
            if (this.isPwm) {
                this.pwm.getPwmPowerLevelChannel().setNextWriteValue(0.f);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean readyToChange() {
        //always available
        return true;
    }

    /**
     * Changes the powervalue by percentage.
     *
     * @param percentage to adjust the current powerlevel.
     * @return successful boolean
     * <p>
     * If the Pump is only a relays --> if negative --> controlyRelays false, else true
     * If it's in addition a pwm --> check if the powerlevel - percentage <= 0
     * --> pump is idle --> relays off and pwm is 0.f %
     * Otherwise it's calculating the new Power-level and writing
     * the old power-level in the LastPowerLevel Channel
     * </p>
     */
    @Override
    public boolean changeByPercentage(double percentage) {

        if (this.isRelays) {
            if (this.isPwm) {
                if ((this.getPowerLevel().getNextValue().get() + percentage <= 0)) {
                    this.getLastPowerLevel().setNextValue(this.getPowerLevel().getNextValue().get());
                    this.getPowerLevel().setNextValue(0);
                    try {
                        this.pwm.getPwmPowerLevelChannel().setNextWriteValue(0.f);
                    } catch (OpenemsError.OpenemsNamedException e) {
                        e.printStackTrace();
                        return false;
                    }
                    controlRelays(false, "");
                    return true;
                } else {
                    controlRelays(true,"");
                }
            } else if (percentage <= 0) {
                controlRelays(false, "");
            } else {
                controlRelays(true, "");
            }
        }
        if (this.isPwm) {
            double currentPowerLevel;
            currentPowerLevel = this.getPowerLevel().getNextValue().get();
            this.getLastPowerLevel().setNextValue(currentPowerLevel);
            currentPowerLevel += percentage;
            currentPowerLevel = currentPowerLevel > 100 ? 100
                    : currentPowerLevel < 0 ? 0 : currentPowerLevel;

            this.getPowerLevel().setNextValue(currentPowerLevel);
            try {
                this.pwm.getPwmPowerLevelChannel().setNextWriteValue((float) currentPowerLevel);
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public void controlRelays(boolean activate, String whichRelays) {
        try {
            if (this.relays.isCloser().value().get()) {
                this.relays.getRelaysChannel().setNextWriteValue(activate);
            } else {
                this.relays.getRelaysChannel().setNextWriteValue(!activate);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
