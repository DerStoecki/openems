package io.openems.edge.bridge.rest.communcation;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Bridge Rest",
        description = " Rest Bridge, needed for the communication with Master/Slave Devices e.g. Different Openems.")

@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Rest Bridge - ID", description = "Id of Rest Bridge.")
    String id() default "Rest";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Rest Bridge [{id}]";
}
