package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Map;

public abstract class AbstractMqttTask implements MqttTask {
    private String topic;
    String payloadToOrFromBroker = "";
    private MqttType mqttType;
    private boolean retainFlag;
    private boolean addTime;
    private int qos;
    private MqttPriority mqttPriority;
    Map<String, Channel<?>> channels;
    private long timeStamp = -1;
    private int timeToWait;
    String configuredPayload;
    PayloadStyle style;
    String id;

    AbstractMqttTask(String topic, MqttType mqttType,
                     boolean retainFlag, boolean addTime, int qos, MqttPriority priority, Map<String, Channel<?>> channels,
                     String payloadForTask, int timeToWait, PayloadStyle style, String id) {

        this.topic = topic;
        this.channels = channels;
        this.mqttType = mqttType;
        this.retainFlag = retainFlag;
        this.addTime = addTime;
        this.qos = qos;
        this.mqttPriority = priority;
        this.timeToWait = timeToWait;
        this.configuredPayload = payloadForTask;
        this.style = style;
        this.id = id;

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
        return this.payloadToOrFromBroker;
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

    @Override
    public boolean isReady(long currentTime) {
        boolean isReady = false;
        if ((currentTime - timeStamp) / 1000 >= timeToWait) {
            timeStamp = currentTime;
            isReady = true;
        }
        return isReady;
    }

}
