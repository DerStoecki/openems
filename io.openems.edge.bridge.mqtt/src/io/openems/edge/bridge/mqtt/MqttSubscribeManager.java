package io.openems.edge.bridge.mqtt;

import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.api.MqttTask;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttSubscribeManager extends AbstractMqttManager {

    private Map<Integer, MqttConnectionSubscribe> connections = new HashMap<>();

    MqttSubscribeManager(Map<String, List<MqttTask>> subscribeTasks, String mqtt_broker, String mqtt_broker_url,
                         String mqtt_username, String mqtt_password, String mqtt_client_id, int keepAlive,
                         boolean timeEnabled, String timeFormat, String locale) throws MqttException {

        super(mqtt_broker, mqtt_broker_url, mqtt_username, mqtt_password, mqtt_client_id, keepAlive, subscribeTasks,
                timeEnabled, timeFormat, locale, false);
        for (int x = 0; x < 3; x++) {
            this.connections.put(x, new MqttConnectionSubscribe(super.timeEnabled, super.timeFormat, super.locale));
            this.connections.get(x).createMqttSubscribeSession(super.mqttBroker, super.mqttClientId + "_SUBSCRIBE_" + x,
                    super.mqttUsername, super.mqttPassword, super.keepAlive);

        }

    }

    @Override
    protected void forever() throws InterruptedException {
        super.allTasks.forEach((key, value) -> {
            value.forEach(task -> {
                if (task instanceof MqttSubscribeTask) {
                    ((MqttSubscribeTask) task).response(this.connections.get(task.getQos()).getPayload(task.getTopic()));
                }
            });
        });

    }

    void subscribeToTopic(MqttTask task, String id) throws MqttException {
        this.connections.get(task.getQos()).subscribeToTopic(task.getTopic(), task.getQos(), id);

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
}
