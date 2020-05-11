package io.openems.edge.rest.communicator;

import io.openems.edge.bridge.rest.communcation.api.RestBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.rest.communicator.api.RestCommunicator;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Base64;
@Designate(ocd = Config.class, factory = true)
@Component(name = "Rest.Remote.Communicator", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class RestCommunicatorImpl extends AbstractOpenemsComponent implements OpenemsComponent, RestCommunicator {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    RestBridge restBridge;


    public RestCommunicatorImpl() {
        super(OpenemsComponent.ChannelId.values(),
                RestCommunicator.ChannelId.values());

    }

    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            this.isMaster().setNextValue(config.isMaster());
            restBridge.addCommunicator(super.id(), config.ipAddress(), config.port(),Base64.getEncoder().encodeToString((config.username() + ":" + config.password()).getBytes()));
        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        restBridge.removeCommunicator(super.id());
    }


}
