package io.openems.edge.controller.heatpump.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
        name = "Controller Heat Pump", //
        description = "Controller to calculate and set h Const Ref Min Max and rRem")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "ControllerHeatPump0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "HeatPumpId", description = "Unique Id of the Pump.")
    String heatPumpId() default "HeatPump0";

    @AttributeDefinition(name = "MinRange", description = "MinReference Value of Min Pressure (0-253).")
    double hRefMin() default 5;

    @AttributeDefinition(name = "MaxRange", description = "MaxReference Value of Max Pressure (1-254).")
    double hRefMax() default 127;

    @AttributeDefinition(name = "Wanted Pressure in %", description = "Pressure Level in % (depending on Min and Max Range")
            double rRem() default 50.5;

    String webconsole_configurationFactory_nameHint() default "Controller Heat Pump [{id}]";
}