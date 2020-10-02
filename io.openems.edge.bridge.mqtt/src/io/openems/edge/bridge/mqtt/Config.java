package io.openems.edge.bridge.mqtt;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;


@ObjectClassDefinition(
        name = "Bridge Mqtt",
        description = "Mqtt Bridge to communicate with a specific broker.")
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "MqttBridge - ID", description = "Id of Mqtt Bridge.")
    String id() default "Mqtt";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Broker IP", description = "IP of the broker")
    String ipBroker() default "localhost";

    @AttributeDefinition(name = "Broker URL", description = "URL of the Broker (if any given)")
    String brokerUrl() default "";

    @AttributeDefinition(name = "Port", description = "The Port the broker opened for communication")
    int portBroker() default 1883;

    //Username Pw; Type: tcp / SSL ; Keep Alive ; ClientName ; MQTT version
    @AttributeDefinition(name = "Username", description = "Username for the Broker")
    String username() default "user";

    @AttributeDefinition(name = "Password", description = "Password")
    String password() default "user";

    @AttributeDefinition(name = "Connection Type", description = "Tcp or TLS",
            options = {
                    @Option(label = "Tcp", value = "Tcp"),
                    @Option(label = "TLS", value = "TLS")
            })
    String connection() default "Tcp";

    @AttributeDefinition(name = "ClientName", description = "ClientId used for brokerConnection")
    String clientId() default "OpenEMS-1";

    @AttributeDefinition(name = "Keep Alive", description = "Keep Alive in Seconds")
    int keepAlive() default 60;


    @AttributeDefinition(name = "LastWillSet", description = "Do you want a Last Will / Testament to be enabled")
    boolean lastWillSet() default true;

    @AttributeDefinition(name = "Topic Last Will", description = "Topic for Last Will")
    String topicLastWill() default "OpenEMS/Leaflet_0/Status/";

    @AttributeDefinition(name = "LastWill Payload", description = "Payload for the last Will")
    String payloadLastWill() default "\"Status\": Connection Lost";

    @AttributeDefinition(name = "QoS of Last Will", description = "Quality of Service of last Will Msg")
    int qosLastWill() default 0;

    @AttributeDefinition(name = "Clean Session Flag", description = "If set to false --> Persistent Session")
    boolean cleanSessionFlag() default true;

    @AttributeDefinition(name = "Retaines Message for Last Will?", description = "Retained Flag of Mqtt Last Will Message.")
    boolean retainedFlag() default true;

    @AttributeDefinition(name = "Use Time for Last Will", description = "Send a timestamp?")
    boolean timeStampEnabled() default true;

    @AttributeDefinition(name = "Date Format", description = "What Format should your Date have?")
    String timeFormat() default "yyy-MM-dd 'at' HH:mm:ss";

    @AttributeDefinition(name = "Region", description = "Set Region of your Device e.g. DE")
    String locale() default "DE";

    @AttributeDefinition(name = "Mqtt Types", description = "Possible MqttTypes, will be filled after activation")
    String[] mqttTypes() default {""};

    @AttributeDefinition(name = "Mqtt Priorities", description = "MqttPriorities, will be filled automatically after activation")
    String[] mqttPriorities() default {""};

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Mqtt Bridge [{id}]";
}
