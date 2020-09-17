package io.openems.edge.bridge.mqtt;

import io.openems.edge.bridge.mqtt.api.MqttSubscribeTask;

import java.util.List;
import java.util.Map;

public class MqttSubscribeManager extends AbstractMqttManager {

    private Map<String, List<MqttSubscribeTask>> subscribeTasks;

    public MqttSubscribeManager(Map<String, List<MqttSubscribeTask>> subscribeTasks) {
        this.subscribeTasks = subscribeTasks;
    }

    @Override
    protected void forever() throws InterruptedException {

    }
}
