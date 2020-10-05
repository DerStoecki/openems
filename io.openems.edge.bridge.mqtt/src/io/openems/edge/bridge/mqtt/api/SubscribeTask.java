package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.util.*;
import java.util.stream.Collectors;


public class SubscribeTask extends AbstractMqttTask implements MqttSubscribeTask {

    private int messageId;
    //original nameIDs and their position
    private List<String> nameIds;
    //original channelId position
    private List<String> channelIds;

    private Map<String, String> nameIdAndChannelIdMap;

    public SubscribeTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime,
                         int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask,
                         PayloadStyle payloadStyle, String id) {
        super(topic, type, retainFlag, useTime, qos, priority, channelMapForTask, payloadForTask, timeToWait,
                payloadStyle, id);
        this.nameIds = new ArrayList<>();

        //ChannelID --> Used to identify value the pub tasks get / value to put for sub task
        this.channelIds = new ArrayList<>();
        this.nameIdAndChannelIdMap = new HashMap<>();
        String[] tokens = payloadForTask.split(":");

        for (int x = 0; x < tokens.length; x += 2) {
            this.nameIdAndChannelIdMap.put(tokens[x], tokens[x + 1]);
        }
    }

    @Override
    public void response(String payload) {
        //TODO PAYLOAD update
        super.payloadToOrFromBroker = payload;
        switch (super.style) {

            case STANDARD:
            default:
                standardResponse();
        }
    }

    /**
     * Standard Response for subscription.
     * <p>Each ID from broker has a value.
     * message contains {
     * "SentOnDate": Timestamp,
     * "NameOfBrokerParam": "ID of Sensor"
     * "metrics":{
     * "NameOfBrokerParam": "Value for Param"
     * }
     * }
     * </p>
     * <p>
     * The name of broker param after metrics --> has a value, this value will be written into an Openems channel.
     * It either writes directly in the channel and sets something (e.g. subscribe to telemetry)
     * or
     * MqttType --> Each MqttComponent got a channel for corresponding MqttType and therefore each component can react to
     * entrys of such channel.
     * </p>
     * <p>
     * standard Response works as follows:
     * Replace the String with  "" if it is not a Alphanumeric a decimal or a : ... the leftover string will be split
     * at the :
     * the first part is for id/Name of broker params. the second part is for value.
     * The Id/Name of broker params was already saved and therefore the index of the id matches the index of the corresponding ChannelId in the list.
     * After that the Map where the ChannelId with the corresponding Channel is stored can be called and the next Value can be set.
     * </p>
     */
    private void standardResponse() {
        String response = super.payloadToOrFromBroker;
        if (response.equals("")) {
            return;
        }
        Map<String, String> idChannelValueMap = new HashMap<>();
        String[] tokensWithTime = {null};
        if (response.contains("sentOn")) {
            //TODO SAVE THE TIME AND PUT IT TO A DATE TIME
            String responseContainsTime = response.replaceAll(("[^A-Za-z0-9.:,]"), "");
            tokensWithTime = responseContainsTime.split(",");
            response = Arrays.stream(tokensWithTime).filter(entry -> !entry.contains("sentOn")).collect(Collectors.toList()).toString();
        }
        String[] tokens;
        response = response.replaceAll(",", ":");
        if (tokensWithTime.length > 0) {
            response = response.replaceAll(("[^A-Za-z0-9.:]"), "");
        }
        tokens = response.split(":");
        if (tokens.length > 0) {
            for (int x = 0; x < tokens.length; ) {
                if (tokens[x].equals("metrics")) {
                    x++;
                } else if (tokens[x].equals("sentOn")) {
                    x += 3;
                } else {
                    if (!tokens[x].equals("ID")) {
                        idChannelValueMap.put(tokens[x], tokens[x + 1]);
                    }
                    x += 2;
                }
            }
        }
        idChannelValueMap.forEach((key, value) -> {
            //index of nameIds is the same as for ChannelIds.
            if (this.nameIdAndChannelIdMap.containsKey(key) && !value.equals("Not Defined Yet")) {
                String channelId = this.nameIdAndChannelIdMap.get(key);
                Channel<?> channel = super.channels.get(channelId);
                channel.setNextValue(value);
                System.out.println("Update Channel: " + channelId + "with Value: " + value);
            } else {
                System.out.println("Value not defined yet for: " + this.nameIdAndChannelIdMap.get(key));
            }
        });

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
