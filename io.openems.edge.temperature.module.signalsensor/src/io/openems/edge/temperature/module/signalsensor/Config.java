package io.openems.edge.temperature.module.signalsensor;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(name = "Signal Sensor Spi", description = "SignalSensor plugged in TemperatureModule.")
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Id", description = "Unique Id for this Error Sensor.")
    String id() default "SignalSensor0";

    @AttributeDefinition(name = "Alias", description = "Human readable name of this Sensor.")
    String alias() default "";

    @AttributeDefinition(name = "Temperature Module Id", description = "Same Id as CircuitBoard connected to it.")
    String temperatureBoardId() default "TemperatureModule0";

    @AttributeDefinition(name = "Dip Switch", description = "What Dip switch is used for this Temperature Sensor (Starting with 0)")
    short spiChannel() default (short) 0;

    @AttributeDefinition(name = "Pin Position", description = "What Pin Position of the Adc you want to use, starting with No. 0")
    short pinPosition() default 0;

    @AttributeDefinition(name = "SignalType", description = "Is the Signal an Error/Status",
            options = {
                    @Option(label = "Status", value = "Status"),
                    @Option(label = "Error", value = "Error"),
                    @Option(label = "Fault Message", value = "Fault Message")
            })
    String signalType() default "Status";


    @AttributeDefinition(name = "SignalDescription", description = "Type in Your DescriptionType for the Error/Signal/Status")
            String signalDescription() default "operation";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "SginalSensorSpi [{id}]";
}
