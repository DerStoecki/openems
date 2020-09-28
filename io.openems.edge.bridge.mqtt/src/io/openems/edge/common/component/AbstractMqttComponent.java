package io.openems.edge.common.component;

import java.util.List;

import io.openems.edge.common.channel.Channel;


public abstract class AbstractMqttComponent {

    private List<String> configList;
    private List<Channel<?>> channels;
    private List<String> componentIds;
    private List<String> payloads;

    protected AbstractMqttComponent(List<String> configList, List<String> payloads, List<String> componentIds, List<Channel<?>> firstInitialChannelIds) {
        this.configList = configList;
        this.componentIds = componentIds;
        this.channels = firstInitialChannelIds;
        this.payloads = payloads;

        createMqttTasks();
    }


    /**
     * Creates for each config entry a pub or sub Task
     * Add to List of MqttBridge
     * Component can get List of Tasks via Bridge and their Id
     */
    private void createMqttTasks() {
        this.configList.forEach(entry -> {
            
        });
    }

    //Each entry of List contains: Pub/Sub then: QoS: LastWill; TimeStampEnabled; PayloadNo Entry;


}




