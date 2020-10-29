package io.openems.edge.bridge.mqtt.dummys;

import io.openems.edge.bridge.mqtt.api.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class SubscribeTaskDummy extends DummyAbstractMqttTask implements MqttSubscribeTask {

    private int messageId = -1;


    public SubscribeTaskDummy(String topic, MqttType mqttType, boolean retainFlag, boolean addTime, int qos,
                              MqttPriority priority) {
        super(topic, mqttType, retainFlag, addTime, qos, priority);
    }

    @Override
    public void response(String payload) {
        super.payload = payload;
        System.out.println("Subscribe Task " +  "received Payload : " + payload);
    }

    @Override
    public void putMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public void convertTime(DateTimeZone timeZone) throws ParseException {

    }

    @Override
    public DateTime getTime() {
        return null;
    }

    @Override
    public boolean timeAvailable() {
        return false;
    }

    @Override
    public void setTime(DateTime date) {

    }

    @Override
    public Map<MqttCommandType, CommandWrapper> getCommandValues() {
        return null;
    }

}
