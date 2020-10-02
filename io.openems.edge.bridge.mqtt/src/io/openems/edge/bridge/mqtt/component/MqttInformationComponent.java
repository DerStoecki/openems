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

        update();


    }

    private void update() {
        Configuration c;
        boolean changesWereMade = false;

        try {
            c = ca.getConfiguration(this.servicePid(), "?");
            Dictionary<String, Object> properties = c.getProperties();
            Object target = properties.get("mqttTypes");
            Object target2 = properties.get("mqttPriorities");
            Object target3 = properties.get("mqttCommandTypes");
            Object target4 = properties.get("mqttEventTypes");
            Object target5 = properties.get("payloadStyle");
            Object target6 = properties.get("mqttPriority");
            final String existingTarget = target.toString();
            final String existingTarget2 = target2.toString();
            final String existingTarget3 = target3.toString();
            final String existingTarget4 = target4.toString();
            final String existingTarget5 = target5.toString();
            final String existingTarget6 = target6.toString();

            if (existingTarget.isEmpty()) {
                properties.put("mqttTypes", Arrays.toString(MqttType.values()));
                changesWereMade = true;
            }
            if (existingTarget2.isEmpty()) {
                properties.put("mqttPriorities", Arrays.toString(MqttPriority.values()));
                changesWereMade = true;
            }
            if (existingTarget3.isEmpty()) {
                properties.put("mqttCommandTypes", Arrays.toString(MqttCommandType.values()));
                changesWereMade = true;
            }
            if (existingTarget4.isEmpty()) {
                properties.put("mqttEventTypes", Arrays.toString(MqttEventType.values()));
                changesWereMade = true;
            }

            if (existingTarget5.isEmpty()) {
                properties.put("payloadStyle", Arrays.toString(PayloadStyle.values()));
                changesWereMade = true;
            }
            if (existingTarget6.isEmpty()) {
                properties.put("mqttPriority", Arrays.toString(MqttPriority.values()));
            }

            if (changesWereMade) {
                c.update(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }
}
