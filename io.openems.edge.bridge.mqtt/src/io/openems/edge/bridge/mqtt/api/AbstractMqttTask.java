package io.openems.edge.bridge.mqtt.api;

public abstract class AbstractMqttTask implements MqttTask {
    private String topic;
    String payload = "";
    private MqttType mqttType;
    private boolean retainFlag;
    private boolean addTime;
    private int qos;
    private MqttPriority mqttPriority;

    AbstractMqttTask(String topic, MqttType mqttType,
                     boolean retainFlag, boolean addTime, int qos, MqttPriority priority) {

        this.topic = topic;

        this.mqttType = mqttType;
        this.retainFlag = retainFlag;
        this.addTime = addTime;
        this.qos = qos;
        this.mqttPriority = priority;


    }


    @Override
    public int getQos() {
        return this.qos;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public String getPayload() {
        return this.payload;
    }

    @Override
    public boolean getRetainFlag() {
        return this.retainFlag;
    }

    @Override
    public boolean getAddTime() {
        return this.addTime;
    }

    @Override
    public MqttPriority getPriority() {
        return this.mqttPriority;
    }

    @Override
    public MqttType getMqttType() {
        return this.mqttType;
    }


}