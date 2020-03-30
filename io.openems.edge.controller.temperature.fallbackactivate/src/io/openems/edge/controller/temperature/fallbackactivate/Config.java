package io.openems.edge.controller.temperature.fallbackactivate;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Consolinno FallbackActivate",
        description = "This Controller activates the fallback heater if needed."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Passing Controller.")
    String id() default "ControllerFallbackActivate0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Controller.")
    String alias() default "FallbackActivate";

    @AttributeDefinition(name = "Minimum temperature", description = "The minimum temperature.")
    int min_temp() default 40;

    @AttributeDefinition(name = "Temperature Sensor Id", description = "The TemperatureSensor Id used to measure the Temperature.")
    String temp_Sensor() default "TemperatureSensor0";

    @AttributeDefinition(name =  "Relay Id", description = "The Relay Id used to activate the fallback heater.")
    String relay_id() default "Relay0";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno FallbackActivate [{id}]";

}

