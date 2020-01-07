package io.openems.edge.gaspedal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno Chp Module",
        description = "This Component communicates with the Chp Devices."
)

@interface Config {
    String service_pid();

    @AttributeDefinition(name = "ChpModule Id", description = "Name of the ChpModule.")
    String id() default "ChpModule0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Board.")
    String alias() default "";

    @AttributeDefinition(name = "VersionNumber", description = "What version of ChpBoard are  you using.",
    options = @Option(label = "Version 1.0", value = "1"))
    String version() default "1";

    @AttributeDefinition(name = "Address", description = "The allocated address of the Module.")
    String address() default "0x60";

    @AttributeDefinition(name = "Bus Device", description = "The Bus you want to use on your device; Raspberry pi 3 only supports 1.")
    int bus() default 1;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Consolinno BhkW Module [{id}]";

}


