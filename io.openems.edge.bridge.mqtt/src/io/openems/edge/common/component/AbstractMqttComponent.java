package io.openems.edge.common.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Iterables;
import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.channel.Channel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Reference;

//EventManager to alter Payload
public abstract class AbstractMqttComponent {
    @Reference
    MqttBridge mqttBridge;

    private List<String> configList;
    private List<Channel<?>> channels;
    private List<String> payloads;
    private PayloadStyle payloadStyle;
    //STRING = TOPIC as ID
    private Map<String, MqttPublishTask> publishTasks;
    private Map<String, MqttSubscribeTask> subscribeTasks = new ConcurrentHashMap<>();

    //Path/Qos/SpecifiedType/Payloadno     payloads     ComponentChannel
    protected AbstractMqttComponent(List<String> configList, List<String> payloads, List<Channel<?>> channelIds,
                                    boolean createdByOsgi, PayloadStyle style) {
        this.configList = configList;
        this.channels = channelIds;
        this.payloads = payloads;
        this.payloadStyle = style;
        if (createdByOsgi) {
            createMqttTasksFromOsgi();
        } else {
            createMqttTasksFromJson();
        }
    }


    /**
     * Creates for each config entry a pub or sub Task.
     * Add to List of MqttBridge
     * Component can get List of Tasks via Bridge and their Id
     */
    private void createMqttTasksFromOsgi() {
        List<MqttTask> mqttTasksToAdd = new ArrayList<>();
        Exception[] ex = {null};
        AtomicInteger mapCounter = new AtomicInteger(0);
        this.configList.forEach(entry -> {

            //split the entry
            String[] tokens = entry.split("!");
            if (tokens.length != 6) {
                ex[0] = new ConfigurationException(entry, "Invalid Config");
            } else {
                //MqttType
                MqttType type = MqttType.valueOf(tokens[0].toUpperCase());
                //Topic
                String topic = tokens[1];
                //Qos
                int qos = Integer.parseInt(tokens[2]);
                //RetainFlag
                boolean retainFlag = Boolean.parseBoolean(tokens[3]);
                //UseTime
                boolean useTime = Boolean.parseBoolean(tokens[4]);
                //PayloadNo
                String payload = configure()
                //getAndAlterPayloadAndCreateMap
            }
            // compare and try to add AT THE END IF NO ERRORS OCCURRED
            //ConfigPub has: MqttType!Topic!Qos!RetainFlag!useTime!PayloadNo
            //Sub has: MqttType!Topic!Qos!RetainFlag!useTime!MqttPriority


            // Publish task has: Topic; PayLoad; MqttType; retainFlag; addTime;qoS, MqttPriority
            //
        });
    }

    //Each entry of List contains: Pub/Sub then: QoS: LastWill; TimeStampEnabled; PayloadNo Entry;


    protected String getPayloadFromSubscriber(String topic) {
        return this.subscribeTasks.get(topic).getPayload();
    }

    private void createMqttTasksFromJson() {
    }
}




