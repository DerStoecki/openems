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

    @AttributeDefinition(name = "Curve parameter: room temperature", description = "The desired room temperature, in °C.")
    int room_temp() default 20;

    @AttributeDefinition(name = "Curve parameter: slope", description = "Slope of the heating curve.")
    double slope() default 1;


    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno Heating Curve Regulator [{id}]";
}