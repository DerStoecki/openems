package io.openems.edge.bridge.mqtt;

public class MqttConnectionSubscribe extends AbstractMqttConnection {

    MqttConnectionSubscribe(boolean timeStampEnabled, String timeDataFormat, String locale) {
        super(timeStampEnabled, timeDataFormat, locale);
    }

    //TODO SUBSCRIBE TO TOPIC
    public void SubscribeToTopic() {

    }

    //TODO HANDLE TOPIC
    public String getPayloadFromTopic() {
        return "";
    }
}
