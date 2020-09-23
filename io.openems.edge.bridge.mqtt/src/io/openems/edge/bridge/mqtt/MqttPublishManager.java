package io.openems.edge.bridge.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.openems.edge.bridge.mqtt.api.MqttTask;

public class MqttPublishManager extends AbstractMqttManager {

    private Map<Integer, MqttConnectionPublish> connections = new HashMap<>();


    MqttPublishManager(Map<String, List<MqttTask>> publishTasks, String mqtt_broker, String mqtt_broker_url,
                       String mqtt_username, String mqtt_password, String mqtt_client_id, int keepAlive,
                       boolean timeEnabled, String timeFormat, String locale) throws MqttException {

        super(mqtt_broker, mqtt_broker_url, mqtt_username, mqtt_password, mqtt_client_id, keepAlive, publishTasks,
                timeEnabled, timeFormat, locale, true);
        //Create new Connection Publish
        for (int x = 0; x < 3; x++) {
            this.connections.put(x, new MqttConnectionPublish(super.timeEnabled, super.timeFormat, super.locale));
            this.connections.get(x).createMqttPublishSession(super.mqttBroker, super.mqttClientId + "_PUBLISH_" + x,
                    super.keepAlive, super.mqttUsername, super.mqttPassword, x == 0);
            this.connections.get(x).connect();
        }
    }

    @Override
    protected void forever() {
        super.foreverAbstract();
        super.currentToDo.forEach(task -> {
            try {
                int qos = task.getQos();
                super.stopwatch.start();
                this.connections.get(qos).sendMessage(task.getTopic(), task.getPayload(), qos, task.getRetainFlag(), task.getAddTime());
                long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                //update timeForQosTask randomly; Used for Later Average Time Calculation of each Task.

                int counterOfQoS = super.counterForQos.get(qos);
                super.timeForQos.get(qos).add(counterOfQoS, time);
                super.counterForQos.replace(qos, (counterOfQoS + 1) % super.maxListLength);

                super.stopwatch.stop();
                super.stopwatch.reset();
                //remove Task after it's done
                super.currentToDo.remove(task);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
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
