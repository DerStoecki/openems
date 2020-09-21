package io.openems.edge.bridge.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public abstract class AbstractMqttConnection {

    MqttClient mqttClient;
    //TODO RESEARCH MEMORYPERSISTENCE!
    //TODO INFLIGHT MESSAGES
    private MemoryPersistence persistence;
    private MqttConnectOptions mqttConnectOptions;

    //protected boolean lastWillSet;
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

    private void createMqttSessionBasicSetup(String mqttBroker, String mqttClientId, String username, String mqttPassword,
                                              boolean cleanSession, int keepAlive) throws MqttException {
        this.mqttClient = new MqttClient(mqttBroker, mqttClientId, this.persistence);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(mqttPassword.toCharArray());
        mqttConnectOptions.setCleanSession(cleanSession);
        mqttConnectOptions.setKeepAliveInterval(keepAlive);
    }


    void createMqttSubscribeSession(String mqttBroker, String mqttClientId, String username, String mqttPassword, int keepAlive) throws MqttException {
        createMqttSessionBasicSetup(mqttBroker, mqttClientId, username, mqttPassword,  false, keepAlive);
        connect();
    }

    void createMqttPublishSession(String broker, String clientId, int keepAlive, String username,
                                  String password, boolean cleanSession) throws MqttException {

        createMqttSessionBasicSetup(broker, clientId, username, password, cleanSession, keepAlive);

    }

    void addLastWill(String topicLastWill, String payloadLastWill, int qosLastWill, boolean shouldAddTime, boolean retainedFlag) {
        if (shouldAddTime) {
            payloadLastWill = addTimeToPayload(payloadLastWill);
        }
        mqttConnectOptions.setWill(topicLastWill, payloadLastWill.getBytes(), qosLastWill, retainedFlag);
    }


    String addTimeToPayload(String payload) {
        Date date = new Date(System.currentTimeMillis());
        String now = formatter.format(date);
        payload = "{\n \"sentOn\": " + now + ",\n" + payload;
        return payload;
    }

    void connect() throws MqttException {
        System.out.println("Connecting to Broker");
        this.mqttClient.connect(this.mqttConnectOptions);
        System.out.println("Connected");
    }

    //TODO EXCEPTION HANDLING

    void disconnect() throws MqttException {

        this.mqttClient.disconnect();

    }

}
