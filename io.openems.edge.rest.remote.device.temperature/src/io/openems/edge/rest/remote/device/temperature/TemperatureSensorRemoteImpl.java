package io.openems.edge.rest.remote.device.temperature;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.api.RestBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.rest.communicator.api.RestCommunicator;
import io.openems.edge.rest.remote.device.temperature.task.TemperatureSensorRemoteReadTask;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Rest.Remote.TemperatureSensor", immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)

public class TemperatureSensorRemoteImpl extends AbstractOpenemsComponent implements OpenemsComponent, Thermometer {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    RestBridge restBridge;

    @Reference
    ComponentManager cpm;

    private RestCommunicator communicator;

    private String slaveMasterId;
    private TemperatureSensorRemoteReadTask task;


    public TemperatureSensorRemoteImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Thermometer.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException, OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {

            if (cpm.getComponent(config.slaveMasterId()) instanceof RestCommunicator) {
                communicator = cpm.getComponent(config.slaveMasterId());
                this.slaveMasterId = config.slaveMasterId();
                task = new TemperatureSensorRemoteReadTask(super.id(), slaveMasterId,
                        communicator.isMaster().getNextValue().get(), config.realTemperatureSensorId(), this.getTemperature());

                restBridge.addRestRequest(slaveMasterId, task);
            } else {
                throw new ConfigurationException(config.slaveMasterId(), "Master Slave Id Incorrect or not configured yet!");
            }

        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        restBridge.removeRestRemoteDevice(super.id(), slaveMasterId);
    }

    @Override
    public String debugLog() {

        if (restBridge.getRequests(this.slaveMasterId).contains(task)) {
            return "T:" + this.getTemperature().value().asString() + " of RemoteTemperatureSensor: " + super.id() + super.alias()
                    + "\n";
        } else {
            return "\n";
        }
    }

}
