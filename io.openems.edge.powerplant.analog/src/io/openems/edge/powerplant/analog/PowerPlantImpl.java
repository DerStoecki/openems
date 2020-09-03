package io.openems.edge.powerplant.analog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.lucidcontrol.device.api.LucidControlDeviceOutput;
import io.openems.edge.powerplant.analog.api.PowerPlant;
import io.openems.edge.temperature.module.signalsensor.api.SignalSensorSpi;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Powerplant.Analog",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class PowerPlantImpl extends AbstractOpenemsComponent implements PowerPlant, OpenemsComponent, EventHandler {

    @Reference
    ComponentManager cpm;

    private LucidControlDeviceOutput output;
    private List<SignalSensorSpi> sensors = new ArrayList<>();
    private int lastPercentValue = -1;
    private int lastKiloWattValue = -1;

    public PowerPlantImpl() {
        super(OpenemsComponent.ChannelId.values(), PowerPlant.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (cpm.getComponent(config.analogueDevice()) instanceof LucidControlDeviceOutput) {
            this.output = cpm.getComponent(config.analogueDevice());
        }
        OpenemsError.OpenemsNamedException[] ex = {null};
        Arrays.stream(config.errorBits()).forEach(string -> {
            try {
                if (cpm.getComponent(string) instanceof SignalSensorSpi) {
                    sensors.add(cpm.getComponent(string));
                }
            } catch (OpenemsError.OpenemsNamedException e) {
                ex[0] = e;
            }
        });
        if (ex[0] != null) {
            throw ex[0];
        }
        this.getMaximumKw().setNextValue(config.maxKw());
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    private void forever() {
        AtomicBoolean hasError = new AtomicBoolean(false);
        this.sensors.forEach(sensor -> {
            if (sensor.getSignalType().value().get().equals("Error")) {
                if (sensor.signalActive().value().get()) {
                    hasError.set(true);
                }
            }

        });
        if (hasError.get() == false) {
            this.getErrorOccured().setNextValue(false);

            if (checkLastPercentAndRefresh() == false) {
                checkLastKilowattAndRefresh();
            }
            this.output.getPercentageChannel().setNextValue(lastPercentValue);

        } else {
            this.getErrorOccured().setNextValue(true);
        }
    }

    //Returns False if no Value is defined or hasn't changed and previous CheckLastPercent was False;
    private boolean checkLastKilowattAndRefresh() {
        if (this.getPowerLevelKiloWatt().value().isDefined()) {
            int kiloWatt = this.getPowerLevelKiloWatt().value().get() > 0 ? this.getPowerLevelKiloWatt().value().get() : 0;
            if (kiloWatt != this.lastKiloWattValue) {
                int newPercent = kiloWatt * 100 / this.getMaximumKw().value().get();
                this.lastPercentValue = newPercent;
                this.getPowerLevelPercent().setNextValue(newPercent);
                return true;
            }
        }
        return false;
    }

    //Returns false if last Percent Value hasn't changed to the current Value
    //If percentValue has changed --> set in LucidControl and refresh kW Value.
    private boolean checkLastPercentAndRefresh() {
        if (this.getPowerLevelPercent().value().isDefined()) {
            int percent = this.getPowerLevelPercent().value().get() > 0 ? this.getPowerLevelPercent().value().get() : 0;
            if (percent != this.lastPercentValue) {
                this.lastPercentValue = percent;
                int newKw = percent * this.getMaximumKw().value().get() / 100;
                this.lastKiloWattValue = newKw;
                this.getPowerLevelKiloWatt().setNextValue(newKw);
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.forever();
        }
    }
}
