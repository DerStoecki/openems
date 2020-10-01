package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PublishTask extends AbstractMqttTask implements MqttPublishTask {


    public PublishTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime,
                       int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask, PayloadStyle style, String id) {
        super(topic, type, retainFlag, useTime, qos, priority, channelMapForTask,
                payloadForTask, timeToWait, style, id);

    }

    @Override
    public void updatePayload() {
        switch (super.style) {

            case STANDARD:
            default:
                createStandardPayload();
                break;
        }
    }

    private void createStandardPayload() {

        StringBuilder builder = new StringBuilder();
        builder.append("{\n\t");
        builder.append("ID : ").append(super.id).append(",\n\t \"metrics\" : {");
        String[] tokens = super.configuredPayload.split("!");
        AtomicInteger counter = new AtomicInteger(0);
        Arrays.stream(tokens).forEachOrdered(consumer -> {
            if (counter.get() % 2 == 0) {
                builder.append("\n\t\t");
                builder.append(tokens[counter.get()]).append(" : ");
            } else {
                Channel<?> channel = super.channels.get(tokens[counter.get()]);
                if (channel.value().isDefined()) {
                    builder.append(channel.value().get()).append(" ").append(channel.channelDoc().getUnit().getSymbol()).append(",\n\t");
                } else {
                    builder.append("Not Defined Yet ");
                    //prevent of adding , after last value
                    if (counter.get() < tokens.length - 1) {
                        builder.append(",");
                    }
                }
            }
            counter.getAndIncrement();
        });
        builder.append("\n\t}\n}");

        super.payloadToOrFromBroker = builder.toString();
    }

}

