package io.openems.edge.bridge.mqtt.dummys;

import io.openems.edge.bridge.mqtt.api.MqttPriority;
import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.api.MqttType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public void convertTime(SimpleDateFormat formatter) throws ParseException {

    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public boolean timeAvailable() {
        return false;
    }

    @Override
    public void setTime(Date date) {

    }

}
