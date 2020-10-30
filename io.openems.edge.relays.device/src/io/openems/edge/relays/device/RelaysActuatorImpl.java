package io.openems.edge.relays.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.bridge.mqtt.component.MqttConfigurationComponent;
import io.openems.edge.bridge.mqtt.component.MqttConfigurationComponentImpl;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.i2c.mcp.api.Mcp;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;

import io.openems.edge.relays.device.task.RelaysActuatorTask;
import io.openems.edge.relays.module.api.RelaysModule;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Device.Relays",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class RelaysActuatorImpl extends AbstractOpenemsComponent implements ActuatorRelaysChannel, OpenemsComponent, MqttComponent {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    MqttBridge mqttBridge;

    private final Logger log = LoggerFactory.getLogger(RelaysActuatorImpl.class);

    private Mcp allocatedMcp;

    @Reference
    protected ComponentManager cpm;

    @Reference
    protected ConfigurationAdmin ca;

    private MqttConfigurationComponent mqttConfigurationComponent;

    public RelaysActuatorImpl() {
        super(OpenemsComponent.ChannelId.values(),
                MqttComponent.ChannelId.values(),
                ActuatorRelaysChannel.ChannelId.values());
    }

    private boolean relaysValue = false;

    @Activate
    void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, MqttException, ConfigurationException, IOException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.useMqtt()) {
            configureMqtt(config);
        }
        allocateRelaysValue(config.relaysType());
        this.isCloser().setNextValue(relaysValue);
        if (cpm.getComponent(config.relaysBoard_id()) instanceof RelaysModule) {
            RelaysModule relaysModule = cpm.getComponent(config.relaysBoard_id());
            if (relaysModule.getId().equals(config.relaysBoard_id())) {
                Mcp mcp = relaysModule.getMcp();
                allocatedMcp = mcp;
                // Relays is always "off" on activation in OSGi --> Means closer and opener will be off
                //mcp.setPosition(config.position(), !this.isCloser().getNextValue().get());
                // Value if it's deactivated Opener will be opened and Closer will be opened
                //mcp.addToDefault(config.position(), !this.isCloser().getNextValue().get());
                // if closer should be off(Normally Open) and Opener on (Normally Closed) uncomment the following code:
                mcp.setPosition(config.position(), false);
                mcp.addToDefault(config.position(), false);
                mcp.shift();
                mcp.addTask(config.id(), new RelaysActuatorTask(config.position(),
                        this.getRelaysChannel(),
                        config.relaysBoard_id(), !relaysValue));
            }
        }
    }

    private void configureMqtt(Config config) throws MqttException, ConfigurationException, IOException {
        this.mqttConfigurationComponent = new MqttConfigurationComponentImpl(config.subscriptionList(), config.publishList(),
                config.payloads(), super.id(), config.createdByOsgi(), this.mqttBridge, config.mqttId());
        List<Channel<?>> channels = new ArrayList<>(this.channels());
        this.mqttConfigurationComponent.update(ca.getConfiguration(this.servicePid(), "?"), "channelIdList",
                channels, config.channelIdList().length);
        if(!config.createdByOsgi() && !config.pathForJson().trim().equals("")){
            this.mqttConfigurationComponent.initJson(new ArrayList<>(this.channels()), config.pathForJson());
        }
        if (this.mqttConfigurationComponent.hasBeenConfigured() && config.configurationDone()) {
            this.mqttConfigurationComponent.initTasks(new ArrayList<>(this.channels()));
            this.mqttBridge.addMqttComponent(super.id(), this);
        }
    }


    private void allocateRelaysValue(String relaysType) {
        this.relaysValue = "Closer".equals(relaysType);
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        allocatedMcp.removeTask(this.id());
    }

    @Override
    public String debugLog() {
        if (this.getRelaysChannel().getNextValue().isDefined()) {
            String onOrOff = "deactivated";
            if (this.getRelaysChannel().getNextValue().get()) {
                onOrOff = "activated";
            }
            return "Status of " + super.id() + " alias: " + super.alias() + " is " + onOrOff + "\n";
        } else {
            return "\n";
        }
    }


    @Override
    public void reactToEvent() {

    }

    @Override
    public void reactToCommand() {

        this.mqttBridge.getSubscribeTasks(super.id()).stream().filter(entry -> entry.getMqttType()
                .equals(MqttType.COMMAND)).collect(Collectors.toList()).forEach(entry -> {
            if (entry instanceof MqttSubscribeTask) {
                MqttSubscribeTask task = (MqttSubscribeTask) entry;
                task.getCommandValues().forEach((key, value) -> {
                    if (this.mqttConfigurationComponent.expired(task, value)) {
                        reactToComponentCommand(key, value);
                    }
                });
            }
        });
    }

    @Override
    public void updateJsonConfig() throws MqttException, ConfigurationException {

    }

    @Override
    public boolean isConfigured() {
        return this.mqttConfigurationComponent.isConfigured();
    }

    private void reactToComponentCommand(MqttCommandType key, CommandWrapper value) {
        switch (key) {
            case SETPOWER:
                try {
                    this.getRelaysChannel().setNextWriteValue(Boolean.parseBoolean(value.getValue()));
                } catch (OpenemsError.OpenemsNamedException e) {
                    log.warn("Couldn't write : " + value.getValue() + "To the Channel: " + this.getRelaysChannel().channelId());
                }
            default:
                log.warn("Command " + key + " is not supported by Class " + this.getClass());
        }
    }
}
