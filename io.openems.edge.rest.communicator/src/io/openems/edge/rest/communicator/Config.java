package io.openems.edge.rest.communicator;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Rest Communicator ",
        description = " The Devices you wish to Communicate with. As a Master --> register your Slaves. If Slaves want "
                + "to Communicate with Master tick boolean --> isMaster")
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Unique Id of Device", description = "Id of the Device you want to communicate with."
            + "(You can declare them yourself id will be only important for remote Devices)")
    String id() default "Leafleft0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Ip Address", description = "Ip Address of Device you want to communicate with.")
    String ipAddress() default "192.168.101.1";

    @AttributeDefinition(name = "Port", description = "Open Port of Device")
    String port() default "8084";

    @AttributeDefinition(name = "AuthorisationHeader - User", description = "UserName to access Device")
    String username() default "Admin";

    @AttributeDefinition(name = "Password", description = "Password for authorization")
    String password() default "";

    @AttributeDefinition(name = "IsMaster", description = "Only Tick true if your Device is a Slave and you want "
            + "to Communicate with Master.")

    boolean isMaster() default false;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Rest Communicator [{id}]";

}
