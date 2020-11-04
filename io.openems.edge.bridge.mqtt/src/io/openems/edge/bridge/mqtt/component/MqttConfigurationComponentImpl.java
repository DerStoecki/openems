package io.openems.edge.bridge.mqtt.component;

import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.channel.Channel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MqttConfigurationComponentImpl implements MqttConfigurationComponent {


    private MqttComponentImpl mqttComponent;
    private boolean configured;

    public MqttConfigurationComponentImpl(String[] subscriptions, String[] publish, String[] payloads, String id,
                                          boolean createdByOsgi, MqttBridge mqttBridge, String mqttId) {
        this.mqttComponent = new MqttComponentImpl(id, Arrays.asList(subscriptions), Arrays.asList(publish),
                Arrays.asList(payloads), createdByOsgi, mqttBridge, mqttId);
    }

    @Override
    public void initTasks(List<Channel<?>> channels) throws MqttException, ConfigurationException {
        try {
            this.mqttComponent.initTasks(channels);
            this.configured = true;
        } catch (MqttException | ConfigurationException e) {
            configured = false;
            throw e;
        }
    }

    @Override
    public boolean hasBeenConfigured() {
        return this.mqttComponent.hasBeenConfigured();
    }

    @Override
    public boolean expired(MqttSubscribeTask task, CommandWrapper key) {
        return this.mqttComponent.expired(task, Integer.parseInt(key.getExpiration()));
    }

    @Override
    public void update(Configuration configuration, String channelIdList, List<Channel<?>> channels, int length) {
        this.mqttComponent.update(configuration, channelIdList, channels, length);
    }

    @Override
    public boolean isConfigured() {
        return this.configured;
    }

    @Override
    public void initJson(ArrayList<Channel<?>> channels, String pathForJson) throws IOException, ConfigurationException {
        this.mqttComponent.initJsonFromFile(channels, pathForJson);
    }

    @Override
    public void updateJsonByChannel(ArrayList<Channel<?>> channels, String config) throws ConfigurationException {
        this.mqttComponent.initJson(channels, config);
    }


    private class MqttComponentImpl extends AbstractMqttComponent {
        /**
         * Initially update Config and after that set params for initTasks.
         *  @param id            id of this Component, usually from configuredDevice and it's config.
         * @param subConfigList Subscribe ConfigList, containing the Configuration for the subscribeTasks.
         * @param pubConfigList Publish ConfigList, containing the Configuration for the publishTasks.
         * @param payloads      containing all the Payloads. ConfigList got the Payload list as well.
         * @param createdByOsgi is this Component configured by OSGi or not. If not --> Read JSON File/Listen to Configuration Channel.
         * @param mqttBridge    mqttBridge of this Component.
         * @param mqttId
         */
        public MqttComponentImpl(String id, List<String> subConfigList, List<String> pubConfigList, List<String> payloads,
                                 boolean createdByOsgi, MqttBridge mqttBridge, String mqttId) {
            super(id, subConfigList, pubConfigList, payloads, createdByOsgi, mqttBridge, mqttId);
        }
    }


}
