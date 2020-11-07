package io.openems.edge.pwm.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Consolinno Pwm Device",
        description = "Module for Pulse widening modulation"
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Pwm Device Id", description = "Unique Id of the Pwm Device.")
    String id() default "PwmDevice0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Pwm Module Name", description = "Name of the allocated Pwm Module")
    String pwm_module() default "Pwm0";

    @AttributeDefinition(name = "Pin Position", description = "What Channel input you are using with this PWM Device (0-7)")
    short pinPosition() default 0;

    @AttributeDefinition(name = "Is Inverse", description = "If the Device is powered at a low Flank set true")
    boolean isInverse() default false;

    @AttributeDefinition(name = "Pwm LED Signal available", description = "Is a LED On the PWM Module available")
    boolean hasLed() default false;

    @AttributeDefinition(name = "Which PwmPosition has a LED", description = "Only if LED available")
    int pinPositionLed() default 4;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Pwm Device [{id}]";

}
