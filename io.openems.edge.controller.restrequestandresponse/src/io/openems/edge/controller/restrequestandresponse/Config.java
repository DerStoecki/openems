package io.openems.edge.controller.restrequestandresponse;

import com.oracle.webservices.internal.api.message.PropertySet;
import com.sun.xml.internal.ws.util.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
            name = "Controller RestRequestAndResponse",
            description = " Rest Controller, needed for the communication with different OpenemsModules.")

@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Controller - ID", description = "Unique Id of Controller.")
    String id() default "GetterAndSetter";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    boolean enabled() default true;
}
