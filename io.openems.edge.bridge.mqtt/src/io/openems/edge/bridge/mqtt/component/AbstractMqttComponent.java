package io.openems.edge.bridge.mqtt.component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.channel.Channel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

//EventManager to alter Payload
public abstract class AbstractMqttComponent {

    private MqttBridge mqttBridge;

    private List<String> subConfigList;
    private List<String> pubConfigList;
    private List<String> payloads;
    //STRING = TOPIC as ID ---- TASK
    private Map<String, MqttPublishTask> publishTasks = new HashMap<>();
    private Map<String, MqttSubscribeTask> subscribeTasks = new HashMap<>();
    //ChannelId ----- Channel Itself
    private Map<String, Channel<?>> mapOfChannel = new ConcurrentHashMap<>();
    private String id;
    private boolean createdByOsgi;
    private boolean hasBeenConfigured;
    private String jsonConfig = "";

    /**
     * Initially update Config and after that set params for initTasks.
     *
     * @param id            id of this Component, usually from configuredDevice and it's config.
     * @param subConfigList Subscribe ConfigList, containing the Configuration for the subscribeTasks.
     * @param pubConfigList Publish Configlist, containing the Configuration for the publishTasks.
     * @param payloads      containing all the Payloads. ConfigList got the Payload list as well.
     * @param createdByOsgi is this Component configured by OSGi or not. If not --> Read JSON File/Listen to Configuration Channel.
     * @param mqttBridge    mqttBridge of this Component.
     */
    //Path/Qos/SpecifiedType/Payloadno     payloads     ComponentChannel
    public AbstractMqttComponent(String id, List<String> subConfigList,
                                 List<String> pubConfigList, List<String> payloads,
                                 boolean createdByOsgi, MqttBridge mqttBridge) {

        this.id = id;
        this.subConfigList = subConfigList;
        this.pubConfigList = pubConfigList;
        this.payloads = payloads;
        this.createdByOsgi = createdByOsgi;
        this.mqttBridge = mqttBridge;

    }


    /**
     * CALL THIS AFTER UPDATE IS DONE in component.
     *
     * @param channelIds usually from Parent.
     * @throws MqttException          will be thrown if a Problem occurred with the broker.
     * @throws ConfigurationException will be thrown if the configuration was wrong.
     */
    public void initTasks(List<Channel<?>> channelIds) throws MqttException, ConfigurationException {
        if (createdByOsgi) {
            createMqttTasksFromOsgi(channelIds);
        }
    }


