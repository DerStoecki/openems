package io.openems.edge.relays.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Relays I2c",
        description = "Relays with a Channel to Open and Close."
)

@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Relays Id", description = "Unique Id of the Relays.")
    String id() default "Relays0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Relays Type", description = "Is the Relays an opener or closer.",
            options = {
                    @Option(label = "Opener", value = "Opener"),
                    @Option(label = "Closer", value = "Closer")
            })
    String relaysType() default "Closer";

    @AttributeDefinition(name = "Relays-Module Id", description = "Id of the Relays-Module allocated to this Relays-Device.")
    String relaysBoard_id() default "relaysModule0";

    @AttributeDefinition(name = "Position", description = "The position of the Relays. Starting with 0.")
    int position() default 0;

    @AttributeDefinition(name = "Use MQTT", description = "Should this component use Mqtt, else ignore following configurations")
    boolean useMqtt() default true;
    @AttributeDefinition(name = "Mqtt Id", description = "Id of this component appearing in the Broker")
    String mqttId() default "Relay-0";

    @AttributeDefinition(name = "Created by OSGi", description = "Do you configure your MQTT Component by OSGi/Apache Felix or via JSON")
    boolean createdByOsgi() default true;
    @AttributeDefinition(name = "JSON File Path", description = " IF created by OSGi is false AND you want to read your config from Json File (Otherwise configure via REST later)")
    String pathForJson() default "";

    @AttributeDefinition(name = "ChannelIds", description = "This List will automatically filled with ChannelIds to configure for Pub and Sub")
    String[] channelIdList() default {};


    @AttributeDefinition(name = "Payloads. Starting with 0", description = "Type in the Payloads with ID:ChannelId:Id:ChannelId, where ID Means : Name represented in Broker and ChannelId = OpenemsChannel")
    String[] payloads() default {};

    @AttributeDefinition(name = "SubscriptionConfig", description = "This List is for configuring subscriptions, the accepted form is: "
            + "MqttType!Priority!Topic!QoS!RetainFlag!TimestampUseBoolean!PayloadNo!TimeToWait")
    String[] subscriptionList() default {
            "MqttType!Priority!Topic!QoS!RetainFlagBoolean!TimestampUseBoolean!PayloadNo!TimeToWait"};

    @AttributeDefinition(name = "PublishConfig", description = "This List is for configuring publishes, accepted form is: "
            + "MqttType!Priority!Topic!QoS!RetainFlag!TimestampUseBoolean!PayloadNo!TimeToWait")
    String[] publishList() default {
            "MqttType!Priority!Topic!Qos!RetainFlagBoolean!TimestampUseBoolean!PayloadNo!TimeToWait"
    };

    @AttributeDefinition(name = "PayloadStyle", description = "PayloadStyle you want to use (See Mqtt Information Component for more options)")
    String payloadStyle() default "STANDARD";

    boolean configurationDone() default false;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Relays [{id}]";


}