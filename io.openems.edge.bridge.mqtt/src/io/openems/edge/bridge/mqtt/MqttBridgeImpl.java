package io.openems.edge.bridge.mqtt;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionPublishImpl;
import io.openems.edge.bridge.mqtt.connection.MqttConnectionSubscribeImpl;
import io.openems.edge.bridge.mqtt.manager.MqttPublishManager;
import io.openems.edge.bridge.mqtt.manager.MqttSubscribeManager;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.eclipse.paho.client.mqttv3.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Mqtt",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE
)
public class MqttBridgeImpl extends AbstractOpenemsComponent implements OpenemsComponent, MqttBridge, EventHandler {

    @Reference
    ConfigurationAdmin ca;

    private final Logger log = LoggerFactory.getLogger(MqttBridgeImpl.class);


    //Add to Manager
    private Map<String, List<MqttTask>> publishTasks = new ConcurrentHashMap<>();
    private Map<String, List<MqttTask>> subscribeTasks = new ConcurrentHashMap<>();
    //MqttComponentMap
    private Map<String, MqttComponent> components = new ConcurrentHashMap<>();

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
            this.createMqttSession(config);
        } catch (MqttException e) {
            throw new OpenemsException(e.getMessage());
        }
        //Important for last will.


        publishManager = new MqttPublishManager(publishTasks, this.mqttBroker, this.mqttBrokerUrl, this.mqttUsername,
                this.mqttPassword, config.keepAlive(), this.mqttClientId, config.timeStampEnabled(), formatter);
        //ClientId --> + CLIENT_SUB_0
        subscribeManager = new MqttSubscribeManager(subscribeTasks, this.mqttBroker, this.mqttBrokerUrl, this.mqttUsername,
                this.mqttPassword, this.mqttClientId, config.keepAlive(), config.timeStampEnabled(), formatter);

        publishManager.activate(super.id() + "_publish");
        subscribeManager.activate(super.id() + "_subscribe");
    }

    /**
     * Updates Config --> MqttTypes and Priorities.
     */
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

    /**
     * Due to the fact that the inputs are arrays this needs to be done.
     *
     * @param types either mqttTypes or Priorities
     * @return the String[] for update in OSGi
     */
    private String[] propertyInput(String types) {
        types = types.replaceAll("\\[", "");
        types = types.replaceAll("]", "");
        types = types.replaceAll(" ", "");
        return types.split(",");
    }

    /**
     * Creates the MQTT Session and connects to broker.
     * TODO ENCRYPTION TCP socket
     *
     * @param config config of this mqttBridge
     * @throws MqttException if somethings wrong like pw wrong or user etc.
     */
    private void createMqttSession(Config config) throws MqttException {
        //Create Broker URL/IP etc
        //TCP OR SSL
        String broker = config.connection().equals("Tcp") ? "tcp" : "ssl";
        boolean isTcp = broker.equals("tcp");
        broker += "://" + config.ipBroker() + ":" + config.portBroker();
        this.mqttBroker = broker;
        this.mqttUsername = config.username();

        //TODO ENCRYPT PASSWORD IF TCP NOT TLS!
        this.mqttPassword = config.password();
        //ClientID will be automatically altered by Managers depending on what they're doing
        this.mqttClientId = config.clientId();
        this.mqttBrokerUrl = config.brokerUrl();
        //BridgePublish set LastWill if configured
        this.bridgePublisher.createMqttPublishSession(this.mqttBroker, this.mqttClientId, config.keepAlive(),
                this.mqttUsername, this.mqttPassword, config.cleanSessionFlag());
        if (config.lastWillSet()) {
            this.bridgePublisher.addLastWill(config.topicLastWill(),
                    config.payloadLastWill(), config.qosLastWill(), config.timeStampEnabled(), config.retainedFlag(),
                    formatter.format(new Date(System.currentTimeMillis())));
        }
        //External Call bc Last will can be set
        this.bridgePublisher.connect();

    }

    @Deactivate
    public void deactivate() {
        try {
            //Disconnect every connection
            this.bridgePublisher.disconnect();
            this.bridgeSubscriberTest.disconnect();
            this.publishManager.deactivate();
            this.subscribeManager.deactivate();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add Mqtt Task. Usually called by AbstractMqttComponent.
     *
     * @param id       usually from MqttComponent / Same as component id
     * @param mqttTask usually created by MqttComponent
     * @throws MqttException if somethings wrong
     */

    @Override
    public void addMqttTask(String id, MqttTask mqttTask) throws MqttException {

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
    }

    /**
     * Removes the MqttTask by id. Usually Called by AbstractMqttComponent
     *
     * @param id usually from AbstractMqttComponent
     */
    @Override
    public void removeMqttTasks(String id) {
        this.subscribeTasks.remove(id);
        this.publishTasks.remove(id);
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
    public boolean addMqttComponent(String id, MqttComponent component) {
        if (this.components.containsKey(id)) {
            return false;
        } else {
            this.components.put(id, component);
            return true;
        }
    }

    @Override
    public void removeMqttComponent(String id) {
        this.components.remove(id);
        this.removeMqttTasks(id);
    }

    @Override
    public void handleEvent(Event event) {

        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            //handle all Tasks
            this.subscribeManager.triggerNextRun();
            this.publishManager.triggerNextRun();
            //Update the components Config if available
            this.components.forEach((key, value) -> {
                if (value.getConfiguration().value().isDefined() && !value.getConfiguration().value().get().equals("")) {
                    try {
                        value.updateJSONConfig();
                    } catch (MqttException | ConfigurationException e) {
                        log.warn("Couldn't refresh the config of component " + value.id() + " Please check your"
                                + " configuration or MqttConnection");
                    }
                }
                value.reactToEvent();
                value.reactToCommand();
            });
        }
    }
}
