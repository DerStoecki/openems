package io.openems.edge.heatpump.tecalor;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Heat Pump Tecalor",
        description = "A module to map Modbus calls to OpenEMS channels for a Tecalor heat pump."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "HeatPump-Device ID", description = "Unique Id of the HeatPump.")
    String id() default "HeatPump0";

    @AttributeDefinition(name = "ModBus-Bridge Id", description = "The Unique Id of the modBus-Bridge you what to allocate to this device.")
    String modbusBridgeId() default "modbus0";

    @AttributeDefinition(name = "alias", description = "Human readable name of HeatPump.")
    String alias() default "";

    @AttributeDefinition(name = "ModBus-Unit Id", description = "Integer Unit Id of the Component.")
    int modbusUnitId() default 1;

    @AttributeDefinition(name = "Debug", description = "Enable debug mode.")
    boolean debug() default false;

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Heat Pump Tecalor Device [{id}]";

}