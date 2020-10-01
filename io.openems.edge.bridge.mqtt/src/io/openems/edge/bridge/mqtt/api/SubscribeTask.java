package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Map;

public class SubscribeTask extends DummyAbstractMqttTask implements MqttSubscribeTask {

    private int messageId;

    public SubscribeTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime,
                         int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask,
                         PayloadStyle payloadStyle) {
        super(topic, type, retainFlag, useTime, qos, priority);
    }

    @Override
    public void response(String payload) {
        //TODO PAYLOAD update
    }

    @Override
    public void putMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public int getMessageId() {
        return this.messageId;
    }
}
