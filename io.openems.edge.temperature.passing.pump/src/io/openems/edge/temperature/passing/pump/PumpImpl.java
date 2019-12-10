package io.openems.edge.temperature.passing.pump;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.device.PwmPowerLevelChannel;
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.pump.api.Pump;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Passing.Pump")
public class PumpImpl extends AbstractOpenemsComponent implements OpenemsComponent, Pump {

    private ActuatorRelaisChannel relais;
    private PwmPowerLevelChannel pwm;
    private boolean onlyRelais = false;

    @Reference
    ComponentManager cpm;

    public PumpImpl() {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (config.pump_Type().equals("Both") || config.pump_Type().equals("Relais")) {
                if (cpm.getComponent(config.pump_Relais()) instanceof ActuatorRelaisChannel) {
                    //close relais via default
                    this.relais = cpm.getComponent(config.pump_Relais());
                    if (this.relais.isCloser().getNextValue().get()) {
                        this.relais.getRelaisChannel().setNextWriteValue(false);
                    } else {
                        this.relais.getRelaisChannel().setNextWriteValue(true);
                    }
                } else {
                    throw new ConfigurationException(config.pump_Relais(), "Allocated Relais not a (configured) Relais");
                }
            }
            if (config.pump_Type().equals("Both") || config.pump_Type().equals("Pwm")) {
                if (cpm.getComponent(config.pump_Pwm()) instanceof PwmPowerLevelChannel) {
                    //set the powerLevel to 0
                    this.pwm = cpm.getComponent(config.pump_Pwm());
                    this.pwm.getPwmPowerLevelChannel().setNextWriteValue(0.f);
                    this.getPowerLevel().setNextValue(0);
                } else {
                    throw new ConfigurationException(config.pump_Pwm(), "Allocated Pwm, not a (configured) Relais");
                }
            }
        } catch (ConfigurationException | OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        if (config.pump_Type().equals("Relais")) {
            this.onlyRelais = true;
        }
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getLastPowerLevel().setNextValue(0);
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        try {
            this.pwm.getPwmPowerLevelChannel().setNextWriteValue(0.f);

            if (this.relais.isCloser().getNextValue().get()) {
                this.relais.getRelaisChannel().setNextWriteValue(false);
            } else {
                this.relais.getRelaisChannel().setNextWriteValue(true);
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

    @Override
    public boolean changeByPercentage(double percentage) {
        if (!this.onlyRelais) {
            double currentPowerLevel;
            if (this.pwm.getPwmPowerLevelChannel().getNextWriteValue().isPresent()) {
                this.getLastPowerLevel().setNextValue(this.pwm.getPwmPowerLevelChannel().getNextWriteValue().get());
            } else {
                this.getLastPowerLevel().setNextValue(0);
            }
            currentPowerLevel = this.getPowerLevel().getNextValue().get();
            currentPowerLevel += percentage;
            if (currentPowerLevel > 100) {
                currentPowerLevel = 100;
            } else if (currentPowerLevel < 0) {
                currentPowerLevel = 0;
                controlRelais(false, "");
            }
            if (currentPowerLevel > 0) {
                controlRelais(true, "");
            }
            this.getPowerLevel().setNextValue(currentPowerLevel);

            try {
                this.pwm.getPwmPowerLevelChannel().setNextWriteValue((float) currentPowerLevel);

                return true;
            } catch (OpenemsError.OpenemsNamedException e) {
                e.printStackTrace();
                return false;
            }
            //In Case Pump is only a relais --> relais will be set on or Off; also depending on PID later
        } else {
            if (percentage <= 0) {
                controlRelais(false, "");
            } else {
                controlRelais(true, "");
            }
            return true;
        }
    }

    @Override
    public void controlRelais(boolean activate, String whichRelais) {
        try {
            if (this.relais.isCloser().value().get()) {
                this.relais.getRelaisChannel().setNextWriteValue(activate);
            } else {
                this.relais.getRelaisChannel().setNextWriteValue(!activate);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }
}
