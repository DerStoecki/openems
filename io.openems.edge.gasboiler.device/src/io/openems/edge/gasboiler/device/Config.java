package io.openems.edge.gasboiler.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "GasBoiler Viessmann",
        description = "A Gasboiler provided by Viessmann, communicating via ModbusTCP."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "GasBoiler-Device ID", description = "Unique Id of the GasBoiler.")
    String id() default "GasBoiler0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Chp.")
    String alias() default "";

    @AttributeDefinition(name = "ModBus-Bridge Id", description = "The Unique Id of the modBus-Bridge you what to allocate to this device.")
    String modbusBridgeId() default "modbus0";

    @AttributeDefinition(name = "ModBus-Unit Id", description = "Integer Unit Id of the Component.")
    int modbusUnitId() default 0;

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "GasBoiler Device [{id}]";

}