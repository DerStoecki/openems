package io.openems.edge.rest.remote.device.general;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Rest Remote Device ",
        description = " The Devices you wish to Communicate with. As a Master --> register your Slaves. If Slaves want "
                + "to Communicate with Master tick boolean --> isMaster")
@interface Config {


    String service_pid();

    @AttributeDefinition(name = "Unique Id of Device", description = "Id of the Device")
    String id() default "RemoteDevice0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Slave/Master ModuleId", description = "Id of the Rest Device you want to communicate with"
            + "e.g. Leaflet where the original / true Temperature Sensor is.")
    String slaveMasterId() default "Leafleft0";

    @AttributeDefinition(name = "Real Device Id", description = "Id of the device on Master/Slave you want to communicate with.")
    String realDeviceId() default "TemperatureSensor0";

    @AttributeDefinition(name = "Type Selection", description = "What Device Type do you want to Read/Write to",
            options = {
                    @Option(label = "TemperatureSensor", value = "TemperatureSensor"),
                    @Option(label = "Relays", value = "Relays")
            })
    String deviceType() default "TemperatureSensor";

    @AttributeDefinition(name = "Channel", description = "Channel of the Device you want to read",
            options = {
                    @Option(label = "Temperature", value = "Temperature"),
                    @Option(label = "OnOff", value = "OnOff"),
                    @Option(label = "IsCloser", value = "OnOff")
            })
    String deviceChannel() default "Temperature";

    @AttributeDefinition(name = "AutoAdapting of Relays", description = "In case you don't know if the Relays is "
            + "Normally Open or Normally Closed the Remote Device can Adapt automatically for you"
            + "Meaning if you want to Activate something it will depend on Opener Or Closer")
    boolean autoAdapt();

    @AttributeDefinition(name = "OperationType", description = "Do you want to Read or Write",
            options = {
                    @Option(label = "Read", value = "Read"),
                    @Option(label = "Write", value = "Write")
            }
    )
            String deviceMode() default "Read";

    boolean enabled() default true;


    String webconsole_configurationFactory_nameHint() default "Rest Device [{id}]";

}
