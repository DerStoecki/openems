package io.openems.edge.bridge.mqtt.api;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.ConfigurationException;

public interface MqttComponent extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        TELEMETRY(Doc.of(OpenemsType.STRING)),

        COMMANDS(Doc.of(OpenemsType.STRING)),
        COMMANDS_VALUE(Doc.of(OpenemsType.STRING)),

        EVENTS(Doc.of(OpenemsType.STRING)),
        EVENTS_VALUE(Doc.of(OpenemsType.STRING)),

        CONFIGURATION(Doc.of(OpenemsType.STRING));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    /**
     * Gets the TelemetrySubscription.
     *
     * @return the Channel
     */
    default Channel<String> getTelemetry() {
        return this.channel(ChannelId.TELEMETRY);
    }

    /**
     * Gets the Commands entry of the Channel (Usually broker publishes here).
     *
     * @return the Channel.
     */
    default Channel<String> getSubscribe() {
        return this.channel(ChannelId.COMMANDS);
    }

    /**
     * Gets the Event from the Channel (Usually broker publishes here).
     *
     * @return the Channel.
     */

    default Channel<String> getEvents() {
        return this.channel(ChannelId.EVENTS);
    }

    default Channel<String> getCommands() {
        return this.channel(ChannelId.COMMANDS);
    }

    default Channel<String> getConfiguration() {
        return this.channel(ChannelId.CONFIGURATION);
    }

    void reactToEvent();

    void reactToCommand();

    void updateJSONConfig() throws MqttException, ConfigurationException;

}
