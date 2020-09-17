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

    //Username Pw; Type: tcp / SSL ; Keep Alive ; ClientName ; MQTT version
    @AttributeDefinition(name = "Username", description = "Username for the Broker")
    String username() default "user";

    @AttributeDefinition(name = "Password", description = "Password")
    String password() default "user";

    @AttributeDefinition(name = "Connection Type", description = "Tcp or TLS",
            options = {
                    @Option(value = "Tcp"),
                    @Option(value = "TLS")
            })
    String connection() default "Tcp";

    @AttributeDefinition(name = "ClientName", description = "ClientId used for brokerConnection")
    String clientId() default "OpenEMS-1";


    @AttributeDefinition(name = "LastWillSet", description = "Do you want a Last Will / Testament to be enabled")
    boolean lastWillSet() default true;


    @AttributeDefinition(name = "Topic", description = "Topic for Last Will")
    String 
    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Mqtt Bridge [{id}]";
}
