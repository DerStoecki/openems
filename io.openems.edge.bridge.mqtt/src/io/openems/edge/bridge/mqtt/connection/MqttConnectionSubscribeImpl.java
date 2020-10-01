package io.openems.edge.bridge.mqtt.connection;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openems.edge.bridge.mqtt.api.MqttConnectionSubscribe;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;


public class MqttConnectionSubscribeImpl extends AbstractMqttConnection implements MqttCallback, MqttConnectionSubscribe {

    //          Topics  Payload
    private Map<String, String> subscriptions = new HashMap<>();
    //          ID      Topic
    private Map<String, List<String>> idsAndTopics = new HashMap<>();

    private boolean connectionLost;
    private boolean callBackWasSet;

    public MqttConnectionSubscribeImpl() {
        super();
    }


    public void subscribeToTopic(String topic, int qos, String id) throws MqttException {

        super.mqttClient.subscribe(topic, qos);
        if (callBackWasSet == false) {
            mqttClient.setCallback(this);
            callBackWasSet = true;
        }
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

    public String getPayload(String topic) {
        if (this.subscriptions.containsKey(topic)) {
            return this.subscriptions.get(topic);
        }

        return "";
    }


    public List<String> getTopic(String id) {
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
