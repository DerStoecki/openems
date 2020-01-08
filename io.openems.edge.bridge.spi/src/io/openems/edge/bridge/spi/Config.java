package io.openems.edge.bridge.spi;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Bridge Spi",
        description = " Spi Bridge, needed for the Communciation with Spi Modules and Devices.")

@interface Config {
    String service_pid();

    @AttributeDefinition(name = "SpiBridge - ID", description = "Id of Spi Bridge.")
    String id() default "Spi";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Spi Bridge [{id}]";
}
