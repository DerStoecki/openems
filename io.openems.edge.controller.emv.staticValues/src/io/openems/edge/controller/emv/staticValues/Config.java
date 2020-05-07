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

    @AttributeDefinition(name = "RelaysId", description = "Select RelaysDevices",
            options = {
                    @Option(label = "Relays0", value = "Relays0"),
                    @Option(label = "Relays1", value = "Relays1"),
                    @Option(label = "Relays2", value = "Relays2"),
                    @Option(label = "Relays3", value = "Relays3"),
                    @Option(label = "Relays4", value = "Relays4"),
                    @Option(label = "Relays5", value = "Relays5"),
                    @Option(label = "Relays6", value = "Relays6"),
                    @Option(label = "Relays7", value = "Relays7"),
                    @Option(label = "Relays8", value = "Relays8"),
                    @Option(label = "Relays9", value = "Relays9"),
                    @Option(label = "Relays10", value = "Relays10"),
                    @Option(label = "Relays11", value = "Relays11"),
                    @Option(label = "Relays12", value = "Relays12"),
                    @Option(label = "Relays13", value = "Relays13"),
                    @Option(label = "Relays14", value = "Relays14"),
                    @Option(label = "Relays15", value = "Relays15"),
                    @Option(label = "Relays16", value = "Relays16"),
                    @Option(label = "Relays17", value = "Relays17"),
                    @Option(label = "Relays18", value = "Relays18"),
                    @Option(label = "Relays19", value = "Relays19"),
                    @Option(label = "Relays20", value = "Relays20"),
                    @Option(label = "Relays21", value = "Relays21"),
                    @Option(label = "Relays22", value = "Relays22"),
                    @Option(label = "Relays23", value = "Relays23"),
                    @Option(label = "Relays24", value = "Relays24"),
                    @Option(label = "Relays25", value = "Relays25"),
                    @Option(label = "Relays26", value = "Relays26"),
                    @Option(label = "Relays27", value = "Relays27"),
                    @Option(label = "Relays28", value = "Relays28"),
                    @Option(label = "Relays29", value = "Relays29"),
                    @Option(label = "Relays30", value = "Relays30"),
                    @Option(label = "Relays31", value = "Relays31"),

            })
    String[] relaysDeviceList();

    @AttributeDefinition(name = "RelaysValues", description = "RelaysValues can be changed via 1 and 0 ")
    String relaysValues() default "1100101011001010";

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

    @AttributeDefinition(name = "PwmDeviceId", description = "Select PwmDevices",
            options = {
                    @Option(label = "PwmDevice0", value = "PwmDevice0"),
                    @Option(label = "PwmDevice1", value = "PwmDevice1"),
                    @Option(label = "PwmDevice2", value = "PwmDevice2"),
                    @Option(label = "PwmDevice3", value = "PwmDevice3"),
                    @Option(label = "PwmDevice4", value = "PwmDevice4"),
                    @Option(label = "PwmDevice5", value = "PwmDevice5"),
                    @Option(label = "PwmDevice6", value = "PwmDevice6"),
                    @Option(label = "PwmDevice7", value = "PwmDevice7")

            })
    String[] PwmDeviceList();

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