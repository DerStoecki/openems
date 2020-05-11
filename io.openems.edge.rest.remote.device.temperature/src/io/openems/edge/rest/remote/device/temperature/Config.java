package io.openems.edge.rest.remote.device.temperature;


import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Temperature Sensor Remote ",
        description = " The Devices you wish to Communicate with. As a Master --> register your Slaves. If Slaves want "
                + "to Communicate with Master tick boolean --> isMaster")
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Unique Id of Device", description = "Id of the Device")
    String id() default "RemoteTemperature0";

    @AttributeDefinition(name = "Slave/Master ModuleId", description = "Id of the Rest Device you want to communicate with"
    + "e.g. Leaflet where the original / true Temperature Sensor is.")
    String slaveMasterId() default "Leafleft0";

    @AttributeDefinition(name = "Real TemperatureSensorId", description = "Id of the Real TemperatureSensor on Master/Slave")
    String realTemperatureSensorId() default "TemperatureSensor0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Rest Communicator [{id}]";


}
