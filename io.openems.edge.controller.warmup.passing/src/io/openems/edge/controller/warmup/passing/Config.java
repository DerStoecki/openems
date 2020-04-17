package io.openems.edge.controller.warmup.passing;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Consolinno WarmupPassing",
        description = "This Controller is used to warm up the passing station."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Passing Controller.")
    String id() default "ControllerWarmupPassing0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Controller.")
    String alias() default "WarmupPassing";

    @AttributeDefinition(name = "Starting temperature, default entry.", description = "The starting temperature of the heating run, in °C. Only considered when no config file is found and one needs to be created.")
    int startTemp() default 20;

    @AttributeDefinition(name = "Temperature increase per step, default entry.", description = "The increase in temperature per heating step, in °C. Only considered when no config file is found and one needs to be created.")
    int tempIncrease() default 5;

    @AttributeDefinition(name = "Number of steps, default entry.", description = "The number of temperature steps, minimum 1. Only considered when no config file is found and one needs to be created.")
    int stepNumber() default 5;

    @AttributeDefinition(name = "Steps length, default entry.", description = "The duration of each temperature step, in minutes. Only considered when no config file is found and one needs to be created.")
    int stepLength() default 1;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno WarmupPassing [{id}]";

}

