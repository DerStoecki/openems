package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Map;

public class PublishTask extends DummyAbstractMqttTask implements MqttPublishTask {

    private PayloadStyle style;
    private int timeToWait;
    private Map<String, Channel<?>> channels;
    private String payload;
    private long timeStamp = -1;
    private boolean readyToAlterPayload;

    public PublishTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime, int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask, PayloadStyle standard) {
        super(topic, type, retainFlag, useTime, qos, priority);
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

