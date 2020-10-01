package io.openems.edge.bridge.mqtt.connection;

import io.openems.edge.bridge.mqtt.api.MqttConnectionPublish;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttConnectionPublishImpl extends AbstractMqttConnection implements MqttConnectionPublish {

    public MqttConnectionPublishImpl() {
        super();
    }

    //TODO MAYBE MORE STUFF
    public void sendMessage(String topic, String message, int qos, boolean retainFlag) throws MqttException {
        MqttMessage messageMqtt;

        messageMqtt = new MqttMessage(message.getBytes());
        messageMqtt.setQos(qos);
        messageMqtt.setRetained(retainFlag);
        super.mqttClient.publish(topic, messageMqtt);
        System.out.println("Message published: " + messageMqtt);
    }

    void sendMessage(String topic, String message, int qos) throws MqttException {
        sendMessage(topic, message, qos, false);
    }

}
