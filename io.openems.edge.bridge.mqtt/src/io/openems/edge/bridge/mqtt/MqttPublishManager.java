package io.openems.edge.bridge.mqtt;


import io.openems.edge.bridge.mqtt.api.MqttPublishTask;

import java.util.List;
import java.util.Map;

public class MqttPublishManager extends AbstractMqttManager {

    Map<String, List<MqttPublishTask>> tasks;


    public MqttPublishManager(Map<String, List<MqttPublishTask>> tasks) {
        this.tasks = tasks;
    }


    MqttPublishManager(Map<String, List<MqttPublishTask>> publishTasks, String mqtt_broker, String mqtt_broker_url,
                       String mqtt_username, String mqtt_password, String mqtt_client_id, int keepAlive) {


    }

    @Override
    protected void forever() throws InterruptedException {

    }
}
