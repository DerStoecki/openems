package io.openems.edge.apartmentmodule;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno Apartment Module",
        description = "A module to map Modbus calls to OpenEMS channels for a Consolinno Apartment Module."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "ApartmentModule-Device ID", description = "Unique Id of the Apartment Module.")
    String id() default "ApartmentModule0";

    @AttributeDefinition(name = "ModBus-Bridge Id", description = "The Unique Id of the modBus-Bridge you what to allocate to this device.")
    String modbusBridgeId() default "modbus0";

    @AttributeDefinition(name = "alias", description = "Human readable name of the Apartment Module.")
    String alias() default "";

    @AttributeDefinition(name = "ModBus-Unit Id", description = "Unit Id of the Component. Decides if the Apartment module is in top or bottom configuration.")
    ModbusId modbusUnitId() default ModbusId.ID_2;

    @AttributeDefinition(name = "Temperature sensor calibration", description = "Calibration value for the PT1000 temperature sensor.")
    int tempCal() default 70;

    // Debug options.
    @AttributeDefinition(name = "Debug", description = "Debug.")
    boolean debug() default false;

    @AttributeDefinition(name = "Turn on relay 1", description = "Turn on relay 1.")
    boolean turnOnRelay1() default false;

    @AttributeDefinition(name = "Turn on relay 2", description = "Turn on relay 2.")
    boolean turnOnRelay2() default false;

    @AttributeDefinition(name = "Relay time.", description = "Relay time.")
    int relayTime() default 0;

    @AttributeDefinition(name = "Reset External Request Flag", description = "Writes 0 in Holding Register 1.")
    boolean resetRequestFlag() default false;

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Consolinno Apartment Module Device [{id}]";

}