package io.openems.edge.bridge.mqtt.manager;

import com.google.common.base.Stopwatch;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.mqtt.api.MqttTask;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

abstract class AbstractMqttManager extends AbstractCycleWorker {
    //STRING = ID OF COMPONENT
    Map<String, List<MqttTask>> allTasks; //with ID
    List<MqttTask> toDoFuture = new ArrayList<>();
    List<MqttTask> currentToDo = new ArrayList<>();
    final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private int connectionLostCounter = 0;


    String mqttBroker;
    String mqttBrokerUrl;
    String mqttUsername;
    String mqttPassword;
    String mqttClientId;
    int keepAlive;
    boolean timeEnabled;
    String timeFormat;
    String locale;
    DateTimeZone timeZone;
    int maxListLength = 30;
    //Counter for Qos --> e.g. QoS 0 has counter 10 --> FOR LIST FILL
    Map<Integer, AtomicInteger> counterForQos = new HashMap<>();
    //Time for QoS in mS
    Map<Integer, List<Long>> timeForQos;
    //Calculate Random new Time for QoS;
    Random rd = new Random();

    long currentTime;

    //TODO GET EFFECTIVE CYCLE TIME
    //TODO ATM STANDARD TIME 1000 ms

    private long maxTime = 1000;

    private List<Long> averageTime = new ArrayList<>();

    AbstractMqttManager(String mqttBroker, String mqttBrokerUrl, String mqttUsername, String mqttPassword,
                        String mqttClientId, int keepAlive, Map<String, List<MqttTask>> allTasks,
                        boolean timeEnabled, DateTimeZone timeZone, boolean useAverageTime) {

        this.mqttBroker = mqttBroker;
        this.mqttBrokerUrl = mqttBrokerUrl;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mqttClientId = mqttClientId;
        this.keepAlive = keepAlive;
        this.allTasks = allTasks;
        this.timeEnabled = timeEnabled;
        this.timeForQos = new HashMap<>();
        for (int x = 0; x < 3; x++) {
            this.timeForQos.put(x, new ArrayList<>());
            this.counterForQos.put(x, new AtomicInteger(0));
            this.timeForQos.put(x, new ArrayList<>());
            this.timeForQos.get(x).add(0, (long) (x + 1) * 10);
        }
        this.timeZone = timeZone;
    }

    void foreverAbstract() {
        calculateAverageTimes();
        calculateCurrentTime();
        addToFutureAndCurrentToDo(sortTasks());
    }

    //TODO IMPROVE
    private void addToFutureAndCurrentToDo(List<MqttTask> sortedTasks) {
        //Add at the End of Future
        if (sortedTasks != null) {
            this.toDoFuture.addAll(sortedTasks);
        }

        boolean timeAvailable = true;
        long timeLeft = maxTime;
        while (timeAvailable && (toDoFuture.size() > 0)) {
            MqttTask task = toDoFuture.get(0);
            timeLeft = timeLeft - averageTime.get(task.getQos());
            if (timeLeft < 0) {
                timeAvailable = false;
                break;
            }
            currentToDo.add(task);
            toDoFuture.remove(0);
        }


    }

    //TODO SEE IF SORTED BY PRIORITY
    private List<MqttTask> sortTasks() {
        List<MqttTask> collectionOfAllTasks = new ArrayList<>();
        this.allTasks.forEach((key, value) -> collectionOfAllTasks.addAll(value));
        //Add QoS 0 to CurrentToDo --> No Time Required
        collectionOfAllTasks.stream().filter(mqttTask -> mqttTask.getQos() == 0 && mqttTask.isReady(this.currentTime)).forEach(task -> {
            this.currentToDo.add(task);
        });
        this.currentToDo.forEach(collectionOfAllTasks::remove);
        if (collectionOfAllTasks.size() > 0) {
            return collectionOfAllTasks.stream().filter(mqttTask -> mqttTask.isReady(this.currentTime)).sorted(Comparator.comparing(MqttTask::getPriority)).collect(Collectors.toList());
        } else {
            return null;
        }
    }


    //EACH QoS has AverageTime except QoS 0

    private void calculateAverageTimes() {
        //for each Time of each QoS --> add and create Average
        AtomicLong time = new AtomicLong(0);
        this.timeForQos.forEach((key, value) -> {
            if (key == 0) {
                this.averageTime.add(key, (long) 0);
            } else {
                value.forEach(time::getAndAdd);
                long addedTime = time.get();
                addedTime /= value.size(); //either maxlength or <
                this.averageTime.add(key, addedTime);
                time.set(0);
            }
        });
    }

    protected int getConnectionLostCounter() {
        return this.connectionLostCounter;
    }

    public void tryReconnect(MqttClient connection) throws MqttException {
        if (connection.isConnected()) {
            return;
        } else {
            try {
                connection.reconnect();
            } catch (MqttException e) {
                if (connectionLostCounter < 10) {
                    connectionLostCounter++;
                } else {
                    throw e;
                }
            }
        }
    }

    protected void calculateCurrentTime() {
        this.currentTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public long getCurrentTime() {
        return this.currentTime;
    }
}
