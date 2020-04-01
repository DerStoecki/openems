package io.openems.edge.controller.csvwriter.emv;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition( //
        name = "Controller Emv Csv Writer", //
        description = "This Controller Writes Timestamp Devices ChannelID and Values in a CSV file.")

@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component.")
    String id() default "csvWriter0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "Path", description = "Path to write the CSV File")
    String path () default "/home/sshconsolinno/DataLog/";

    @AttributeDefinition(name = "TemperatureSensorId", description = "Select TemperatureSensors",
            options = {
                    @Option(label = "TemperatureSensor0", value = "TemperatureSensor0"),
                    @Option(label = "TemperatureSensor1", value = "TemperatureSensor1"),
                    @Option(label = "TemperatureSensor2", value = "TemperatureSensor2"),
                    @Option(label = "TemperatureSensor3", value = "TemperatureSensor3"),
                    @Option(label = "TemperatureSensor4", value = "TemperatureSensor4"),
                    @Option(label = "TemperatureSensor5", value = "TemperatureSensor5"),
                    @Option(label = "TemperatureSensor6", value = "TemperatureSensor6"),
                    @Option(label = "TemperatureSensor7", value = "TemperatureSensor7"),
                    @Option(label = "TemperatureSensor8", value = "TemperatureSensor8"),
                    @Option(label = "TemperatureSensor9", value = "TemperatureSensor9"),
                    @Option(label = "TemperatureSensor10", value = "TemperatureSensor10"),
                    @Option(label = "TemperatureSensor11", value = "TemperatureSensor11"),
                    @Option(label = "TemperatureSensor12", value = "TemperatureSensor12"),
                    @Option(label = "TemperatureSensor13", value = "TemperatureSensor13"),
                    @Option(label = "TemperatureSensor14", value = "TemperatureSensor14"),
                    @Option(label = "TemperatureSensor15", value = "TemperatureSensor15"),

            })
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
    @AttributeDefinition(name = "DacDeviceId", description = "Select DacDevices",
            options = {
                    @Option(label = "DacDevice0", value = "DacDevice0"),
                    @Option(label = "DacDevice1", value = "DacDevice1"),
                    @Option(label = "DacDevice2", value = "DacDevice2"),
                    @Option(label = "DacDevice3", value = "DacDevice3")
            })
            String [] DacDeviceList();

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

    @AttributeDefinition(name = "Meter d0", description = "Select Meter d0",
            options = {
                    @Option(label = "meter0", value = "meter0"),
                    @Option(label = "meter1", value = "meter1"),
            })
    String[] meterList();

    @AttributeDefinition(name = "Time Interval", description = "Interval of Time in seconds")
            double timeInterval() default 1;

    String webconsole_configurationFactory_nameHint() default "Controller Emv Csv Writer [{id}]";
    String service_pid();
}
