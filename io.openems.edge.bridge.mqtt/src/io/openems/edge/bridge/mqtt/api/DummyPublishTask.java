package io.openems.edge.bridge.mqtt.api;


public class DummyPublishTask extends AbstractMqttTask implements MqttPublishTask {

    public DummyPublishTask(String topic, String payload, MqttType mqttType, boolean retainFlag,
                            boolean addTime, int qos, MqttPriority priority) {
        super(topic, mqttType, retainFlag, addTime, qos, priority);
        super.payload = payload;
    }


    @Override
    public void alterPayload(String payload) {
        super.payload = payload;
    }
}
