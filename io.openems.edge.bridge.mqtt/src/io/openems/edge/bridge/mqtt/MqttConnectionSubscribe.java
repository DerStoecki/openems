package io.openems.edge.bridge.mqtt;

public class MqttConnectionSubscribe extends AbstractMqttConnection {

    MqttConnectionSubscribe(boolean timeStampEnabled, String timeDataFormat) {
        super(timeStampEnabled, timeDataFormat);
    }

    //TODO SUBSCRIBE TO TOPIC
    public void SubscribeToTopic() {

    }

    //TODO HANDLE TOPIC
    public String getPayloadFromTopic() {
        return "";
    }
}
