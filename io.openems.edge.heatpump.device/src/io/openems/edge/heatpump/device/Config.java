package io.openems.edge.heatpump.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
        name = "HeatPump", //
        description = "Is the HeatPump device communicating via Genibus.")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "HeatPump0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "PumpAddress", description = "Address of the Pump.")
    int heatPumpAddress() default 0x20;

    @AttributeDefinition(name = "PumpType", description = "Denotation of the Pump.")
    String pumpType() default "Magna3";

    @AttributeDefinition(name = "Initial Pressure", description = "Initial Pressure value of the pump.")
    double pumpStartPressure() default 5;

    @AttributeDefinition(name = "min Pressure", description = "Initial min Pressure allowed for the pump.")
    double minPressure() default 1;

    @AttributeDefinition(name = "max Pressure", description = "Initial max Pressure allowed for the pump.")
    double maxPressure() default 6.5;

    String webconsole_configurationFactory_nameHint()default"HeatPump [{id}]";
}