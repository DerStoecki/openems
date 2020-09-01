package io.openems.edge.pump.grundfos;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
        name = "Pump Grundfos", //
        description = "This module maps GENIbus calls to OpenEMS channels for a Grundfos pump.")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "Pump0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "PumpAddress", description = "Address of the Pump.")
    int pumpAddress() default 231;

    @AttributeDefinition(name = "PumpType", description = "Denotation of the Pump.")
    String pumpType() default "Magna3";

    String webconsole_configurationFactory_nameHint()default"Pump Grundfos [{id}]";
}