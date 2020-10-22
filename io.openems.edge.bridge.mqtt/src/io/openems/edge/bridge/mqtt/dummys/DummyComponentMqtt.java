package io.openems.edge.bridge.mqtt.dummys;

import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.bridge.mqtt.component.AbstractMqttComponent;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Designate(ocd = ConfigDummyComponent.class, factory = true)
@Component(name = "Dummy.Mqtt.Component",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class DummyComponentMqtt extends AbstractOpenemsComponent implements OpenemsComponent, MqttComponent, DummyChannels {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    MqttBridge mqttBridge;


    @Reference
    ConfigurationAdmin ca;


    private MqttComponentDummyImpl component;


    public DummyComponentMqtt() {
        super(OpenemsComponent.ChannelId.values(),
                MqttComponent.ChannelId.values(),
                DummyChannels.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, ConfigDummyComponent config) throws MqttException, ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());
        Configuration c = null;
        try {
            c = ca.getConfiguration(this.servicePid(), "?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> subList = Arrays.asList(config.subscriptionList());
        List<String> pubList = Arrays.asList(config.publishList());
        List<String> payloads = Arrays.asList(config.payloads());
        List<Channel<?>> channels = new ArrayList<>(this.channels());
        this.component = new MqttComponentDummyImpl(super.id(), subList, pubList, payloads,
                config.createdByOsgiConfig(), mqttBridge);
        this.component.update(c, "channelIdList", channels, config.channelIdList().length);
        if (this.component.hasBeenConfigured() && config.configurationDone() == true) {
            this.component.initTasks(channels);
        }
        this.getDummyOne().setNextValue(10);
        this.mqttBridge.addMqttComponent(super.id(), this);
    }

    @Override
    public void reactToEvent() {
        if (this.getEvents().value().isDefined() && this.getEventValue().value().isDefined()) {
            System.out.println("REACTING TO: " + this.getEvents().value().get() + " WITH VALUE: "
                    + this.getEventValue().value().get());
        } else {
            System.out.println("No Value for Events yet");
        }
    }

    @Override
    public void reactToCommand() {
        this.mqttBridge.getSubscribeTasks(super.id()).stream().filter(entry -> entry.getMqttType().equals(MqttType.COMMAND)).collect(Collectors.toList()).forEach(entry -> {
            if (entry instanceof MqttSubscribeTask) {
                MqttSubscribeTask task = (MqttSubscribeTask) entry;
                task.getCommandValues().forEach((key, value) -> {
                    if (!expired(task, key))
                        reactToComponentCommand(key, value);
                });
            }
        });
        if (this.getCommands().value().isDefined() && this.getCommandsValue().value().isDefined()) {
            System.out.println("REACTING TO: " + this.getCommands().value().get() + " WITH VALUE: "
                    + this.getCommandsValue().value().get());
        } else {
            System.out.println("No Value for Commands yet");
        }
    }

    private boolean expired(MqttSubscribeTask task, MqttCommandType key) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(task.getTime().getTime() + Long.parseLong(task.getCommandValues().get(key).getExpiration()));
        return now.after(expiration);

    }

    private void reactToComponentCommand(MqttCommandType key, CommandWrapper value) {
        switch (key) {
            case SETPOWER:
                System.out.println("SET POWER WILL BE SET");
                this.getPower().setNextValue(value);
                System.out.println(this.getPower().getNextValue().get());
                break;
            case SETPERFORMANCE:
                break;
            case SETSCHEDULE:
                break;
            case SETTEMPERATURE:
                break;
        }
    }

    @Override
    public void updateJsonConfig() throws MqttException, ConfigurationException {
        if (this.getConfiguration().value().isDefined()) {
            String configuration = this.getConfiguration().value().get();
            this.component.initJson(new ArrayList<>(this.channels()), configuration);
            this.getConfiguration().setNextValue("");
        }
    }


    private static class MqttComponentDummyImpl extends AbstractMqttComponent {

        /**
         * Initially update Config and after that set params for initTasks.
         *
         * @param id            id of this Component, usually from configuredDevice and it's config.
         * @param subConfigList Subscribe ConfigList, containing the Configuration for the subscribeTasks.
         * @param pubConfigList Publish Configlist, containing the Configuration for the publishTasks.
         * @param payloads      containing all the Payloads. ConfigList got the Payload list as well.
         * @param createdByOsgi is this Component configured by OSGi or not. If not --> Read JSON File/Listen to Configuration Channel.
         * @param mqttBridge    The MqttBridge of this Component
         */
        MqttComponentDummyImpl(String id, List<String> subConfigList,
                               List<String> pubConfigList, List<String> payloads,
                               boolean createdByOsgi, MqttBridge mqttBridge) {
            super(id, subConfigList, pubConfigList, payloads, createdByOsgi, mqttBridge);
        }
    }

    @Deactivate
    public void deactivate() {
        this.component.deactivate();
        mqttBridge.removeMqttComponent(this.id());
    }

}
