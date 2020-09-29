package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PublishTask extends AbstractMqttTask implements MqttPublishTask {

    private PayloadStyle style;
    private int timeToWait;
    private Map<Integer, Channel<?>> channels;
    private String payload;
    private long timeStamp = -1;
    private boolean readyToAlterPayload;

    public PublishTask(String topic, MqttType mqttType, boolean retainFlag, int qos, boolean useTimestamp, MqttPriority mqttPriority,
                       String payload, Map<Integer, Channel<?>> channelMap, int timeToWait, PayloadStyle style) {

        super(topic, mqttType, retainFlag, useTimestamp, qos, mqttPriority);
        this.style = style;
        this.channels = channelMap;
        this.timeToWait = timeToWait;
        readyToAlterPayload = true;
        timeStamp = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
        alterPayload(payload);
    }

    @Override
    public void alterPayload(String payload) {
        if (this.readyToAlterPayload) {
            this.payload = payload;

            //TODO create Payload to add to super
        }
    }

    public void readyToAlterPayload(long currentTime) {
        if (timeStamp - currentTime >= timeToWait) {

            this.readyToAlterPayload = true;

        }
        this.readyToAlterPayload = false;
    }

    @Override
    public void updatePayload() {
        this.alterPayload(this.payload);
    }

}

