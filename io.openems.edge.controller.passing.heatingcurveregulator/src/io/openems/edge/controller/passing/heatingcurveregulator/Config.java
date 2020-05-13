package io.openems.edge.controller.passing.heatingcurveregulator;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Consolinno Heating Curve Regulator",
        description = "Automatic regulator that adjusts heating depending on outside temperature."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Heating Controller.")
    String id() default "HeatingCurveRegulator0";

    @AttributeDefinition(name = "alias", description = "Human Readable Name of Component.")
    String alias() default "Heizkurvengesteuerter automatischer Heizregler";

    @AttributeDefinition(name = "TemperatureSensor", description = "The Temperaturesensor allocated to this controller")
    String temperatureSensorId() default "TemperatureSensor5";

    @AttributeDefinition(name = "Activation temperature", description = "If the temperature measured by the sensor falls below this value, heating starts. Unit is °C and the values needs to be equal or lower than the room temperature set in the next option.")
    int activation_temp() default 18;

    @AttributeDefinition(name = "Curve parameter: room temperature", description = "The desired room temperature, in °C.")
    int room_temp() default 20;

    @AttributeDefinition(name = "Curve parameter: slope", description = "Slope of the heating curve.")
    double slope() default 1;

    @AttributeDefinition(name = "Curve parameter: offset", description = "Offset in the heating curve to account for losses in the heating system, in °C.")
    int offset() default 5;


    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno Heating Curve Regulator [{id}]";
}