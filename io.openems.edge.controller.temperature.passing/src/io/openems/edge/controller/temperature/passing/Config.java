package io.openems.edge.controller.temperature.passing;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Consolinno Controller Passing",
        description = "This Controller regulates the Pump and Valves for Heating."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Passing Controller.")
    String id() default "ControllerPassing0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Controller.")
    String alias() default "Uebergabestation";

    @AttributeDefinition(name = "Primary Forward Temperature Sensor Id", description = "The TemperatureSensor Id used to measure the Primary Forward Temperature.")
    String primary_Forward_Sensor() default "TemperatureSensor0";

    @AttributeDefinition(name = "Primary Rewind Temperature Sensor Id", description = "The TemperatureSensor Id used to measure the Primary Rewind Temperature.")
    String primary_Rewind_Sensor() default "TemperatureSensor1";

    @AttributeDefinition(name = "Secundary Forward Temperature Sensor Id", description = "The TemperatureSensor Id used to measure the Secundary Forward Temperature.")
    String secundary_Forward_Sensor() default "TemperatureSensor2";

    @AttributeDefinition(name = "Secundary Rewind Temperature Sensor Id", description = "The TemperatureSensor Id used to measure the Secundary Rewind Temperature.")
    String secundary_Rewind_Sensor() default "TemperatureSensor3";

    @AttributeDefinition(name = "Valve Open Realis Id", description = "The Relais Id used to open the valve.")
    String valve_Open_Relais() default "Relais0";

    @AttributeDefinition(name = "Valve Close Realis Id", description = "The Relais Id used to open the valve.")
    String valve_Close_Relais() default "Relais1";

    @AttributeDefinition(name = "Pump Relais Id", description = "The Relais Id used to Activate the Pump.")
    String pump_id() default "Relais2";

    @AttributeDefinition(name = "Heating Time", description = "The Time needed to heat up the Primary Forward (t in seconds).")
    int heating_Time() default 50;

    @AttributeDefinition(name = "Valve Time", description = "The time needed to Open and Close the valve (t in seconds)")
    int valve_Time() default 30;
    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Gaspedal [{id}]";

}

