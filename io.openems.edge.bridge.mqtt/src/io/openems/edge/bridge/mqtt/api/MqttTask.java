package io.openems.edge.bridge.mqtt.api;

public interface MqttTask {

    int getQos();

    String getTopic();

    String getPayload();

    boolean getRetainFlag();

    boolean getAddTime();

    MqttPriority getPriority();

    MqttType getMqttType();


}
