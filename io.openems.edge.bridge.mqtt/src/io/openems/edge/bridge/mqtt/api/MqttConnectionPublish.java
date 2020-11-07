package io.openems.edge.bridge.mqtt.api;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface MqttConnectionPublish {

    /**
     * Sends the Message to the Broker. Usually called by the PublishManager.
     *
     * @param topic      Topic of the payload.
     * @param message    Payload of the message.
     * @param qos        Quality of Service of this Message.
     * @param retainFlag Should the message be retained.
     * @throws MqttException if an error occured.
     */

    void sendMessage(String topic, String message, int qos, boolean retainFlag) throws MqttException;
}
