package io.openems.edge.bridge.mqtt.component;

import io.openems.edge.bridge.mqtt.api.*;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import org.osgi.service.metatype.annotations.Designate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;

@Designate(ocd = ConfigMqttInformationComponent.class, factory = true)
@Component(name = "Component.Mqtt.Information",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class MqttInformationComponent extends AbstractOpenemsComponent implements OpenemsComponent {

    @Reference
    ConfigurationAdmin ca;

    public MqttInformationComponent() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, ConfigMqttInformationComponent config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.mqttCommandTypes().length != MqttCommandType.values().length
                || config.mqttPriority().length != MqttPriority.values().length
                || config.mqttEventTypes().length != MqttEventType.values().length
                || config.mqttTypes().length != MqttType.values().length
                || config.payloadStyle().length != PayloadStyle.values().length
        ) {
            update();
        }


    }

    private void update() {
        Configuration c;


        try {
            c = ca.getConfiguration(this.servicePid(), "?");
            Dictionary<String, Object> properties = c.getProperties();

            properties.put("mqttTypes", propertyInput(Arrays.toString(MqttType.values())));
            properties.put("mqttPriorities", propertyInput(Arrays.toString(MqttPriority.values())));
            properties.put("mqttCommandTypes", propertyInput(Arrays.toString(MqttCommandType.values())));
            properties.put("mqttEventTypes", propertyInput(Arrays.toString(MqttEventType.values())));
            properties.put("payloadStyle", propertyInput(Arrays.toString(PayloadStyle.values())));
            properties.put("mqttPriority", propertyInput(Arrays.toString(MqttPriority.values())));

            c.update(properties);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] propertyInput(String types) {
        types = types.replaceAll("\\[", "");
        types = types.replaceAll("]", "");
        return types.split(",");
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }
}
