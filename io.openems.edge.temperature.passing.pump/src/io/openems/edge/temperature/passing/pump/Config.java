package io.openems.edge.temperature.passing.pump;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;


@ObjectClassDefinition(
        name = "Consolinno Pump",
        description = "A Valve mainly used for the Passing Station and Controller"
)
@interface Config {


    String service_pid();

    @AttributeDefinition(name = "Pump Name", description = "Unique Id of the Pump.")
    String id() default "Valve0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Pump Type", description = "What Kind of Pump is it?",
    options = {
            @Option(label = "Relais", value = "Relais"),
            @Option(label = "Pwm", value = "Pwm"),
            @Option(label = "Both", value = "Both")
    })
    String pump_Type() default "Both";

    @AttributeDefinition(name =  "Relais Id", description = "If the Pump is connected to a relais; type the id.")
    String pump_Relais() default "Relais1";

    @AttributeDefinition(name = "PWM Id", description = "If the Pump is connected as a pwm Device; type in the id.")
    String pump_Pwm() default "PwmDevice0";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Pump [{id}]";
}
