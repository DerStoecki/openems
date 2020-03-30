package io.openems.edge.chp.device.simpleDac;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "DAC Device",
        description = "A combined heat and power system."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "DAC-Device ID", description = "Unique Id of the DAC Device.")
    String id() default "DacDevice0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Chp.")
    String alias() default "";


    @AttributeDefinition(name = "DacModule Id", description = "Id of the DacModule you previously activated.")
    String chpModuleId() default "ChpModule0";

    @AttributeDefinition(name = "min - Limit of DAC", description = "Minimum of your Chp API mA.")
    short minLimit() default 0;

    @AttributeDefinition(name = "max - Limit of DAC", description = "Maximum of your Chp API mA.")
    short maxLimit() default 20;

    @AttributeDefinition(name = "Percentage - range", description = "Where is your percentage range (depending on API)."
            + "starting: 0-100%(type 0) or 50-100% (type 50).")
    int percentageRange() default 0;

    @AttributeDefinition(name = "Position on Module", description = "On what Position is your Chp connected with the Module?")
    int position() default 0;


    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "DAC Device [{id}]";

}
