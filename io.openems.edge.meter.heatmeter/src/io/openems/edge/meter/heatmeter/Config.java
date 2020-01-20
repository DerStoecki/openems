package io.openems.edge.meter.heatmeter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "HeatMeterMbus",
        description = "A HeatMeter Communicating via MBus."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "GasBoiler-Device ID", description = "Unique Id of the GasBoiler.")
    String id() default "HeatMeter0";

    @AttributeDefinition(name = "alias", description = "Human readable name of the heat-meter.")
    String alias() default "";

    @AttributeDefinition(name = "MeterType", description = "Identification via Type",
            options = {
                    @Option(label = "Itron-CF51", value = "Itron-CF51"),
                    @Option(label = "Sharky-775", value = "Sharky-775"),
                    @Option(label = "Zelsius CF-CMF", value = "Zelsius CF-CMF"),
            })
    String meterType() default "Itron-CF51";

    @AttributeDefinition(name = "MBus-Bridge Id", description = "The Unique Id of the mBus-Bridge you what to allocate to this device.")
    String mbusBridgeId() default "mbus0";

    @AttributeDefinition(name = "PrimaryAddress", description = "primary Address of the Mbus Component")
    int primaryAddress();

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Heat-meter Device Id [{id}]";

}