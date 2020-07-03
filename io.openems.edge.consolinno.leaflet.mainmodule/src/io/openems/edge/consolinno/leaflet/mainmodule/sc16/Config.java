package io.openems.edge.consolinno.leaflet.mainmodule.sc16;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno LeafLet Mainmodule Sc16",
        description = "Module build in LeafletModule; Here: Sc16."
)
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "Sc16 Module Name", description = "The Unique Id of the Module.")
    String id() default "LeafletSc16";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Version", description = "What Version of Consolinno Leaflet MainModule are you using.",
            options = @Option(label = "Version 0.05", value = "0.05"))
    String version() default "0.05";

    @AttributeDefinition(name = "SpiChannel", description = "What SpiChannel you are using (usually 8).")
    int spiChannel() default 8;

    @AttributeDefinition(name = "Frequency", description = "SPI Clock Frequency for this Device")
            int frequency() default 40000;

    @AttributeDefinition(name = "InterruptActivate", description = "Tick on if you want Interrupt Settings.")
    boolean interruptActivate() default false;

    @AttributeDefinition(name = "InterruptSet", description = "What Type of Interrupt do you want",
    options = {
            @Option(label = "FCR", value = "FCR"),
            @Option(label = "TCR", value = "TCR")
    })
            String interruptSetting() default "FCR";

    @AttributeDefinition(name = "InterruptData", description = "Set Write Flags with 0 and 1; max 8bit")
            String interruptData() default "00000000";

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Consolinno Leaflet Module Sc16 [{id}]";
}
