package io.openems.edge.bridge.mqtt;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionPublishImpl;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionSubscribeImpl;
import io.openems.edge.bridge.mqtt.dummys.PublishTaskDummy;
import io.openems.edge.bridge.mqtt.dummys.SubscribeTaskDummy;
import io.openems.edge.bridge.mqtt.manager.MqttPublishManager;
import io.openems.edge.bridge.mqtt.manager.MqttSubscribeManager;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.eclipse.paho.client.mqttv3.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Mqtt",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE
)
public class MqttBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, MqttBridge, EventHandler {

    @Reference
    ConfigurationAdmin ca;


    //Add to Manager
    private Map<String, List<MqttTask>> publishTasks = new ConcurrentHashMap<>();
    private Map<String, List<MqttTask>> subscribeTasks = new ConcurrentHashMap<>();

    private MqttPublishManager publishManager;
    private MqttSubscribeManager subscribeManager;
    private String mqttUsername;
    private String mqttPassword;
    private String mqttBroker;
    private String mqttBrokerUrl;
    private String mqttClientId;
    static int subscribeIdCounter = 0;
    private SimpleDateFormat formatter;

    //ONLY DEBUG SUBSCRIBE
    private String subscribeId;
    private String subscribeTopic;

    private MqttConnectionPublishImpl bridgePublisher;
    private MqttConnectionSubscribeImpl bridgeSubscriberTest;

