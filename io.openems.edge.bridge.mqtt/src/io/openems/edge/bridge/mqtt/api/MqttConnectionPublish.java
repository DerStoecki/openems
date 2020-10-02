package io.openems.edge.bridge.mqtt.api;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface MqttConnectionPublish {
    //TODO MAYBE MORE STUFF
    void sendMessage(String topic, String message, int qos, boolean retainFlag) throws MqttException;
}
