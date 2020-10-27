package io.openems.edge.bridge.mqtt.api;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.osgi.service.cm.ConfigurationException;

import java.util.stream.Stream;

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
     * Gets the Event from the Channel (Usually broker publishes here).
     *
     * @return the Channel.
     */

    default Channel<String> getEvents() {
        return this.channel(ChannelId.EVENTS);
    }

    /**
     * Gets the Commands entry of the Channel (Usually broker publishes here).
     *
     * @return the Channel.
     */
    default Channel<String> getCommands() {
        return this.channel(ChannelId.COMMANDS);
    }

    /**
     * get the Configuration Channel, if configured by REST/ or json file.
     *
     * @return the channel
     */
    default Channel<String> getConfiguration() {
        return this.channel(ChannelId.CONFIGURATION);
    }

    /**
     * The Value, corresponding to set command of the component.
     *
     * @return the Channel.
     */
    default Channel<String> getCommandsValue() {
        return this.channel(ChannelId.COMMANDS_VALUE);
    }

    /**
     * gets the value of the corresponding set event.
     *
     * @return the channel.
     */
    default Channel<String> getEventValue() {
        return this.channel(ChannelId.EVENTS_VALUE);
    }

    /**
     * Called By Mqtt Bridge. Component has to implement what to do with Events (Either a event happened internally and
     * tells the broker or vice versa).
     */
    void reactToEvent();

    /**
     * Called By Mqtt Bridge. Component has to implement what to do on commands set by mqtt bridge.
     */
    void reactToCommand() throws OpenemsError.OpenemsNamedException;

    /**
     * Updates the JSON Config. Called by MqttBridge.
     *
     * @throws MqttException          If a problem occured with the mqtt connection.
     * @throws ConfigurationException if the configuration is wrong.
     */
    void updateJsonConfig() throws MqttException, ConfigurationException;


    void reactToComponentCommand(MqttCommandType key, CommandWrapper value) throws OpenemsError.OpenemsNamedException;

}
