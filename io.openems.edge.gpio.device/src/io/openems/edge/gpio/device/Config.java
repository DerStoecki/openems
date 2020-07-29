package io.openems.edge.gpio.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Raspberry Pi Gpio Device",
        description = "Gpio Pin used to get basic Information."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Gpio Device Id", description = "Unique Id of the Gpio Device.")
    String id() default "ChpOnOffStatus0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Gpio Information Type.", description = "Information Type of gpio.",
            options = {
                    @Option(label = "Error", value = "Error"),
                    @Option(label = "OnOff", value = "OnOff")
            })
    String informationType() default "OnOff";

    @AttributeDefinition(name = "Pin Position", description = "Position of the Gpio Device on Raspberry Pi.",
            options = {
                    @Option(label = "1.1", value = "1.1"),
                    @Option(label = "1.2", value = "1.2"),
                    @Option(label = "1.3", value = "1.3"),
                    @Option(label = "1.4", value = "1.4"),
                    @Option(label = "2.1", value = "2.1"),
                    @Option(label = "2.2", value = "2.2"),
            })
    String pinPosition() default "1.1";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Gpio Device [{id}]";

}
