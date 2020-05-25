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
    String[] PwmDeviceList()default{"PM6"};

    @AttributeDefinition(name = "PwmValues", description = "PwmValues in %")
    float[] pwmValues() default {25, 100, 75, 40, 80, 50, 100, 10};

    @AttributeDefinition(name = "PcaDevices", description = "Select PcaDevices")
    String[] pcaDevice() default {"IO1"};

    @AttributeDefinition(name = "PcaValue", description = "What would you like to write in the PcaDevice.")
    String pcaDeviceValue() default "1";

    @AttributeDefinition(name = "GpioWriteDevice", description = "What GPIO Devices do you want to use.")
            String [] gpioDevices() default {"ChpOnOffStatus0"};

     @AttributeDefinition(name = "GpioWriteValues", description = "Add the Values e.g. ErrorFlags")
             String gpioDeviceValues () default "111";

    String webconsole_configurationFactory_nameHint() default "Controller Emv Static Values [{id}]";

    String service_pid();
}