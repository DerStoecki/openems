package io.openems.edge.bridge.mqtt.api;

import io.openems.edge.common.channel.Channel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class SubscribeTask extends AbstractMqttTask implements MqttSubscribeTask {

    private int messageId;
    //original nameIDs and their position
    private List<String> nameIds;
    //original channelId position
    private List<String> channelIds;
    //time as String, usually from payload
    private String time;
    //converted time
    private Date timeDate;
    //                                               //name in Broker   // ID of channel
    //Map of ID For Broker and ChannelID --> e.g. roomTemperature: temperature.channelId.id();
    private Map<String, String> nameIdAndChannelIdMap;
    private Map<MqttCommandType, CommandWrapper> commandValueMap;

    public SubscribeTask(MqttType type, MqttPriority priority, String topic, int qos, boolean retainFlag, boolean useTime,
                         int timeToWait, Map<String, Channel<?>> channelMapForTask, String payloadForTask,
                         PayloadStyle payloadStyle, String id) {
        super(topic, type, retainFlag, useTime, qos, priority, channelMapForTask, payloadForTask, timeToWait,
                payloadStyle, id);
        this.nameIds = new ArrayList<>();

        //ChannelID --> Used to identify value the pub tasks get / value to put for sub task
        this.channelIds = new ArrayList<>();
        this.nameIdAndChannelIdMap = new HashMap<>();
        //Important for Telemetry --> Mapping
        String[] tokens = payloadForTask.split(":");

        for (int x = 0; x < tokens.length; x += 2) {
            this.nameIdAndChannelIdMap.put(tokens[x], tokens[x + 1]);
        }
        commandValueMap = new HashMap<>();
        Arrays.stream(MqttCommandType.values()).forEach(consumer -> this.commandValueMap.put(consumer, new CommandWrapper(null, null)));
    }

    @Override
    public void response(String payload) {
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
     * "SentOnDate": time,
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
            super.configuredPayload = response;
            return;
        }

        String[] tokensWithTime = {null};
        //Contains Time
        if (response.contains("time")) {
            String responseContainsTime = response.replaceAll(("[^A-Za-z0-9.:,]"), "");
            //split after each entry
            tokensWithTime = responseContainsTime.split(",");
            //first entry is always sentOn in standard response
            this.time = tokensWithTime[0].substring(tokensWithTime[0].indexOf(':'));

            response = Arrays.stream(tokensWithTime).filter(entry -> !entry.contains("time")).collect(Collectors.toList()).toString();

        }
        String[] tokens;

        if (tokensWithTime[0] != null) {
            //"New response" --> no time
            response = response.replaceAll(("[^A-Za-z0-9.:,]"), "");
        }
        response = response.replaceAll(",", ":");
        tokens = response.split(":");

        switch (this.getMqttType()) {
            case TELEMETRY:
                standardTelemetryResponse(tokens);
                break;
            case COMMAND:
                standardCommandResponse(tokens);
                break;
            case EVENT:
                standardEventResponse(tokens);
                break;
        }

    }

    private void standardEventResponse(String[] tokens) {
        System.out.println("Events are not supported by Subscribers!");
    }

    private void standardCommandResponse(String[] tokens) {
        if (!super.getMqttType().equals(MqttType.COMMAND)) {
            return;
        }
        String lastMqttCommand = "";
        MqttCommandType mqttType = null;
        if (tokens.length > 0) {
            for (int x = 0; x < tokens.length; ) {

                switch (tokens[x]) {
                    case "method":
                        lastMqttCommand = tokens[x + 1].toUpperCase();
                        mqttType = MqttCommandType.valueOf(lastMqttCommand);
                        x += 2;
                        break;
                    case "value":
                        if (this.commandValueMap.containsKey(mqttType)) {
                            this.commandValueMap.get(mqttType).setValue(tokens[x + 1]);
                            x += 2;
                        }
                        break;
                    case "expires":
                        this.commandValueMap.get(mqttType).setValue(tokens[x + 1]);
                        x += 2;
                        break;
                    default:
                        x++;
                        break;
                }

            }
        }

    }

    private void standardTelemetryResponse(String[] tokens) {
        //Events and Commands need to be handled by Component itself, only telemetry is allowed to update Channels directly.
        if (!super.getMqttType().equals(MqttType.TELEMETRY)) {
            return;
        }
        // ID of Name in mqtt  , VALUE for the channel
        Map<String, String> idChannelValueMap = new HashMap<>();

        if (tokens.length > 0) {
            for (int x = 0; x < tokens.length; ) {
                if (tokens[x].equals("metrics")) {
                    x++;
                } else if (tokens[x].equals("time")) {
                    x += 3;
                } else {
                    if (!tokens[x].equals("ID")) {
                        idChannelValueMap.put(tokens[x], tokens[x + 1]);
                    }
                    x += 2;
                }
            }
        }
        //Set the Value of this channel for each entry
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

    @Override
    public void convertTime(SimpleDateFormat formatter) throws ParseException {
        if (!this.time.equals("")) {
            this.timeDate = formatter.parse(this.time);
        }
    }

    @Override
    public Date getTime() {
        return timeDate;
    }

    @Override
    public boolean timeAvailable() {
        return this.timeDate == null;
    }

    @Override
    public void setTime(Date date) {
        this.timeDate = date;
    }

    @Override
    public Map<MqttCommandType, CommandWrapper> getCommandValues() {
        return this.commandValueMap;
    }


}
