package io.openems.edge.bridge.mqtt.manager;

import io.openems.edge.bridge.mqtt.api.MqttPublishTask;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionPublishImpl;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.openems.edge.bridge.mqtt.api.MqttTask;

public class MqttPublishManager extends AbstractMqttManager {

    private Map<Integer, MqttConnectionPublishImpl> connections = new HashMap<>();

    public MqttPublishManager(Map<String, List<MqttTask>> publishTasks, String mqtt_broker, String mqtt_broker_url,
                       String mqtt_username, String mqtt_password, String mqtt_client_id, int keepAlive,
                       boolean timeEnabled, SimpleDateFormat formatter) throws MqttException {

        super(mqtt_broker, mqtt_broker_url, mqtt_username, mqtt_password, mqtt_client_id, keepAlive, publishTasks,
                timeEnabled, formatter, true);
        //Create new Connection Publish
        for (int x = 0; x < 3; x++) {
            this.connections.put(x, new MqttConnectionPublishImpl());
            this.connections.get(x).createMqttPublishSession(super.mqttBroker, super.mqttClientId + "_PUBLISH_" + x,
                    super.keepAlive, super.mqttUsername, super.mqttPassword, x == 0);
            this.connections.get(x).connect();
        }
    }

    @Override
    public void forever() throws InterruptedException, MqttException {
        super.foreverAbstract();
        this.checkLostConnections();
        super.currentToDo.forEach(task -> {
            try {
                if (task instanceof MqttPublishTask) {
                    MqttPublishTask task1 = ((MqttPublishTask) task);
                    String now = formatter.format(new Date(System.currentTimeMillis()));
                    task1.updatePayload(now);
                }

                int qos = task.getQos();
                long time = System.nanoTime();
                this.connections.get(qos).sendMessage(task.getTopic(), task.getPayload(), qos, task.getRetainFlag());
                time = System.nanoTime() - time;
                time = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
                AtomicInteger counter = super.counterForQos.get(qos);
                super.timeForQos.get(qos).add(counter.get(), time);
                counter.getAndIncrement();
                counter.set(counter.get() % 30);
            } catch (MqttException e) {
                e.printStackTrace();
                super.toDoFuture.add(task);
            }
        });
        super.currentToDo.clear();
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
}
