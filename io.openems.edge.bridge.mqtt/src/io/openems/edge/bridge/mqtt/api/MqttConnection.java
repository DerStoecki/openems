package io.openems.edge.bridge.mqtt.api;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public interface MqttConnection {

    void createMqttSubscribeSession(String mqttBroker, String mqttClientId, String username, String mqttPassword, int keepAlive) throws MqttException;

    void createMqttPublishSession(String broker, String clientId, int keepAlive, String username,
                                  String password, boolean cleanSession) throws MqttException;

    void addLastWill(String topicLastWill, String payloadLastWill, int qosLastWill, boolean shouldAddTime, boolean retainedFlag, String time);

    void connect() throws MqttException;

    void disconnect() throws MqttException;

    MqttClient getMqttClient();
}
