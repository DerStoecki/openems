package io.openems.edge.biomassheater;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "MassHeaterWoodChips Gilles",
        description = "A Massheater by Gilles, using Woodchips. Communicating via Modbus."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "MassHeater-Device ID", description = "Unique Id of the MassHeater.")
    String id() default "WoodChipHeater0";

    @AttributeDefinition(name = "alias", description = "Human readable name of MassHeater.")
    String alias() default "";

    @AttributeDefinition(name = "ModBus-Bridge Id", description = "The Unique Id of the modBus-Bridge you what to allocate to this device.")
    String modbusBridgeId() default "modbus0";

    @AttributeDefinition(name = "ModBus-Unit Id", description = "Integer Unit Id of the Component.")
    int modbusUnitId() default 0;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "MassHeater - Device [{id}]";

}