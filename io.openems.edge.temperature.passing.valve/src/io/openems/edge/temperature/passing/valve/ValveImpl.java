package io.openems.edge.temperature.passing.valve;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.relais.api.ActuatorRelaisChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Passing.Valve")
public class ValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, PassingChannel, Valve {

    private ActuatorRelaisChannel closing;
    private ActuatorRelaisChannel opens;
    private boolean opening;

    @Reference
    ComponentManager cpm;
    private long timeStampValve;
    private static int EXTRA_BUFFER_TIME = 2 * 1000;

    public ValveImpl() {
        super(OpenemsComponent.ChannelId.values(), PassingChannel.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.closing_Relais()) instanceof ActuatorRelaisChannel) {
                closing = cpm.getComponent(config.closing_Relais());
            }
            if (cpm.getComponent(config.opening_Relais()) instanceof ActuatorRelaisChannel) {
                opens = cpm.getComponent(config.opening_Relais());
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        this.getTimeNeeded().setNextValue(config.valve_Time());
        this.getIsBusy().setNextValue(false);
        this.getPowerLevel().setNextValue(0);
        this.getLastPowerLevel().setNextValue(0);
    }

    @Deactivate
    public void deactivate() {
        try {
            //in case somethings happening; the Valve will be closed.
            if (closing.isCloser().getNextValue().get()) {
                closing.getRelaisChannel().setNextWriteValue(true);
            } else {
                closing.getRelaisChannel().setNextWriteValue(false);
            }
            if (opens.isCloser().getNextValue().get()) {
                opens.getRelaisChannel().setNextWriteValue(false);
            } else {
                opens.getRelaisChannel().setNextWriteValue(true);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void valveClose() {
    }

    @Override
    public void valveOpen() {
        //opens will be set true when closing is done
        if (!this.getIsBusy().getNextValue().get()) {
            controlRelais(false, "Closed");
            controlRelais(true, "Open");
            this.getIsBusy().setNextValue(true);
            timeStampValve = System.currentTimeMillis();
        }
    }

    @Override
    public void controlRelais(boolean activate, String whichRelais) {
        try {
            switch (whichRelais) {
                case "Open":
                    if (this.opens.isCloser().value().get()) {
                        this.opens.getRelaisChannel().setNextWriteValue(activate);
                    } else {
                        this.opens.getRelaisChannel().setNextWriteValue(!activate);
                    }
                    break;
                case "Closed":
                    if (this.closing.isCloser().value().get()) {
                        this.closing.getRelaisChannel().setNextWriteValue(activate);
                    } else {
                        this.closing.getRelaisChannel().setNextWriteValue(!activate);
                    }
                    break;


            }
            if (!activate) {
                this.getIsBusy().setNextValue(false);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean readyToChangeValve() {
        return ((System.currentTimeMillis() - timeStampValve) > (this.getTimeNeeded().getNextValue().get() + EXTRA_BUFFER_TIME));
    }

    @Override
    public void calculatePercentageState(){}

    @Override
    public boolean changeValvePositionByPercentage(double percentage){
        return false;
    }


}
