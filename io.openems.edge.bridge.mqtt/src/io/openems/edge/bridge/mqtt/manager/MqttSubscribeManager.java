package io.openems.edge.bridge.mqtt.manager;

import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.api.MqttTask;
import io.openems.edge.bridge.mqtt.api.MqttType;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionSubscribeImpl;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttSubscribeManager extends AbstractMqttManager {

    private Map<MqttType, MqttConnectionSubscribeImpl> connections = new HashMap<>();


    public MqttSubscribeManager(Map<String, List<MqttTask>> subscribeTasks, String mqttBroker, String mqttBrokerUrl,
                                String mqttUsername, String mqttPassword, String mqttClientId, int keepAlive,
                                boolean timeEnabled, SimpleDateFormat formatter) throws MqttException {

        super(mqttBroker, mqttBrokerUrl, mqttUsername, mqttPassword, mqttClientId, keepAlive, subscribeTasks,
                timeEnabled, formatter, false);
        MqttType[] types = MqttType.values();
        for (int x = 0; x < types.length; x++) {
            this.connections.put(types[x], new MqttConnectionSubscribeImpl());
            this.connections.get(types[x]).createMqttSubscribeSession(super.mqttBroker, super.mqttClientId + "_SUBSCRIBE_" + x,
                    super.mqttUsername, super.mqttPassword, super.keepAlive);

        }

    }

    @Override
    public void forever() throws InterruptedException, MqttException {

        checkLostConnections();
        super.calculateCurrentTime();
        super.currentToDo.forEach(task -> {
            if (task instanceof MqttSubscribeTask) {
                if (task.isReady(super.getCurrentTime())) {
                    ((MqttSubscribeTask) task).response(this.connections.get(task.getMqttType()).getPayload(task.getTopic()));
                }
            }
        });

    }

    private void checkLostConnections() throws MqttException {
        MqttException[] exceptions = {null};
        this.connections.forEach((key, value) -> {
            if (!value.getMqttClient().isConnected()) {
                try {
                    super.tryReconnect(value.getMqttClient());
                } catch (MqttException e) {
                    exceptions[0] = e;
                }
            }

        });
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
    }

    //CHANGE TO WILDCARD AND RESPONSE --> PUT IDS etc etc
    public void subscribeToTopic(MqttTask task, String id) throws MqttException {
        this.connections.get(task.getMqttType()).subscribeToTopic(task.getTopic(), task.getQos(), id);

    }

    public void deactivate() {
        super.deactivate();
        this.connections.forEach((key, value) -> {
            try {
                value.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    public String getPayloadFromTopic(String topic, MqttType type) {
        return this.connections.get(type).getPayload(topic);
    }
}
