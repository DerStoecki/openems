package io.openems.edge.bridge.mqtt.connection;

import io.openems.edge.bridge.mqtt.api.MqttConnection;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public abstract class AbstractMqttConnection implements MqttConnection {

    MqttClient mqttClient;
    //TODO RESEARCH MEMORYPERSISTENCE!
    //TODO INFLIGHT MESSAGES
    private MemoryPersistence persistence;
    private MqttConnectOptions mqttConnectOptions;

    private boolean disconnected = false;

    AbstractMqttConnection() {
        //protected boolean lastWillSet;
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


   public void createMqttSubscribeSession(String mqttBroker, String mqttClientId, String username, String mqttPassword, int keepAlive) throws MqttException {
        createMqttSessionBasicSetup(mqttBroker, mqttClientId, username, mqttPassword, false, keepAlive);
        connect();
    }

    public void createMqttPublishSession(String broker, String clientId, int keepAlive, String username,
                                  String password, boolean cleanSession) throws MqttException {

        createMqttSessionBasicSetup(broker, clientId, username, password, cleanSession, keepAlive);

    }

    public void addLastWill(String topicLastWill, String payloadLastWill, int qosLastWill, boolean shouldAddTime, boolean retainedFlag, String time) {
        if (shouldAddTime) {
            payloadLastWill = addTimeToPayload(payloadLastWill, time);
        }
        mqttConnectOptions.setWill(topicLastWill, payloadLastWill.getBytes(), qosLastWill, retainedFlag);
    }


    String addTimeToPayload(String payload, String time) {
        payload = "\n\t \"sentOn\": " + time + ",\n" + payload;
        return payload;
    }

    public void connect() throws MqttException {
        System.out.println("Connecting to Broker");
        this.mqttClient.connect(this.mqttConnectOptions);
        this.disconnected = false;
        System.out.println("Connected");
    }

    //TODO EXCEPTION HANDLING

    public void disconnect() throws MqttException {

        this.mqttClient.disconnect();

    }

   public MqttClient getMqttClient() {
        return this.mqttClient;
    }

}
