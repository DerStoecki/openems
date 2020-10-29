package io.openems.edge.bridge.mqtt.api;

import com.google.gson.JsonObject;
import io.openems.edge.common.channel.Channel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The concrete Implementation of the AbstractMqttTask. This component handles it's payload by getting a map of
 * the Channels it has to publish.
 * See Code for details.
 */
public class PublishTask extends AbstractMqttTask implements MqttPublishTask {


    public PublishTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime,
                       int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask, PayloadStyle style, String id) {
        super(topic, type, retainFlag, useTime, qos, priority, channelMapForTask,
                payloadForTask, timeToWait, style, id);

    }

    /**
     * Updates the Payload. Usually called from MqttManager.
     *
     * @param now the Timestamp as a string.
     */
    @Override
    public void updatePayload(String now) {
        switch (super.style) {

            case STANDARD:
            default:
                createStandardPayload(now);
                break;
        }
    }

    /**
     * Creates the StandardPayload from a Config.
     *
     * @param now if Time should be added, now is added to the Payload.
     */
    private void createStandardPayload(String now) {


        JsonObject payload = new JsonObject();
        if (getAddTime()) {
            payload.addProperty("time", now);
        }
        payload.addProperty("ID", super.id);
        AtomicInteger tokenCounter = new AtomicInteger(0);
        String[] configuredPayload = super.configuredPayload.split(":");
        Map<String, String> keyValue = new HashMap<>();
        AtomicInteger jsonCounter = new AtomicInteger(0);
        Arrays.stream(configuredPayload).forEachOrdered(consumer -> {
            if (jsonCounter.get() % 2 == 0) {
                String value = "Not Defined Yet";
                Channel<?> channel = super.channels.get(configuredPayload[jsonCounter.incrementAndGet()]);
                if (channel.value().isDefined()) {
                    value = channel.value().get() + channel.channelDoc().getUnit().getSymbol();
                }
                payload.addProperty(consumer, value);
                jsonCounter.getAndIncrement();
            }
        });
        //UPDATED PAYLOAD saved.
        super.payloadToOrFromBroker = payload.toString();
    }

}

