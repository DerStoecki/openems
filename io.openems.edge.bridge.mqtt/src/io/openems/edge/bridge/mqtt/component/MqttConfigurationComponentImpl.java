package io.openems.edge.bridge.mqtt.component;

import io.openems.edge.bridge.mqtt.api.MqttBridge;
import io.openems.edge.bridge.mqtt.api.MqttCommandType;
import io.openems.edge.bridge.mqtt.api.MqttComponent;
import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.common.channel.Channel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MqttConfigurationComponentImpl implements MqttConfigurationComponent{


    private MqttComponentImpl mqttComponent;

    public MqttConfigurationComponentImpl(String[] subscribtions, String[] publish, String[] payloads, String id,
                                          boolean createdByOsgi, MqttBridge mqttBridge){
        this.mqttComponent = new MqttComponentImpl(id, Arrays.asList(subscribtions), Arrays.asList(publish),
                Arrays.asList(payloads), createdByOsgi, mqttBridge);
    }

    @Override
    public void initTasks(List<Channel<?>> channels) throws MqttException, ConfigurationException {
        this.mqttComponent.initTasks(channels);
    }

    @Override
    public boolean hasBeenConfigured() {
        return this.mqttComponent.hasBeenConfigured();
    }

    @Override
    public boolean expired(MqttSubscribeTask task, MqttCommandType key) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(task.getTime().getTime() + Long.parseLong(task.getCommandValues().get(key).getExpiration()));
        return now.after(expiration);
    }

    @Override
    public void update(Configuration configuration, String channelIdList, List<Channel<?>> channels, int length) {
        this.mqttComponent.update(configuration, channelIdList, channels, length);
    }


    private class MqttComponentImpl extends AbstractMqttComponent {
        /**
         * Initially update Config and after that set params for initTasks.
         *
         * @param id            id of this Component, usually from configuredDevice and it's config.
         * @param subConfigList Subscribe ConfigList, containing the Configuration for the subscribeTasks.
         * @param pubConfigList Publish Configlist, containing the Configuration for the publishTasks.
         * @param payloads      containing all the Payloads. ConfigList got the Payload list as well.
         * @param createdByOsgi is this Component configured by OSGi or not. If not --> Read JSON File/Listen to Configuration Channel.
         * @param mqttBridge    mqttBridge of this Component.
         */
        public MqttComponentImpl(String id, List<String> subConfigList, List<String> pubConfigList, List<String> payloads,
                                 boolean createdByOsgi, MqttBridge mqttBridge) {
            super(id, subConfigList, pubConfigList, payloads, createdByOsgi, mqttBridge);
        }
    }



}
