package io.openems.edge.bridge.mqtt;

import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;
import io.openems.edge.bridge.mqtt.api.MqttTask;

import java.util.List;
import java.util.Map;

public class MqttSubscribeManager extends AbstractMqttManager {

    private Map<String, List<MqttTask>> subscribeTasks;


    MqttSubscribeManager(Map<String, List<MqttTask>> subscribeTasks, String mqtt_broker, String mqtt_broker_url,
                         String mqtt_username, String mqtt_password, String mqtt_client_id, int keepAlive,
                         boolean timeEnabled, String timeFormat, String locale) {

        super(mqtt_broker, mqtt_broker_url, mqtt_username, mqtt_password, mqtt_client_id, keepAlive, subscribeTasks,
                timeEnabled, timeFormat, locale);
        this.subscribeTasks = subscribeTasks;
    }

    @Override
    protected void forever() throws InterruptedException {

    }

    public void deactivate() {

    }
}
