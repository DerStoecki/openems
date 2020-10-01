package io.openems.edge.bridge.mqtt.api;


public class PublishTaskDummy extends DummyAbstractMqttTask implements MqttPublishTask {

    public PublishTaskDummy(String topic, String payload, MqttType mqttType, boolean retainFlag,
                            boolean addTime, int qos, MqttPriority priority) {
        super(topic, mqttType, retainFlag, addTime, qos, priority);
        super.payload = payload;
    }


    @Override
    public void alterPayload(String payload) {
        super.payload = payload;
    }

    @Override
    public void readyToAlterPayload(long currentTime) {

    }

    @Override
    public void updatePayload() {

    }
}
