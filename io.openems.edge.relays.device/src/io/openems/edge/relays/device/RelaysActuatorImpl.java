package io.openems.edge.relays.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.i2c.mcp.api.Mcp;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;

import io.openems.edge.relays.device.task.RelaysActuatorTask;
import io.openems.edge.relays.module.api.RelaysModule;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "RelaysDevice",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class RelaysActuatorImpl extends AbstractOpenemsComponent implements ActuatorRelaysChannel, OpenemsComponent {

    private Mcp allocatedMcp;

    @Reference
    protected ComponentManager cpm;

    public RelaysActuatorImpl() {
        super(OpenemsComponent.ChannelId.values(),
                ActuatorRelaysChannel.ChannelId.values());
    }

    private boolean relaysValue = false;

    @Activate
    void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        allocateRelaisValue(config.relaysType());
        this.isCloser().setNextValue(relaysValue);
        if (cpm.getComponent(config.relaysBoard_id()) instanceof RelaysModule) {
            RelaysModule relaysModule = cpm.getComponent(config.relaysBoard_id());
            if (relaysModule.getId().equals(config.relaysBoard_id())) {
                Mcp mcp = relaysModule.getMcp();
                allocatedMcp = mcp;
                //Relais is always "off" on activation in OSGi --> Means closer and opener will be off
                mcp.setPosition(config.position(), !this.isCloser().getNextValue().get());
                //Value if it's deactivated Opener will be opened and Closer will be opened
                mcp.addToDefault(config.position(), !this.isCloser().getNextValue().get());
                /* if closer should be off and Closer on uncomment the following code:
                *  mcp.setPosition(config.position(), 0);
                *  mcp.addToDefault(config.position(), 0);
                * */
                mcp.shift();
                mcp.addTask(config.id(), new RelaysActuatorTask(mcp, config.position(),
                        !this.relaysValue, this.getRelaysChannel(),
                        config.relaysBoard_id()));
            }
        }
    }


    private void allocateRelaisValue(String relaisType) {
        switch (relaisType) {

            case "Closer":
            case "Reverse":
                this.relaysValue = true;

                break;
            default:
                this.relaysValue = false;

        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        allocatedMcp.removeTask(this.id());
    }

    @Override
    public String debugLog() {
        if (this.getRelaysChannel().getNextValue().isDefined()) {
            String onOrOff = "off";
            if (this.getRelaysChannel().getNextValue().get()) {
                onOrOff = "on";
            }
            return "Status of " + super.id() + " alias: " + super.alias() + " is " + onOrOff;
        } else {
            return "";
        }
    }


}
