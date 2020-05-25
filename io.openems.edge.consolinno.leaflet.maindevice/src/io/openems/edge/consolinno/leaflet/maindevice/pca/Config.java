package io.openems.edge.consolinno.leaflet.maindevice.pca;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno LeafLet MainModule Pca Device",
        description = "The Connected Device on the Pca."
)
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "PcaDeviceId", description = "The Unique Id of the Device.")
    String id() default "IO0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "NotFaultHbus5";

    @AttributeDefinition(name = "Position of PcaDevice", description = "The Position of Device on Pca e.g. IO0 = 0.")
    int pinPosition() default 0;

    @AttributeDefinition(name = "PcaModuleId", description = "Unique Id of the LeafLetModulePca Id")
    String moduleId() default "LeafletPca";

    @AttributeDefinition(name = "OnOff or ErrorFlag", description = "Choose if the Device shows OnOff or ErrorFlags",
            options = {
                    @Option(label = "OnOff", value = "OnOff"),
                    @Option(label = "Error", value = "Error")})
    String descriptor() default "OnOff";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Consolinno Leaflet Module Pca Device [{id}]";

}