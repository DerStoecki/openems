package io.openems.edge.relays.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Consolinno Relais",
        description = "Relais with a Channel to Open and Close."
)

@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Relays Id", description = "Unique Id of the Relays.")
    String id() default "Relays0";

    @AttributeDefinition(name = "Alias", description = "Human readable name for this Component.")
    String alias() default "";

    @AttributeDefinition(name = "Relays Type", description = "Is the Relays an Opener or closer.",
            options = {
                    @Option(label = "Opener", value = "Opener"),
                    @Option(label = "Closer", value = "Closer")
            })
    String relaysType() default "Closer";

    @AttributeDefinition(name = "Relays-Module Id", description = "Id of the Relays-Module allocated to this Relays-Device.")
    String relaysBoard_id() default "relayModule0";

    @AttributeDefinition(name = "Position", description = "The position of the Relays. Starting with 0.")
    int position() default 0;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Relays [{id}]";
}