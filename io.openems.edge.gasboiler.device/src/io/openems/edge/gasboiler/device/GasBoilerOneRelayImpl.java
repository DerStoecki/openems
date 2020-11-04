package io.openems.edge.gasboiler.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.gasboiler.device.api.GasBoiler;
import io.openems.edge.heater.api.Heater;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = ConfigOneRelay.class, factory = true)
@Component(name = "GasBoilerOneRelay",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class GasBoilerOneRelayImpl extends AbstractOpenemsComponent implements OpenemsComponent, GasBoiler, Heater {

    private final Logger log = LoggerFactory.getLogger(GasBoilerOneRelayImpl.class);

    @Reference
    ConfigurationAdmin cm;

    @Reference
    ComponentManager cpm;

    private ActuatorRelaysChannel relay;
    private int thermalOutput;

    ConfigOneRelay config;


    public GasBoilerOneRelayImpl() {
        super(OpenemsComponent.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, ConfigOneRelay config) throws OpenemsError.OpenemsNamedException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        this.config = config;

        if (this.cpm.getComponent(config.relayId()) instanceof ActuatorRelaysChannel) {
            this.relay = this.cpm.getComponent(config.relayId());
        }
        this.thermalOutput = config.maxThermicalOutput();
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        try {
            if (this.relay != null) {
                this.relay.getRelaysChannel().setNextWriteValue(false);
            }
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int calculateProvidedPower(int demand, float bufferValue) throws OpenemsError.OpenemsNamedException {
        if (this.relay != null && this.relay.isEnabled()) {
            this.relay.getRelaysChannel().setNextWriteValue(true);
            return this.thermalOutput;
        } else {
            try {
                if (cpm.getComponent(config.relayId()) instanceof ActuatorRelaysChannel) {
                    this.relay = cpm.getComponent(config.relayId());
                    this.relay.getRelaysChannel().setNextWriteValue(true);
                    return this.thermalOutput;
                }
            } catch (OpenemsError.OpenemsNamedException e) {
                log.warn("Couldn't find component!" + e.getMessage());
                return 0;


            }

        }
        return 0;
    }


    @Override
    public int getMaximumThermalOutput() {
        return this.thermalOutput;
    }

    @Override
    public void setOffline() throws OpenemsError.OpenemsNamedException {
        if (this.relay != null) {
            this.relay.getRelaysChannel().setNextWriteValue(false);
        }
    }

    @Override
    public int runFullPower() {
        try {
            return this.calculateProvidedPower(this.thermalOutput, 1.0f);
        } catch (OpenemsError.OpenemsNamedException e) {
            log.warn("Couldn't write demand!" + e.getMessage());
            return 0;
        }
    }

    @Override
    public String debugLog() {

        if (this.relay.getRelaysChannel().value().isDefined()) {
            String active = this.relay.getRelaysChannel().value().get() ? "active" : "not Active";
            return this.id() + " Status: " + active;
        }

        return "No Value available yet";

    }
}
