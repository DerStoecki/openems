package io.openems.edge.bridge.mqtt;


import io.openems.edge.bridge.mqtt.api.MqttPublishTask;

import java.util.List;
import java.util.Map;

public class MqttPublishManager extends AbstractMqttManager {

    Map<String, List<MqttPublishTask>> tasks;


    public MqttPublishManager(Map<String, List<MqttPublishTask>> tasks, String username, String password, String connection, String s) {
        this.tasks = tasks;
    }

    @Override
    protected void forever() throws InterruptedException {

    }
}
