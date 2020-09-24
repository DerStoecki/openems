package io.openems.edge.temperature.passing.valve;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;


@ObjectClassDefinition(
        name = "Passing Valve",
        description = "A Valve controlled by 2 relays used in the passing station."
)
@interface Config {


    String service_pid();

    @AttributeDefinition(name = "Valve Name", description = "Unique Id of the Valve.")
    String id() default "Valve0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Closing Relays", description = "What Relays is responsible for closing the Valve.")
    String closing_Relays() default "Relays0";

    @AttributeDefinition(name = "Opening Relays", description = "What Relays is responsible for closing the Valve.")
    String opening_Relays() default "Relays1";

    @AttributeDefinition(name = "Valve Time", description = "The time needed to Open and Close the valve (t in seconds).")
    int valve_Time() default 30;

    @AttributeDefinition(name = "Should Close on Activation", description = "Should the Valve Close completely if it's "
            + "activated: prevents in flight status due to crashes or restarts etc")
    boolean shouldCloseOnActivation() default true;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Valve Two Relays [{id}]";
}
