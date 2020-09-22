package io.openems.edge.bridge.mqtt;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.mqtt.api.MqttTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

abstract class AbstractMqttManager extends AbstractCycleWorker {
    //STRING = ID OF COMPONENT
    Map<String, List<MqttTask>> allTasks; //with ID
    List<MqttTask> toDoFuture;
    List<MqttTask> currenttoDo;
    String mqttBroker;
    String mqttBrokerUrl;
    String mqttUsername;
    String mqttPassword;
    String mqttClientId;
    int keepAlive;
    boolean timeEnabled;
    String timeFormat;
    String locale;
    int maxListLength = 30;
    //Counter for Qos --> e.g. QoS 0 has counter 10 --> FOR LIST FILL
    Map<Integer, Integer> counterForQos = new HashMap<>();
    //Time for QoS in mS
    Map<Integer, List<Long>> timeForQos;
    //Calculate Random new Time for QoS;
    Random rd = new Random();

    private List<Long> averageTime = new ArrayList<>();

    AbstractMqttManager(String mqttBroker, String mqttBrokerUrl, String mqttUsername, String mqttPassword,
                        String mqttClientId, int keepAlive, Map<String, List<MqttTask>> allTasks,
                        boolean timeEnabled, String timeFormat, String locale) {

        this.mqttBroker = mqttBroker;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mqttClientId = mqttClientId;
        this.keepAlive = keepAlive;
        this.allTasks = allTasks;
        this.timeEnabled = timeEnabled;
        this.timeFormat = timeFormat;
        this.locale = locale;
        this.timeForQos = new HashMap<>();
        for (int x = 0; x < 3; x++) {
            this.timeForQos.put(x, new ArrayList<>());
            this.counterForQos.put(x, 0);
            this.timeForQos.put(x, new ArrayList<>());
            this.timeForQos.get(x).add(0, (long) (x + 1) * 10);
        }
    }


    void foreverAbstract() {
        calculateAverageTime();
        handleTasks();
    }

    private void handleTasks() {
        
    }


    //EACH QoS has AverageTime

    void calculateAverageTime() {
        //for each Time of each QoS --> add and create Average
        AtomicLong time = new AtomicLong(0);
        this.timeForQos.forEach((key, value) -> {
            value.forEach(time::getAndAdd);
            long addedTime = time.get();
            addedTime /= value.size(); //either maxlength or <
            this.averageTime.add(key, addedTime);
            time.set(0);
        });
    }
}
