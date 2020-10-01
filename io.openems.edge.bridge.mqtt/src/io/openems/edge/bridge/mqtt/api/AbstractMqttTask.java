package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Map;

public abstract class AbstractMqttTask implements MqttTask {
    private String topic;
    String payloadFinal = "";
    private MqttType mqttType;
    private boolean retainFlag;
    private boolean addTime;
    private int qos;
    private MqttPriority mqttPriority;
    Map<String, Channel<?>> channels;


    AbstractMqttTask(String topic, MqttType mqttType,
                     boolean retainFlag, boolean addTime, int qos, MqttPriority priority, Map<String, Channel<?>> channels,
                     String payloadForTask) {

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
        return this.payloadFinal;
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