    public MqttBridgeImpl() {
        super(OpenemsComponent.ChannelId.values(),
                MqttBridge.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsException, MqttException {

        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.mqttPriorities().length != MqttPriority.values().length || config.mqttTypes().length != MqttPriority.values().length) {
            updateConfig();
        }
        try {
            this.formatter = new SimpleDateFormat(config.timeFormat(), new Locale.Builder().setRegion(config.locale()).build());
            this.bridgePublisher = new MqttConnectionPublishImpl();
            this.bridgeSubscriberTest = new MqttConnectionSubscribeImpl();
            this.createMqttSession(config);
        } catch (MqttException e) {
            throw new OpenemsException(e.getMessage());
        }

        publishManager = new MqttPublishManager(publishTasks, this.mqttBroker, this.mqttBrokerUrl, this.mqttUsername,
                this.mqttPassword, config.keepAlive(), this.mqttClientId, config.timeStampEnabled(), formatter);
        //ClientId --> + CLIENT_SUB_0
        subscribeManager = new MqttSubscribeManager(subscribeTasks, this.mqttBroker, this.mqttBrokerUrl, this.mqttUsername,
                this.mqttPassword, this.mqttClientId, config.keepAlive(), config.timeStampEnabled(), formatter);

        publishManager.activate(super.id() + "_publish");
        subscribeManager.activate(super.id() + "_subscribe");

        //TODO DELETE LATER ONLY FOR TEST
        this.addMqttTask("Test", new PublishTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge",
                "{\"Arrived\": true}", MqttType.TELEMETRY, true, true, 0, MqttPriority.LOW));
        this.addMqttTask("Test", new PublishTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge/Qos/1",
                "{\"Arrived\": true}", MqttType.TELEMETRY, true, true, 1, MqttPriority.HIGH));
        this.addMqttTask("Test", new PublishTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge/Qos/2",
                "{\"Arrived\": true}", MqttType.TELEMETRY, true, true, 2, MqttPriority.URGENT));
        this.addMqttTask("Test2", new SubscribeTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge", MqttType.TELEMETRY,
                true, false, 0, MqttPriority.LOW));

        this.addMqttTask("Test", new SubscribeTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge/Qos/1",
                MqttType.TELEMETRY, false, false, 1, MqttPriority.LOW));
        this.addMqttTask("Test2", new SubscribeTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge/#",
                MqttType.TELEMETRY, false, false, 0, MqttPriority.LOW));
        this.addMqttTask("Test3", new SubscribeTaskDummy("Consolinno/Test/FirstPublishTopic/Bridge/Qos/2",
                MqttType.TELEMETRY, false, false, 2, MqttPriority.LOW));


    }

    private void updateConfig() {
        Configuration c;

        try {
            c = ca.getConfiguration(this.servicePid(), "?");
            Dictionary<String, Object> properties = c.getProperties();
            String types = Arrays.toString(MqttType.values());

            properties.put("mqttTypes", propertyInput(types));
            this.setMqttTypes().setNextValue(MqttType.values());
            types = Arrays.toString(MqttPriority.values());
            properties.put("mqttPriorities", propertyInput(types));
            c.update(properties);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] propertyInput(String types) {
        types = types.replaceAll("\\[", "");
        types = types.replaceAll("]", "");
        types = types.replaceAll(" ", "");
        return types.split(",");
    }


    private void createMqttSession(Config config) throws MqttException {
        //TCP OR SSL
        String broker = config.connection().equals("Tcp") ? "tcp" : "ssl";
        boolean isTcp = broker.equals("tcp");
        broker += "://" + config.ipBroker() + ":" + config.portBroker();
        this.mqttBroker = broker;
        this.mqttUsername = config.username();

        //TODO ENCRYPT PASSWORD IF TCP NOT SSL!
        this.mqttPassword = config.password();

        this.mqttClientId = config.clientId();
        this.mqttBrokerUrl = config.brokerUrl();

        this.bridgePublisher.createMqttPublishSession(this.mqttBroker, this.mqttClientId, config.keepAlive(),
                this.mqttUsername, this.mqttPassword, config.cleanSessionFlag());
        if (config.lastWillSet()) {
            this.bridgePublisher.addLastWill(config.topicLastWill(),
                    config.payloadLastWill(), config.qosLastWill(), config.timeStampEnabled(), config.retainedFlag(),
                    formatter.format(new Date(System.currentTimeMillis())));
        }
        //External Call bc Last will can be set
        this.bridgePublisher.connect();

        this.bridgePublisher.sendMessage(config.topicLastWill(), "\"Status\": Connected", 1,
                config.retainedFlag());

        //DEBUG AND TEST TODO DELETE LATER
        this.subscribeId = this.mqttClientId + "_CLIENT_TEST_SUBSCRIBE";
        this.bridgeSubscriberTest.createMqttSubscribeSession(this.mqttBroker, subscribeId,
                this.mqttUsername, this.mqttPassword, config.keepAlive());

        this.subscribeTopic = config.topicLastWill();

        this.bridgeSubscriberTest.subscribeToTopic(subscribeTopic, 0, subscribeId);

    }

    //TODO Password encryption etc --> Research etc; Public and private Keys etc etc on not Secure Connection.
    // TODO AT THE END!
    //TODO DO THIS IN AN EXTRA CLASS!
    /*private char[] createHashedPassword(String password) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return Arrays.toString(hashedPassword).toCharArray();
    }*/

    @Deactivate
    public void deactivate() {
        try {
            this.bridgePublisher.disconnect();
            this.bridgeSubscriberTest.disconnect();
            this.publishManager.deactivate();
            this.subscribeManager.deactivate();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add Mqtt Task.
     *
     * @param id       usually from MqttComponent / Same as component id
     * @param mqttTask usually created by MqttComponent
     * @return true if add was successful
     * @throws MqttException if somethings wrong
     */

    @Override
    public boolean addMqttTask(String id, MqttTask mqttTask) throws MqttException {

        if (mqttTask instanceof MqttPublishTask) {
            if (this.publishTasks.containsKey(id)) {
                this.publishTasks.get(id).add(mqttTask);
            } else {
                List<MqttTask> task = new ArrayList<>();
                task.add(mqttTask);
                this.publishTasks.put(id, task);
            }
        }

        if (mqttTask instanceof MqttSubscribeTask) {
            if (this.subscribeTasks.containsKey(id)) {
                this.subscribeTasks.get(id).add(mqttTask);
            } else {
                List<MqttTask> task = new ArrayList<>();
                task.add(mqttTask);

                this.subscribeTasks.put(id, task);
            }
            ((MqttSubscribeTask) mqttTask).putMessageId(subscribeIdCounter++);
            this.subscribeManager.subscribeToTopic(mqttTask, id);
        }

        return true;
    }

    @Override
    public boolean removeMqttTasks(String id) {
        //TODO SEE IF MANAGER TASKS GET REMOVED
        this.subscribeTasks.remove(id);
        this.publishTasks.remove(id);

        return true;
    }

    @Override
    public List<MqttTask> getSubscribeTasks(String id) {
        return this.subscribeTasks.get(id);
    }

    @Override
    public List<MqttTask> getPublishTasks(String id) {
        return this.publishTasks.get(id);
    }

    @Override
    public String getSubscribePayloadFromTopic(String topic, MqttType type) {
        return this.subscribeManager.getPayloadFromTopic(topic, type);
    }

    @Override
    public void handleEvent(Event event) {

        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {

            this.subscribeManager.triggerNextRun();
            this.publishManager.triggerNextRun();


            System.out.println("Getting Message");
            this.bridgeSubscriberTest.getTopic(this.subscribeId).forEach(entry -> {
                System.out.println(entry + this.bridgeSubscriberTest.getPayload(entry));
            });
            //System.out.println(this.bridgeSubscriberTest.getTopic(this.subscribeId) + this.bridgeSubscriberTest.getPayload(subscribeTopic));
            System.out.println("New Message Await");
        }
    }
}
