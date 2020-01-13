package io.openems.edge.bridge.i2c;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Bridge I2C",
        description = "Bridge to communicate with the connected devices communicating via I2C."
)
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "I2CBridge-ID", description = "ID of I2c bridge.")
    String id() default "I2C";

    @AttributeDefinition(name = "Alias", description = "Human readable Name.")
    String alias() default "";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "I2C Bridge[{id}]";
}