package io.openems.edge.controller.emv.staticValues;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition( //
        name = "Controller Emv Static Values", //
        description = "This Controller sets Static values to given Components.")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component.")
    String id() default "csvStaticComponent0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "RelaysId", description = "Select RelaysDevices")
    String[] relaysDeviceList() default {"M6"};

    @AttributeDefinition(name = "RelaysValues", description = "RelaysValues can be changed via 1 and 0 ")
    String relaysValues() default "1";

    @AttributeDefinition(name = "DacDeviceId", description = "Select DacDevices",
            options = {
                    @Option(label = "DacDevice0", value = "DacDevice0"),
                    @Option(label = "DacDevice1", value = "DacDevice1"),
                    @Option(label = "DacDevice2", value = "DacDevice2"),
                    @Option(label = "DacDevice3", value = "DacDevice3")
            })
    String[] DacDeviceList();

    @AttributeDefinition(name = "DacValues in mA", description = "Set the DacValues in mA")
    double[] dacValues() default {7, 15, 20, 10};

    @AttributeDefinition(name = "PwmDeviceId", description = "Select PwmDevices")
    String[] PwmDeviceList() default {"PM6"};

    @AttributeDefinition(name = "PwmValues", description = "PwmValues in %")
    float[] pwmValues() default {25, 100, 75, 40, 80, 50, 100, 10};

    @AttributeDefinition(name = "Sc16Id", description = "Id of the Mainmodule for the Sc16")
    String mainModuleId() default "LeafletSc16";

    @AttributeDefinition(name = "Sc16States", description = "What Sc16 states you want to control",
            options = {
                    @Option(label = "LED-RED", value = "LED-RED"),
                    @Option(label = "LED-YELLOW", value = "LED-YELLOW"),
                    @Option(label = "ENABLE-OUTPUT", value = "ENABLE-OUTPUT")}
    )
    String[] sc16ChoiceList() default {};

    @AttributeDefinition(name = "Sc16Values", description = "Values for the Statuses")
    String sc16Values() default "101";

    String webconsole_configurationFactory_nameHint() default "Controller Emv Static Values [{id}]";

    String service_pid();
}