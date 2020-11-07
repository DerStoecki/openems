package io.openems.edge.bridge.mqtt.api;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
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

    void convertTime(DateTimeZone timeZone) throws ParseException;

    DateTime getTime();

    boolean timeAvailable();

    void setTime(DateTime date);



    Map<MqttCommandType, CommandWrapper> getCommandValues();
}
