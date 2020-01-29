package io.openems.edge.lucidcontrol.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "LucidControl Device",
        description = "LucidControl Device, connected with the LucidControl Module."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "LucidControlDevice-ID", description = "ID of LucidControlDevice.")
    String id() default "LucidControlDevice0";

    @AttributeDefinition(name = "Alias", description = "Human readable Name.")
    String alias() default "PressureMeter";

    @AttributeDefinition(name = "LucidControlModule-ID", description = "ID of LucidControlModule where the Device is connected with.")
    String moduleId() default "LucidControlModule0";

    @AttributeDefinition(name = "Position", description = "Position of Device (0-3)")
    int pinPos() default 0;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "LucidControlDevice[{id}]";
}