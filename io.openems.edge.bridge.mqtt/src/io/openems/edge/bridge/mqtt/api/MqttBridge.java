package io.openems.edge.bridge.mqtt.api;

import io.openems.common.channel.Debounce;
import io.openems.common.channel.Level;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;


public interface MqttBridge extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        //TODO Own Channel With Disconnects etc etc
        SLAVE_COMMUNICATION_FAILED(Doc.of(Level.FAULT) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        CYCLE_TIME_IS_TOO_SHORT(Doc.of(Level.WARNING) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        EXECUTION_DURATION(Doc.of(OpenemsType.LONG)),
        MQTT_TYPES(Doc.of(OpenemsType.STRING));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    //add and remove Task

    boolean addMqttTask(String id, MqttTask mqttTask) throws MqttException;

    boolean removeMqttTasks(String id);

    default Channel<String> setMqttTypes() {
        return this.channel(ChannelId.MQTT_TYPES);
    }

    List<MqttTask> getSubscribeTasks(String id);

    List<MqttTask> getPublishTasks(String id);

    String getSubscribePayloadFromTopic(String topic, MqttType type);
}


