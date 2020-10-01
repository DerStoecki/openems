package io.openems.edge.common.component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.channel.Channel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Reference;

//EventManager to alter Payload
public abstract class AbstractMqttComponent {
    @Reference
    MqttBridge mqttBridge;

    @Reference
    ConfigurationAdmin ca;

    private List<String> subConfigList;
    private List<String> pubConfigList;
    private List<String> payloads;
    private PayloadStyle payloadStyle;
    //STRING = TOPIC as ID ---- TASK
    private Map<String, MqttPublishTask> publishTasks;
    private Map<String, MqttSubscribeTask> subscribeTasks = new ConcurrentHashMap<>();
    //ChannelId ----- Channel Itself
    private Map<String, Channel<?>> mapOfChannel = new ConcurrentHashMap<>();
    private String id;
    private boolean createdByOsgi;

    //Path/Qos/SpecifiedType/Payloadno     payloads     ComponentChannel
    protected AbstractMqttComponent(String id, String servicePid, String configTarget, List<String> subConfigList, List<String> pubConfigList, List<String> payloads, List<Channel<?>> channelIds,
                                    boolean createdByOsgi, PayloadStyle style) throws ConfigurationException, MqttException {

        updateConfig(servicePid, configTarget, channelIds);
        this.id = id;
        this.subConfigList = subConfigList;
        this.pubConfigList = pubConfigList;
        this.payloads = payloads;
        this.payloadStyle = style;
        this.createdByOsgi = createdByOsgi;
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
        } else {
            createMqttTasksFromJson();
        }
    }


    /**
     * Creates for each config entry a pub or sub Task.
     * Add to List of MqttBridge
     * Component can get List of Tasks via Bridge and their Id
     *
     * @param channelIds usually from base Component; all channelIds.
     * @throws ConfigurationException if the Channels are Wrong
     * @throws MqttException          if a problem with Mqtt Occured
     */
    private void createMqttTasksFromOsgi(List<Channel<?>> channelIds) throws ConfigurationException, MqttException {


        createTasks(this.subConfigList, true, channelIds);
        createTasks(this.pubConfigList, false, channelIds);

    }

    /**
     * Create Tasks with Config given.
     *
     * @param configList usually from Parent config.
     * @param subTasks   is the current configList a sub/Pub task.
     * @param channelIds all the Channels that'll be configured
     * @throws ConfigurationException will be thrown if config is wrong/has an Error.
     * @throws MqttException          will be thrown if there's a problem with the Broker.
     */

    private void createTasks(List<String> configList, boolean subTasks, List<Channel<?>> channelIds) throws ConfigurationException, MqttException {

        ConfigurationException[] exConfig = {null};
        MqttException[] exMqtt = {null};

        configList.forEach(entry -> {
            Map<String, Channel<?>> channelMapForTask = new ConcurrentHashMap<>();
            String payloadForTask = "";
            //split the entry
            String[] tokens = entry.split("!");
            if (tokens.length != 9) {
                exConfig[0] = new ConfigurationException(entry, "Invalid Config");
            } else {
                //MqttType
                MqttType type = MqttType.valueOf(tokens[0].toUpperCase());

                //MqttPriority
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

                try {
                    channelMapForTask = configurePayload(channelIds, payloadNo);
                    payloadForTask = this.payloads.get(payloadNo);
                    if (subTasks) {
                        mqttBridge.addMqttTask(id, new SubscribeTask(type, priority, topic, qos, retainFlag, useTime, timeToWait, channelMapForTask, payloadForTask, PayloadStyle.STANDARD));
                        //Create SubTasks
                    } else {
                        //Create PubTasks
                        mqttBridge.addMqttTask(id, new PublishTask(type, priority, topic, qos, retainFlag, useTime, timeToWait, channelMapForTask, payloadForTask, PayloadStyle.STANDARD));
                    }

                } catch (ConfigurationException e) {
                    exConfig[0] = e;
                } catch (MqttException e) {
                    exMqtt[0] = e;
                }
            }
            // compare and try to add AT THE END IF NO ERRORS OCCURRED
            //ConfigPub has: MqttType!Topic!Qos!RetainFlag!useTime!PayloadNo
            //Sub has: MqttType!Topic!Qos!RetainFlag!useTime!MqttPriority


            // Publish task has: Topic; PayLoad; MqttType; retainFlag; addTime;qoS, MqttPriority
            //
        });
        if (exConfig[0] != null) {
            throw exConfig[0];
        } else if (exMqtt[0] != null) {
            throw exMqtt[0];
        }
    }

    private Map<String, Channel<?>> configurePayload(List<Channel<?>> givenChannels, int payloadNo) throws ConfigurationException {
        Map<String, Channel<?>> channelMapForTask = new HashMap<>();
        String currentPayload = this.payloads.get(payloadNo);
        List<String> ids = new ArrayList<>();
        List<String> channelIds = new ArrayList<>();
        String[] tokens = currentPayload.split(":");
        AtomicInteger counter = new AtomicInteger(0);
        Arrays.stream(tokens).forEachOrdered(consumer -> {
            if ((counter.get() % 2) == 0) {
                ids.add(consumer);
            } else {
                channelIds.add(consumer);
            }
        });
        if (ids.size() != channelIds.size()) {
            throw new ConfigurationException("configurePayload " + payloadNo, "Payload incorrect");
        }
        //1. Go through ChannelMap and if not found through ChannelList
        //1. If from ChannelList --> Add to all channel Map
        // 2. Add Channel to channelMapForTask
        // 3. Remove Channel from AllChannelList bc it can be found in ChannelMap
        //repeat for each entry in Channel Ids

        while (counter.get() < ids.size()) {
            String channelIdKey = channelIds.get(counter.get());
            if (this.mapOfChannel.containsKey(channelIdKey)) {
                channelMapForTask.put(channelIdKey, this.mapOfChannel.get(channelIdKey));
            } else if (givenChannels.stream().anyMatch(entry -> entry.channelId().id().equals(channelIds.get(counter.get())))) {
                final Channel<?>[] channelToAdd = new Channel<?>[1];
                givenChannels.stream().filter(entry -> entry.channelId().id().equals(channelIdKey)).findFirst().ifPresent(channels -> channelToAdd[0] = channels);
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


    protected String getPayloadFromSubscriber(String topic) {
        return this.subscribeTasks.get(topic).getPayload();
    }

    private void createMqttTasksFromJson() {
    }

    /**
     * Update Config and if successful you can initialize the MqttComponent.
     *
     * @param servicePid   usually from Parent.
     * @param configTarget usually from Parent-->Config.
     * @param channels     usually from Parent --> Channels.
     */

    private void updateConfig(String servicePid, String configTarget, List<Channel<?>> channels) {
        Configuration c;
        AtomicInteger counter = new AtomicInteger(0);
        String[] channelIdArray = new String[channels.size()];
        channels.forEach(channel -> {
            channelIdArray[counter.getAndIncrement()] = channel.channelId().id();
        });

        try {
            c = ca.getConfiguration(servicePid, "?");
            Dictionary<String, Object> properties = c.getProperties();
            Object target = properties.get(configTarget);
            String existingTarget = target.toString();
            if (existingTarget.isEmpty()) {
                properties.put(configTarget, Arrays.toString(channelIdArray));
                c.update(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




