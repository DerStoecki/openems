package io.openems.edge.bridge.mqtt.api;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;

public interface MqttConnectionSubscribe {

    void subscribeToTopic(String topic, int qos, String id) throws MqttException;

    String getPayload(String topic);

    List<String> getTopic(String id);

    boolean isConnectionLost();
}
