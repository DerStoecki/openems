package io.openems.edge.heatpump.alphainnotec;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Heat Pump Alpha Innotec",
        description = "A heat pump provided by Alpha Innotec, communicating via ModbusTCP."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "HeatPump-Device ID", description = "Unique Id of the HeatPump.")
    String id() default "HeatPump0";

    @AttributeDefinition(name = "ModBus-Bridge Id", description = "The Unique Id of the modBus-Bridge you what to allocate to this device.")
    String modbusBridgeId() default "modbus0";

    @AttributeDefinition(name = "HeatPump Type", description = "Select used heat pump.",
    options = {
            @Option(label = "Default", value = "Default"),
            @Option(label = "Placeholder2", value = "Placeholder2"),
            @Option(label = "Not in List", value = "Not in List")
    })
    String gasBoilerType() default "Default";

    @AttributeDefinition(name = "alias", description = "Human readable name of HeatPump.")
    String alias() default "";

    @AttributeDefinition(name = "ModBus-Unit Id", description = "Integer Unit Id of the Component.")
    int modbusUnitId() default 1;

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Heat Pump Alpha Innotec Device [{id}]";

}