    /**
     * Creates for each config entry a pub or sub Task.
     * Add to List of MqttBridge
     * Component can get List of Tasks via Bridge and their Id
     *
     * @param channelIds usually from base Component; all channelIds.
     * @throws ConfigurationException if the Channels are Wrong
     * @throws MqttException          if a problem with Mqtt occurred
     */
    private void createMqttTasksFromOsgi(List<Channel<?>> channelIds) throws ConfigurationException, MqttException {

        createTasks(this.pubConfigList, false, channelIds);
        createTasks(this.subConfigList, true, channelIds);
        MqttException[] exMqtt = {null};
        this.subscribeTasks.forEach((key, value) -> {
            try {
                if (exMqtt[0] == null) {
                    mqttBridge.addMqttTask(this.id, value);
                    System.out.println("Added Task: " + value.getTopic());
                }
            } catch (MqttException e) {
                exMqtt[0] = e;
            }
        });
        this.publishTasks.forEach((key, value) -> {
            try {
                if (exMqtt[0] == null) {
                    mqttBridge.addMqttTask(this.id, value);
                    System.out.println("Added pub Task: " + value.getTopic());
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        if (exMqtt[0] != null) {
            mqttBridge.removeMqttTasks(this.id);
            throw exMqtt[0];
        }

    }

    /**
     * Create Tasks with Config given.
     *
     * @param configList usually from Parent config.
     * @param subTasks   is the current configList a sub/Pub task.
     * @param channelIds all the Channels that'll be configured
     * @throws ConfigurationException will be thrown if config is wrong/has an Error.
     */

    private void createTasks(List<String> configList, boolean subTasks, List<Channel<?>> channelIds) throws ConfigurationException {
        //
        ConfigurationException[] exConfig = {null};

        //For Each ConfigEntry (sub/pub) get the Channels and map them, create a task and add them at the end to the mqtt bridge.
        configList.forEach(entry -> {
            Map<String, Channel<?>> channelMapForTask;
            //futurePayload
            String payloadForTask;
            //split the entry; Each ConfigEntry looks like this:
            //MqttType!Priority!Topic!QoS!RetainFlag!TimeStampEnabled!PayloadNo!TimeToWait
            String[] tokens = entry.split("!");
            if (tokens.length != 9) {
                exConfig[0] = new ConfigurationException(entry, "Invalid Config");
            } else {
                //MqttType
                MqttType type = MqttType.valueOf(tokens[0].toUpperCase());

                //MqttPriority
                //Default is low for pub tasks --> no real priority
                MqttPriority priority = MqttPriority.LOW;
                if (subTasks) {
                    priority = MqttPriority.valueOf(tokens[1]);
                }
                //Topic
                String topic = tokens[2];
                //Qos
                int qos = Integer.parseInt(tokens[3]);
                //RetainFlag
                boolean retainFlag = Boolean.parseBoolean(tokens[4]);
                //UseTime
                boolean useTime = Boolean.parseBoolean(tokens[5]);
                //PayloadNo
                int payloadNo = Integer.parseInt(tokens[6]);
                //TimeToWait
                int timeToWait = Integer.parseInt(tokens[7]);
                //PayloadStyle
                PayloadStyle style = PayloadStyle.valueOf(tokens[8]);
                //if Error already occurred save time with this.
                if (exConfig[0] == null) {
                    try {
                        //create Map for the Tasks here, use payloadNo to identify the payload
                        channelMapForTask = configureChannelMapForTask(channelIds, payloadNo);
                        //Payload for Tasks
                        payloadForTask = this.payloads.get(payloadNo);
                        //subtasks will use payload to match their input to channels
                        if (subTasks) {
                            SubscribeTask task = new SubscribeTask(type, priority, topic, qos, retainFlag, useTime, timeToWait, channelMapForTask, payloadForTask, style, this.id);
                            this.subscribeTasks.put(topic, task);
                            //Create SubTasks
                        } else {
                            //Create PubTasks
                            //Publish tasks will use payload to map ID and the actual ChannelValue of the ChannelID
                            PublishTask task = new PublishTask(type, priority, topic, qos, retainFlag, useTime, timeToWait,
                                    channelMapForTask, payloadForTask, style, this.id);
                            this.publishTasks.put(topic, task);
                        }

                    } catch (ConfigurationException e) {
                        exConfig[0] = e;
                    }
                }
            }
        });


        if (exConfig[0] != null) {
            throw exConfig[0];
        }

    }

    /**
     * Configure a ChannelMap for the created MqttTask.
     *
     * @param givenChannels Channel List will be Reduced each time; For better Mapping usually from Device
     * @param payloadNo     number in the playload list usually from config.
     * @return return Map of ChannelId to Channel for the Task.
     * @throws ConfigurationException if the channel is not in the map or in the channelList
     */
    private Map<String, Channel<?>> configureChannelMapForTask(List<Channel<?>> givenChannels, int payloadNo) throws ConfigurationException {
        Map<String, Channel<?>> channelMapForTask = new HashMap<>();
        String currentPayload = this.payloads.get(payloadNo);
        //PAYLOADconfig is: ID:CHANNELID:ID:CHANNELID....
        //ID == Name available in Broker
        List<String> ids = new ArrayList<>();
        //ChannelID --> Used to identify value the pub tasks get / value to put for sub task
        List<String> channelIds = new ArrayList<>();
        String[] tokens = currentPayload.split(":");

        AtomicInteger counter = new AtomicInteger(0);
        Arrays.stream(tokens).forEachOrdered(consumer -> {
            if ((counter.get() % 2) == 0) {
                ids.add(consumer);
            } else {
                channelIds.add(consumer);
            }
            counter.getAndIncrement();
        });
        if (ids.size() != channelIds.size()) {
            throw new ConfigurationException("configurePayload " + payloadNo, "Payload incorrect");
        }
        counter.set(0);
        while (counter.get() < ids.size()) {
            //ChannelID == key for Map
            String channelIdKey = channelIds.get(counter.get());
            //Check if ComponentMap already got the ID as Key
            if (this.mapOfChannel.containsKey(channelIdKey)) {
                channelMapForTask.put(channelIdKey, this.mapOfChannel.get(channelIdKey));
                //Check The Channellist and check if any Matches the channelId at the current pos. if none matches --> throw Exception --> ChannelId is wrong.
            } else if (givenChannels.stream().anyMatch(entry -> entry.channelId().id().equals(channelIds.get(counter.get())))) {
                final Channel<?>[] channelToAdd = new Channel<?>[1];
                givenChannels.stream().filter(entry -> entry.channelId().id().equals(channelIdKey)).findFirst().ifPresent(channels -> channelToAdd[0] = channels);
                //put channel into this mapChannel and to channelMap for the task. Remove from ChannelList to reduce size.
                if (channelToAdd[0] != null) {
                    this.mapOfChannel.put(channelIdKey, channelToAdd[0]);
                    channelMapForTask.put(channelIdKey, this.mapOfChannel.get(channelIdKey));
                    givenChannels.remove(channelToAdd[0]);
                }
            } else {
                throw new ConfigurationException("configurePayload", "incorrect Channel!  " + channelIds.get(counter.get()));
            }
            counter.getAndIncrement();
        }
        return channelMapForTask;
    }


    /**
     * Returns the Payload from a certain topic of a subscriber.
     *
     * @param topic of the MqttSubscribeTask.
     * @return the corresponding subscribeTask.
     */

    protected String getPayloadFromSubscriber(String topic) {
        return this.subscribeTasks.get(topic).getPayload();
    }

    /**
     * Update method available for Components using MQTT.
     *
     * @param config        config of the Component, will be updated automatically.
     * @param configTarget  target, where to put ChannelIds. Usually something like "ChannelIds".
     * @param channelsGiven Channels of the Component, collected by this.channels, filtered by "_Property"
     * @param length        length of the configTarget entries. If Length doesn't match ChannelSize --> Update.
     */
    public void update(Configuration config, String configTarget, List<Channel<?>> channelsGiven, int length) {
        List<Channel<?>> channels =
                channelsGiven.stream().filter(entry ->
                        !entry.channelId().id().startsWith("_Property")
                ).collect(Collectors.toList());
        if (length != channels.size()) {
            this.updateConfig(config, configTarget, channels);
            hasBeenConfigured = false;

        } else {
            hasBeenConfigured = true;
        }
    }


    /**
     * Update Config and if successful you can initialize the MqttComponent.
     *
     * @param config       Configuration of the OpenemsComponent
     * @param configTarget usually from Parent-->Config.
     * @param channels     usually from Parent --> Channels.
     */

    private void updateConfig(Configuration config, String configTarget, List<Channel<?>> channels) {
        AtomicInteger counter = new AtomicInteger(0);
        String[] channelIdArray = new String[channels.size()];
        channels.forEach(channel -> {
            channelIdArray[counter.getAndIncrement()] = channel.channelId().id();
        });

        try {
            Dictionary<String, Object> properties = config.getProperties();
            properties.put(configTarget, propertyInput(Arrays.toString(channelIdArray)));
            config.update(properties);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] propertyInput(String types) {
        types = types.replaceAll("\\[", "");
        types = types.replaceAll("]", "");
        types = types.replace(" ", "");
        return types.split(",");
    }

    public Map<String, MqttPublishTask> getPublishTasks() {
        return publishTasks;
    }

    public Map<String, MqttSubscribeTask> getSubscribeTasks() {
        return subscribeTasks;
    }

    /**
     * Get The SubscribeTasks identified by their MqttType e.g. Telemetry, Command, Response
     *
     * @param type usually from calling Device. Give the MqttType.
     * @return the filtered Map.
     */
    public Map<String, MqttSubscribeTask> getMqttTypeSubscriberMap(MqttType type) {

        return this.subscribeTasks.entrySet().stream().filter(entry -> entry.getValue().getMqttType().equals(type))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private void createMqttTasksFromJson(List<Channel<?>> channelIds) {
    }


    public boolean hasBeenConfigured() {
        return this.hasBeenConfigured;
    }

    public void deactivate() {
        this.mqttBridge.removeMqttTasks(this.id);
    }

    public void setJsonConfig(String s) {
        this.jsonConfig = s;
    }

    public String getJsonConfig() {
        return this.jsonConfig;
    }

    public void initJson(ArrayList<Channel<?>> channels, String jsonConfig) {
        if (this.jsonConfig.equals(jsonConfig)) {
            return;
        } else {
            this.jsonConfig = jsonConfig;
            //TODO DO SOMETHING WITH CONFIG
        }
    }
}




