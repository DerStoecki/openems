package io.openems.edge.bridge.mqtt.api;

public interface MqttSubscribeTask extends MqttTask {


    void response(String payload);

    void putMessageId(int messageId);

    int getMessageId();
}
