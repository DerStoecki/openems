package io.openems.edge.bridge.lmnwired;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
        name = "Bridge LMN Wired", //
        description = "Provides a service for connecting to, querying and writing to a wired LMN device.")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "lmnwired0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "Port-Name", description = "The name of the serial port - e.g. '/dev/ttyUSB0' or 'COM3'")
    String portName() default "/dev/ttyUSB0";

    @AttributeDefinition(name = "Baudrate", description = "The baudrate - e.g. 9600, 19200, 38400, 57600 or 115200")
    int baudRate() default 460800;

    @AttributeDefinition(name = "TimeSlots", description = "Timeslots for device addressing, eg. 32")
    int timeSlots() default 32;

    @AttributeDefinition(name = "TimeSlotDuration in ms", description = "Timeslot duration for device addressing, eg. 5")
    int timeSlotDurationInMs() default 5;

    @AttributeDefinition(name = "Devices", description = "Current Devices in list")
    String[] devices() default {};

    String webconsole_configurationFactory_nameHint() default "Bridge LMN wired [{id}]";
}
