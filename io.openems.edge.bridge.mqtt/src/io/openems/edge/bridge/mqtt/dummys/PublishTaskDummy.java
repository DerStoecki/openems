package io.openems.edge.bridge.mqtt.dummys;


import io.openems.edge.bridge.mqtt.api.MqttPriority;
import io.openems.edge.bridge.mqtt.api.MqttPublishTask;
import io.openems.edge.bridge.mqtt.api.MqttType;

public class PublishTaskDummy extends DummyAbstractMqttTask implements MqttPublishTask {

    public PublishTaskDummy(String topic, String payload, MqttType mqttType, boolean retainFlag,
                            boolean addTime, int qos, MqttPriority priority) {
        super(topic, mqttType, retainFlag, addTime, qos, priority);
        super.payload = payload;
    }

    @Override
    public void updatePayload(String now) {

    }
}
