package io.openems.edge.bridge.mqtt.api;

public interface MqttPublishTask extends MqttTask {

    void alterPayload(String payload);
}
