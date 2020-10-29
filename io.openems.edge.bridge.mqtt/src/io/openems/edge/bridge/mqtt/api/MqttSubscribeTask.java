package io.openems.edge.bridge.mqtt.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public interface MqttSubscribeTask extends MqttTask {

    /**
     * Called by MqttSubscribeManager. Response to Payload.
     *
     * @param payload the Payload for the concrete MqttTask.
     */
    void response(String payload);

    /**
     * MessageId of the MqttTask. Given by the MqttBridge.
     *
     * @param messageId the Number of the message.
     */

    void putMessageId(int messageId);

    int getMessageId();

    void convertTime(SimpleDateFormat formatter) throws ParseException;

    Date getTime();

    boolean timeAvailable();

    void setTime(Date date);



    Map<MqttCommandType, CommandWrapper> getCommandValues();
}
