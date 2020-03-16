package io.openems.edge.bridge.gpio;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Bridge Gpio",
        description = "Bridge to communicate with the Gpio's of Raspberry Pi."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "GpioBridge-ID", description = "ID of Gpio bridge.")
    String id() default "GpioBridge";

    @AttributeDefinition(name = "Alias", description = "Human readable Name.")
    String alias() default "";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Gpio Bridge[{id}]";
}