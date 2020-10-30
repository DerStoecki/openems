package io.openems.edge.bridge.mqtt.component;

import io.openems.edge.bridge.mqtt.api.MqttPriority;
import io.openems.edge.bridge.mqtt.api.MqttType;

class MqttJsonObject {
    private MqttType type;
    private MqttPriority priority;
    private String topic;
    private int qos;
    private boolean retain;
    private Payload payload;
}
