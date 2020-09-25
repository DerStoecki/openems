package io.openems.edge.bridge.mqtt.api;

public class DummySubscribeTask extends AbstractMqttTask implements MqttSubscribeTask {

    private int messageId = -1;


    public DummySubscribeTask(String topic, MqttType mqttType, boolean retainFlag, boolean addTime, int qos,
                              MqttPriority priority) {
        super(topic, mqttType, retainFlag, addTime, qos, priority);
    }

    @Override
    public void response(String payload) {
        super.payload = payload;
        System.out.println("Subscribe Task " +  "received Payload : " + payload);
    }

    @Override
    public void putMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

}
