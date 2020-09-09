package io.openems.edge.meter.watermeter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(//
        name = "Meter Water M-Bus", //
        description = "Implements M-Bus water meters.")
@interface Config {

   @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "MW-0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Mbus-ID", description = "ID of M-Bus brige.")
    String mbus_id() default "mbus0";

    @AttributeDefinition(name = "Mbus PrimaryAddress", description = "PrimaryAddress of the M-Bus device.")
    int primaryAddress();

    @AttributeDefinition(name = "Model", description = "Identification via Type",
            options = {
                    @Option(label = "Relay PadPuls M2", value = "PAD_PULS_M2"),
                    @Option(label = "Itron BM +m", value = "ITRON_BM_M"),
                    @Option(label = "None of the above", value = "none"),
            })
    String model() default "none";

//    @AttributeDefinition(name="Local Time", description = "Use actual time instead of timestamp from meter")
//    boolean localtime() default false;

    @AttributeDefinition(name="Address Volume", description = "Record position of metered volume in M-Bus Telegram; only relevant if \"none\" meter is selected")
    int volAddress();

    @AttributeDefinition(name="Address Timestamp", description = "Record position of metered volume in M-Bus Telegram; only relevant if \"none\" meter is selected and \"Localtime\" is unselected")
    int timeStampAddress();

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Water meter M-Bus [{id}]";

}
