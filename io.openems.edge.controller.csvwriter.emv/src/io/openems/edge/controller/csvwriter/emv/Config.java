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
    String path() default "/home/sshconsolinno/DataLog/";

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
                    @Option(label = "TemperatureSensor16", value = "TemperatureSensor16"),
                    @Option(label = "TemperatureSensor17", value = "TemperatureSensor17"),
                    @Option(label = "TemperatureSensor18", value = "TemperatureSensor18"),
                    @Option(label = "TemperatureSensor19", value = "TemperatureSensor19"),
                    @Option(label = "TemperatureSensor20", value = "TemperatureSensor20"),
                    @Option(label = "TemperatureSensor21", value = "TemperatureSensor21"),
                    @Option(label = "TemperatureSensor22", value = "TemperatureSensor22"),
                    @Option(label = "TemperatureSensor23", value = "TemperatureSensor23"),
                    @Option(label = "TemperatureSensor24", value = "TemperatureSensor24"),
                    @Option(label = "TemperatureSensor25", value = "TemperatureSensor25"),
                    @Option(label = "TemperatureSensor26", value = "TemperatureSensor26"),
                    @Option(label = "TemperatureSensor27", value = "TemperatureSensor27"),
                    @Option(label = "TemperatureSensor28", value = "TemperatureSensor28"),
                    @Option(label = "TemperatureSensor29", value = "TemperatureSensor29"),
                    @Option(label = "TemperatureSensor30", value = "TemperatureSensor30"),
                    @Option(label = "TemperatureSensor31", value = "TemperatureSensor31"),
                    @Option(label = "TemperatureSensor32", value = "TemperatureSensor32"),
                    @Option(label = "TemperatureSensor33", value = "TemperatureSensor33"),
                    @Option(label = "TemperatureSensor34", value = "TemperatureSensor34"),
                    @Option(label = "TemperatureSensor35", value = "TemperatureSensor35"),
                    @Option(label = "TemperatureSensor36", value = "TemperatureSensor36"),
                    @Option(label = "TemperatureSensor37", value = "TemperatureSensor37"),
                    @Option(label = "TemperatureSensor38", value = "TemperatureSensor38"),
                    @Option(label = "TemperatureSensor39", value = "TemperatureSensor39"),
                    @Option(label = "TemperatureSensor40", value = "TemperatureSensor40"),
                    @Option(label = "TemperatureSensor41", value = "TemperatureSensor41"),
                    @Option(label = "TemperatureSensor42", value = "TemperatureSensor42"),
                    @Option(label = "TemperatureSensor43", value = "TemperatureSensor43"),
                    @Option(label = "TemperatureSensor44", value = "TemperatureSensor44"),
                    @Option(label = "TemperatureSensor45", value = "TemperatureSensor45"),
                    @Option(label = "TemperatureSensor46", value = "TemperatureSensor46"),
                    @Option(label = "TemperatureSensor47", value = "TemperatureSensor47"),
                    @Option(label = "TemperatureSensor48", value = "TemperatureSensor48"),
                    @Option(label = "TemperatureSensor49", value = "TemperatureSensor49"),
                    @Option(label = "TemperatureSensor50", value = "TemperatureSensor50"),
                    @Option(label = "TemperatureSensor51", value = "TemperatureSensor51"),
                    @Option(label = "TemperatureSensor52", value = "TemperatureSensor52"),
                    @Option(label = "TemperatureSensor53", value = "TemperatureSensor53"),
                    @Option(label = "TemperatureSensor54", value = "TemperatureSensor54"),
                    @Option(label = "TemperatureSensor55", value = "TemperatureSensor55"),
                    @Option(label = "TemperatureSensor56", value = "TemperatureSensor56"),
                    @Option(label = "TemperatureSensor57", value = "TemperatureSensor57"),
                    @Option(label = "TemperatureSensor58", value = "TemperatureSensor58"),
                    @Option(label = "TemperatureSensor59", value = "TemperatureSensor59"),
                    @Option(label = "TemperatureSensor60", value = "TemperatureSensor60"),
                    @Option(label = "TemperatureSensor61", value = "TemperatureSensor61"),
                    @Option(label = "TemperatureSensor62", value = "TemperatureSensor62"),
                    @Option(label = "TemperatureSensor63", value = "TemperatureSensor63"),

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

    @AttributeDefinition(name = "DacDeviceId", description = "Select DacDevices",
            options = {
                    @Option(label = "DacDevice0", value = "DacDevice0"),
                    @Option(label = "DacDevice1", value = "DacDevice1"),
                    @Option(label = "DacDevice2", value = "DacDevice2"),
                    @Option(label = "DacDevice3", value = "DacDevice3")
            })
    String[] DacDeviceList();

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

    @AttributeDefinition(name = "Sc16Devices", description = "Select Sc16Device")
    String[] doubleUartList()default {"Gpio0"};


    @AttributeDefinition(name = "Time Interval", description = "Interval of Time in seconds")
    double timeInterval() default 1;

    String webconsole_configurationFactory_nameHint() default "Controller Emv Csv Writer [{id}]";

    String service_pid();
}
