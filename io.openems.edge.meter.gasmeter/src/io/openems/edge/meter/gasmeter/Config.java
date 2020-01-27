package io.openems.edge.meter.gasmeter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "GasMeterMbus",
        description = "A GasMeter Communicating via MBus."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "GasMeter-Device ID", description = "Unique Id of the GasBoiler.")
    String id() default "GasMeter0";

    @AttributeDefinition(name = "alias", description = "Human readable name of the gasmeter.")
    String alias() default "";

    @AttributeDefinition(name = "MeterType", description = "Identification via Type",
            options = {
                    @Option(label = "Placeholder", value = "Placeholder"),
                    @Option(label = "Placeholder", value = "Placeholder"),
                    @Option(label = "Placeholder", value = "Placeholder"),
            })
    String meterType() default "Placeholder";

    @AttributeDefinition(name = "MBus-Bridge Id", description = "The Unique Id of the mBus-Bridge you what to allocate to this device.")
    String mbusBridgeId() default "mbus0";

    @AttributeDefinition(name = "PrimaryAddress", description = "primary Address of the Mbus Component.")
    int primaryAddress();

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Gas-meter Device Id [{id}]";

}