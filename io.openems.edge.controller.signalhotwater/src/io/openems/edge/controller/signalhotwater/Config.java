package io.openems.edge.controller.signalhotwater;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Consolinno SignalHotWater",
        description = "Controller that sends the signal \"need hot water\"."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Heating Controller.")
    String id() default "SignalHotWater0";

    @AttributeDefinition(name = "alias", description = "Human Readable Name of Component.")
    String alias() default "Warmwasser Anforderung";

    @AttributeDefinition(name = "TemperatureSensor", description = "The Temperature sensor in the water tank, allocated to this controller")
    String temperatureSensorId() default "T_PS_oben";

    @AttributeDefinition(name = "Minimum temperature", description = "Minimum temperature of the water tank. Unit is °C")
    int min_temp() default 60;

    @AttributeDefinition(name = "response timeout", description = "How long to wait for the response signal before continuing without it. Unit is seconds.")
    int response_timeout() default 10;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno SignalHotWater [{id}]";
}