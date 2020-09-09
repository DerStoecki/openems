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

    @AttributeDefinition(name = "HeatMeter-Device ID", description = "Unique Id of the HeatMeter.")
    String id() default "MH-0";

    @AttributeDefinition(name = "alias", description = "Human readable name of the heat-meter.")
    String alias() default "";

    @AttributeDefinition(name = "Model", description = "Identification via Type",
            options = {
                    // value has to be one of those specified in HeatMeterType.java
                    @Option(label = "Itron-CF51", value = "ITRON_CF_51"),
                    @Option(label = "Sharky-775", value = "ZELSIUS_C5_CMF"),
                    @Option(label = "Zelsius CF-CMF", value = "SHARKY_775"),
            })
    String model() default "ITRON_CF_51";

    @AttributeDefinition(name = "MBus-Bridge Id", description = "The Unique Id of the mBus-Bridge you what to allocate to this device.")
    String mbusBridgeId() default "mbus0";

    @AttributeDefinition(name = "PrimaryAddress", description = "primary Address of the Mbus Component")
    int primaryAddress();

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Heat-meter Device Id [{id}]";

}