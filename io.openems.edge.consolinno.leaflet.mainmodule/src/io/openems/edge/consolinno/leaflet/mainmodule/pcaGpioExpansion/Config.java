package io.openems.edge.consolinno.leaflet.mainmodule.pcaGpioExpansion;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno LeafLet Mainmodule Pca",
        description = "Module build in LeafletModule; Here: Pca."
)
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Pca Module Name", description = "The Unique Id of the Module.")
    String id() default "LeafletSc16";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Version", description = "What Version of Consolinno Leaflet MainModule are you using.",
            options = @Option(label = "Version 0.05", value = "0.05"))
    String version() default "0.05";

    @AttributeDefinition(name = "Bus Device Address", description = "What I2C Bus are you using.")
    int bus_address() default 1;

    @AttributeDefinition(name = "Pca Address", description = "The address of your Pca.")
    int pca_address() default 0x41;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Consolinno Leaflet Module Pca [{id}]";
}
