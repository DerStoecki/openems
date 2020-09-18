package io.openems.edge.bridge.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class AbstractMqttConnection {

    MqttClient mqttClient;
    //TODO RESEARCH MEMORYPERSISTENCE!
    //TODO INFLIGHT MESSAGES
    private MemoryPersistence persistence;
    private MqttConnectOptions mqttConnectOptions;

    protected boolean lastWillSet;
    boolean timeStampEnabled;
    private SimpleDateFormat formatter;

    AbstractMqttConnection(boolean timeStampEnabled, String timeDataFormat, String locale) {
        this.timeStampEnabled = timeStampEnabled;
        if (timeStampEnabled == true) {
            this.formatter = new SimpleDateFormat(timeDataFormat, new Locale.Builder().setRegion(locale).build());
        }
        this.persistence = new MemoryPersistence();
        this.mqttConnectOptions = new MqttConnectOptions();
    }

    void createMqttSession(String broker, String clientId, boolean retainedFlag, int keepAlive, String username,
                           String password, boolean lastWillSet, String topicLastWill, String payloadLastWill,
                           int qosLastWill, boolean shouldAddTime, boolean cleanSession) throws MqttException {

        this.mqttClient = new MqttClient(broker, clientId, this.persistence);

        mqttConnectOptions.setCleanSession(cleanSession);
        mqttConnectOptions.setKeepAliveInterval(keepAlive);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        if (lastWillSet) {
            if (shouldAddTime) {
                payloadLastWill = addTimeToPayload(payloadLastWill);
            }

            mqttConnectOptions.setWill(topicLastWill, payloadLastWill.getBytes(), qosLastWill, retainedFlag);
        }
        this.mqttConnectOptions.setCleanSession(cleanSession);
        System.out.println("Connecting to Broker");
        this.mqttClient.connect(mqttConnectOptions);
        System.out.println("Connected");

    }

    String addTimeToPayload(String payload) {
        Date date = new Date(System.currentTimeMillis());
        String now = formatter.format(date);
        payload = "{\n \"sentOn\": " + now + ",\n" + payload;
        return payload;
    }

    //TODO EXCEPTION HANDLING
    void disconnect() throws MqttException {

        this.mqttClient.disconnect();

    }

}
