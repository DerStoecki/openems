package io.openems.edge.bridge.mqtt.component;

import io.openems.edge.bridge.mqtt.api.CommandWrapper;
import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.common.channel.Channel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationException;

import java.util.List;

public interface MqttConfigurationComponent {

    void initTasks(List<Channel<?>> channels) throws MqttException, ConfigurationException;

    boolean hasBeenConfigured();

    boolean expired(MqttSubscribeTask task, CommandWrapper key);

    void update(Configuration configuration, String channelIdList, List<Channel<?>> channels, int length);

    boolean isConfigured();
}
