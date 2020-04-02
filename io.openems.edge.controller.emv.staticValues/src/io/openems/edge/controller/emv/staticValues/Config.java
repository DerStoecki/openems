package io.openems.edge.controller.emv.staticValues;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition( //
        name = "Controller Emv Static Values", //
        description = "This Controller sets Static values to given Components.")

@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component.")
    String id() default "csvWriter0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;


    String[] temperaturSensorList();

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

            })
    String[] relaysDeviceList();

    @AttributeDefinition(name = "RelaysValues", description = "RelaysValues can be changes ")
    boolean [] relaysValues() default {true,true,false,false,true,false,true,false};

    @AttributeDefinition(name = "DacDeviceId", description = "Select DacDevices",
            options = {
                    @Option(label = "DacDevice0", value = "DacDevice0"),
                    @Option(label = "DacDevice1", value = "DacDevice1"),
                    @Option(label = "DacDevice2", value = "DacDevice2"),
                    @Option(label = "DacDevice3", value = "DacDevice3")
            })
    String[] DacDeviceList();

    @AttributeDefinition(name = "DacValues in mA", description = "Set the DacValues in mA")
    double [] dacValues() default {7,15,20,10};

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
            float [] pwmValues() default {25,100,75,40,80,50,100,10};


    String webconsole_configurationFactory_nameHint() default "Controller Emv Static Values [{id}]";

    String service_pid();
}