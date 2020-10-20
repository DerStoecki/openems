package io.openems.edge.powerplant.analog;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Power Plant Analog",
        description = "A PowerPlant controlled by an analogue output (e.g. 0-10V)."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "PowerPlant Name", description = "Unique Id of the Powerplant.")
    String id() default "PowerPlant0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Analogue Device Id", description = "What Analogue Device is responsible for sending a signal.")
    String analogueDevice() default "LucidControlDeviceOutput0";

    @AttributeDefinition(name = "Signal Sensors", description = "Device Ids of the SignalSensors e.g. Spi Signal Sensor")
    String[] errorBits() default {"SignalSensor0", "SignalSensor1", "SignalSensor2"};

    @AttributeDefinition(name = "Maximum Kw Output", description = "Maximum kW Output possible for this device")
    int maxKw() default 200;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Power Plant [{id}]";
}