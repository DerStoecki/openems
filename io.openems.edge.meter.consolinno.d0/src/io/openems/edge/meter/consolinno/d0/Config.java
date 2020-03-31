package io.openems.edge.meter.consolinno.d0;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

import io.openems.edge.meter.api.MeterType;

@ObjectClassDefinition( //
        name = "Meter Consolinno D0", //
        description = "Implements generic D0 meter.")

@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "meter0";

    @AttributeDefinition(name = "Meter serial number", description = "Serial number of meter.")
    String serialNumber() default "";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";
    @AttributeDefinition(name = "Meter-Type", description = "Grid (default), Production and Consumption")
    MeterType type() default MeterType.GRID;

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "Use OBIS", description = "Select obis for usage?",
            options = {
                    @Option(label = "96.1.0*255", value = "1-0:96.1.0*255"),
                    @Option(label = "1.8.0*255", value = "1-0:1.8.0*255"),
                    @Option(label = "1.8.1*255", value = "1-0:1.8.1*255"),
                    @Option(label = "1.8.2*255", value = "1-0:1.8.2*255"),
                    @Option(label = "2.8.0*255", value = "1-0:2.8.0*255"),
                    @Option(label = "2.8.1*255", value = "1-0:2.8.1*255"),
                    @Option(label = "2.8.2*255", value = "1-0:2.8.2*255"),
                    @Option(label = "16.7.0*255", value = "1-0:16.7.0*255"),
                    @Option(label = "32.7.0*255", value = "1-0:32.7.0*255"),
                    @Option(label = "52.7.0*255", value = "1-0:52.7.0*255"),
                    @Option(label = "72.7.0*255", value = "1-0:72.7.0*255"),
                    @Option(label = "31.7.0*255", value = "1-0:31.7.0*255"),
                    @Option(label = "51.7.0*255", value = "1-0:51.7.0*255"),
                    @Option(label = "71.7.0*255", value = "1-0:71.7.0*255"),
                    @Option(label = "81.7.1*255", value = "1-0:81.7.1*255"),
                    @Option(label = "81.7.2*255", value = "1-0:81.7.2*255"),
                    @Option(label = "81.7.4*255", value = "1-0:81.7.4*255"),
                    @Option(label = "81.7.15*255", value = "1-0:81.7.15*255"),
                    @Option(label = "81.7.26*255", value = "1-0:81.7.26*255"),
                    @Option(label = "14.7.0*255", value = "1-0:14.7.0*255"),
                    @Option(label = "1.8.0*96", value = "1-0:1.8.0*96"),
                    @Option(label = "1.8.0*97", value = "1-0:1.8.0*97"),
                    @Option(label = "1.8.0*98", value = "1-0:1.8.0*98"),
                    @Option(label = "1.8.0*99", value = "1-0:1.8.0*99"),
                    @Option(label = "1.8.0*100", value = "1-0:1.8.0*100"),
                    @Option(label = "0.2.0*255", value = "1-0:0.2.0*255"),
                    @Option(label = "96.90.2*255", value = "1-0:96.90.2*255"),
                    @Option(label = "97.97.0*255", value = "1-0:97.97.0*255"),
            })
    String[] use_obis_list();

    @AttributeDefinition(name = "LMN-ID", description = "ID of LMNWired brige.")
    String lmnwired_id() default "lmnwired0";

    String webconsole_configurationFactory_nameHint() default "Meter Consolinno D0 [{id}]";
    String service_pid();
}
