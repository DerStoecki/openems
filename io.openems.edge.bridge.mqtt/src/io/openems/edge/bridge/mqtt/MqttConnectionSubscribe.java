package io.openems.edge.bridge.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;


public class MqttConnectionSubscribe extends AbstractMqttConnection implements MqttCallback {

    //          Topics  Payload
    private Map<String, String> subscriptions = new HashMap<>();
    //          ID      Topic
    private Map<String, List<String>> idsAndTopics = new HashMap<>();


    //private String topic = "";
    //private String payload = "";
    private boolean connectionLost;


    MqttConnectionSubscribe(boolean timeStampEnabled, String timeDataFormat, String locale) {
        super(timeStampEnabled, timeDataFormat, locale);
    }

    //TODO SUBSCRIBE TO TOPIC
    void subscribeToTopic(String topic, int qos, String id) throws MqttException {

        super.mqttClient.subscribe(topic, qos);
        mqttClient.setCallback(this);
        this.subscriptions.put(topic, "");
        addTopicList(id, topic);
    }


    private void addTopicList(String id, String topic) {
        if (this.idsAndTopics.containsKey(id)) {
            this.idsAndTopics.get(id).add(topic);
        } else {
            List<String> topicList = new ArrayList<>();
            topicList.add(topic);
            this.idsAndTopics.put(id, topicList);
        }
    }

    String getPayload(String topic) {
        if (this.subscriptions.containsKey(topic)) {
            return this.subscriptions.get(topic);
        }

        return "";
    }


    List<String> getTopic(String id) {
        return this.idsAndTopics.getOrDefault(id, null);
    }


    //TODO
    @Override
    public void connectionLost(Throwable throwable) {
        this.connectionLost = true;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.subscriptions.replace(topic, new String(message.getPayload(), StandardCharsets.UTF_8));
    }

    //TODO PUBACK AND PUBCOMP
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public boolean isConnectionLost() {
        return connectionLost;
    }

}
