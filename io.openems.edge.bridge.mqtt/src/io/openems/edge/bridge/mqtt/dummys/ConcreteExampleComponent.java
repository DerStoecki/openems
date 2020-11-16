package io.openems.edge.bridge.mqtt.dummys;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.mqtt.api.MqttBridge;
import io.openems.edge.bridge.mqtt.api.MqttComponent;
import io.openems.edge.bridge.mqtt.api.MqttType;
import io.openems.edge.bridge.mqtt.api.CommandWrapper;
import io.openems.edge.bridge.mqtt.api.MqttCommandType;
import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.component.MqttConfigurationComponent;
import io.openems.edge.bridge.mqtt.component.MqttConfigurationComponentImpl;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is just to show how to implement a concrete MqttReadyComponent
 * Attention: This is just  for Showcasing adapt your config etc.
 */
@Designate(ocd = ConcreteExampleConfig.class, factory = true)
@Component(name = "ConcreteMqttExample",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
//Note: ADD your nature here as well
public class ConcreteExampleComponent extends AbstractOpenemsComponent implements OpenemsComponent, MqttComponent {

    //Bridge is SingletonPattern
    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    MqttBridge mqttBridge;

    private final Logger log = LoggerFactory.getLogger(ConcreteExampleComponent.class);

    @Reference
    protected ConfigurationAdmin ca;

    //This is where the magic will happen
    private MqttConfigurationComponent mqttConfigurationComponent;

    public ConcreteExampleComponent() {
        super(OpenemsComponent.ChannelId.values(),
                MqttComponent.ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, ConcreteExampleConfig config) throws OpenemsError.OpenemsNamedException, MqttException, ConfigurationException, IOException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.useMqtt()) {
            configureMqtt(config);
        }
    }

    /**
     * Due to architecture it's not possible to extend a class, thats why an extra Component (MqttConfigurationComponent)
     * needs to be implemented and activated.
     * E.g. : An extension of MBusComponent/AbstractOpenEmsComponent/ModbusComponent would mean way to much coding
     * that's why a simple class impl comes in handy.
     * Create the Configuration Component, Get the Channels, Init Tasks or Json if available (tasks can be created / updated later via REST)
     *
     * @param config your Component Config
     * @throws MqttException          Is thrown if subscription fails
     * @throws ConfigurationException if somethings wrong with the configuration
     * @throws IOException            if a JsonFile cannot be loaded
     */
    private void configureMqtt(ConcreteExampleConfig config) throws MqttException, ConfigurationException, IOException {
        this.mqttConfigurationComponent = new MqttConfigurationComponentImpl(config.subscriptionList(), config.publishList(),
                config.payloads(), super.id(), config.createdByOsgiConfig(), this.mqttBridge, config.mqttId());
        List<Channel<?>> channels = new ArrayList<>(this.channels());
        this.mqttConfigurationComponent.update(ca.getConfiguration(this.servicePid(), "?"), "channelIdList",
                channels, config.channelIdList().length);
        if (!config.createdByOsgiConfig() && !config.pathForJson().trim().equals("")) {
            this.mqttConfigurationComponent.initJson(new ArrayList<>(this.channels()), config.pathForJson());

        } else if (config.createdByOsgiConfig() && this.mqttConfigurationComponent.hasBeenConfigured() && config.configurationDone()) {
            this.mqttConfigurationComponent.initTasks(new ArrayList<>(this.channels()), config.payloadStyle());
        }
        this.mqttBridge.addMqttComponent(super.id(), this);
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        if (this.mqttConfigurationComponent != null) {
            this.mqttBridge.removeMqttComponent(super.id());
        }
    }

    /**
     * If Events are available react here to them, usually called by MqttBridge.
     */
    @Override
    public void reactToEvent() {

    }

    /**
     * Called by MqttBridge, react to commands.
     * Note: The MqttBridge handles the MqttComponent bc Not every Component needs to have an Eventhandler and be called
     * (e.g. if your component doesn't use mqtt it will be called never the less, therefore the mqtt Bridge handles calling MqttComponent)
     * Get All the SubscribeTasks of this component and filter them to a CommandList
     * Each Value has a command (Method --> See MqttCommandType which are available)
     * and A command Wrapper with a value and expiration in Seconds
     */
    @Override
    public void reactToCommand() {

        this.mqttBridge.getSubscribeTasks(super.id()).stream().filter(entry -> entry.getMqttType()
                .equals(MqttType.COMMAND)).collect(Collectors.toList()).forEach(entry -> {
            if (entry instanceof MqttSubscribeTask) {
                MqttSubscribeTask task = (MqttSubscribeTask) entry;
                task.getCommandValues().forEach((key, value) -> {
                    if (this.mqttConfigurationComponent.valueLegit(value.getValue())) {
                        if (!this.mqttConfigurationComponent.expired(task, value)) {
                            reactToComponentCommand(key, value);
                        }
                    }
                });
            }
        });
    }


    /**
     * Define what your component is capable of. Or to put in other words, what Commands should be and can be handled.
     *
     * @param key   the Command Type, those are listed in the Enum MqttCommandType
     * @param value The CommandWrapper having a Value (usually the value that should be written in a channel) and an
     *              expiration which will be handled previously.
     */
    private void reactToComponentCommand(MqttCommandType key, CommandWrapper value) {
        switch (key) {
            case SETPOWER:
                break;
            case SETTEMPERATURE:
                System.out.println("Temperature was set with value: " + value);
            default:
                log.warn("Command " + key + " is not supported by Class " + this.getClass());
        }
    }

    @Override
    public void updateJsonConfig() throws ConfigurationException, MqttException {
        this.mqttConfigurationComponent.updateJsonByChannel(new ArrayList<>(this.channels()), this.getConfiguration().value().get());
    }

    @Override
    public boolean isConfigured() {
        return this.mqttConfigurationComponent.isConfigured();
    }

}
