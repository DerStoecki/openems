package io.openems.edge.temperature.passing.valve;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;


@ObjectClassDefinition(
        name = "Consolinno Valve",
        description = "A Valve mainly used for the Passing Station and Controller"
)
@interface Config {


    String service_pid();

    @AttributeDefinition(name = "Valve Name", description = "Unique Id of the Relais.")
    String id() default "Valve0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Closing Relais", description = "What Relais is responsible for closing the Valve")
    String closing_Relais() default "Relais0";

    @AttributeDefinition(name = "Opening Relais", description = "What Relais is responsible for closing the Valve")
    String opening_Relais() default "Relais1";

    @AttributeDefinition(name = "Valve Time", description = "The time needed to Open and Close the valve (t in seconds)")
    int valve_Time() default 30;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Valve [{id}]";
}
