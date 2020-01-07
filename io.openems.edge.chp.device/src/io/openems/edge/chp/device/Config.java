package io.openems.edge.chp.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Bhkw Vitobloc",
        description = "Depending on VersionId you can activate up to X Devices per Bhkw Module."
)

@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Chp Id", description = "Unique Id of the Chp Device.")
    String id() default "Chp0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Chp.")
    String alias() default "";

    @AttributeDefinition(name = "Chp Type", description = "What Chp Type do you want to use(Not important for pure" + "Controlling and no further Information ).",
            options = {
                    @Option(label = "Vitobloc 200 EM - 6/15", value = "EM_6_15"),
                    @Option(label = "Vitobloc 200 EM - 9/20", value = "EM_9_20"),
                    @Option(label = "Vitobloc 200 EM - 20/39", value = "EM_20_39"),
                    @Option(label = "Vitobloc 200 EM - 20/39_70", value = "EM_20_39_70"),
                    @Option(label = "Vitobloc 200 EM - 50/81", value = "EM_50_81"),
                    @Option(label = "Vitobloc 200 EM - 70/115", value = "EM_70_115"),
                    @Option(label = "Vitobloc 200 EM - 100/167", value = "EM_100_167"),
                    @Option(label = "Vitobloc 200 EM - 140/207 SCR", value = "EM_140_207"),
                    @Option(label = "Vitobloc 200 EM - 199/263", value = "EM_199_263"),
                    @Option(label = "Vitobloc 200 EM - 199/293", value = "EM_199_293"),
                    @Option(label = "Vitobloc 200 EM - 238/363", value = "EM_238_363"),
                    @Option(label = "Vitobloc 200 EM - 363/498", value = "EM_363_498"),
                    @Option(label = "Vitobloc 200 EM - 401/549 SCR", value = "EM_401_549"),
                    @Option(label = "Vitobloc 200 EM - 530/660", value = "EM_530_660"),
                    @Option(label = "Vitobloc 200 BM - 36/66", value = "BM_36_66"),
                    @Option(label = "Vitobloc 200 BM - 55/88", value = "BM_55_88"),
                    @Option(label = "Vitobloc 200 BM - 190/238", value = "BM_190_238"),
                    @Option(label = "Vitobloc 200 BM - 366/437", value = "BM_366_437"),
                    @Option(label = "Not in list", value = "Null")
            })
    String chpType() default "EM_140_207";

    @AttributeDefinition(name = "ChpModule Id", description = "Id of the ChpModule you previously activated.")
    String chpModuleid() default "ChpModule0";

    @AttributeDefinition(name = "min Limit of Chp", description = "Minimum of your Chp API mA.")
    short minLimit() default 0;

    @AttributeDefinition(name = "max Limit of Chp", description = "Maximum of your Chp API mA.")
    short maxLimit() default 20;

    @AttributeDefinition(name = "Percentage Range", description = "Where is your percentage range (depending on API) starting: 0-100%(type0) or 50-100% (type 50).")
    int percentageRange() default 0;

    @AttributeDefinition(name = "Position on Module", description = "On what Position is your Chp connected with the Module?")
    int position() default 0;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Chp Device [{id}]";

}
