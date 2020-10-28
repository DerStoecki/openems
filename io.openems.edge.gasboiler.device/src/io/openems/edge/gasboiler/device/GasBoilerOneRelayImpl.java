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


@Designate(ocd = ConfigOneRelay.class, factory = true)
@Component(name = "GasBoilerOneRelay",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class GasBoilerOneRelayImpl extends AbstractOpenemsComponent implements OpenemsComponent, GasBoiler, Heater {

    @Reference
    ConfigurationAdmin cm;

    @Reference
    ComponentManager cpm;

    private ActuatorRelaysChannel relay;
    private int thermicalOutput;

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
        this.thermicalOutput = config.maxThermicalOutput();
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
            return this.thermicalOutput;
        } else {
            try {
                if (cpm.getComponent(config.relayId()) instanceof ActuatorRelaysChannel) {
                    this.relay = cpm.getComponent(config.relayId());
                }
            } catch (OpenemsError.OpenemsNamedException e) {
                System.out.println("Couldn't find component!");


            }

        }
        return 0;
    }


    @Override
    public int getMaximumThermicalOutput() {
        return this.thermicalOutput;
    }

    @Override
    public void setOffline() throws OpenemsError.OpenemsNamedException {
        if (this.relay != null) {
            this.relay.getRelaysChannel().setNextWriteValue(false);
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
