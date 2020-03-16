package io.openems.edge.controller.heatpump.device;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(//
        name = "Controller Heat Pump", //
        description = "Controller to calculate and set h Const Ref Min Max and rRem")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "ControllerHeatPump0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "HeatPumpId", description = "Unique Id of the Pump.")
    String heatPumpId() default "HeatPump0";

     @AttributeDefinition(name = "Maximum system pressure", description = "Maximum System Pressure in bar")
    double maxPressure() default 16;

    @AttributeDefinition(name = "MinRange", description = "MinReference Value of Min Pressure in bar.")
    double hRefMin() default 0.1;

    @AttributeDefinition(name = "MaxRange", description = "MaxReference Value of Max Pressure in bar")
    double hRefMax() default 5;

    @AttributeDefinition(name = "Wanted Pressure in bar", description = "Pressure Level in bar (depending on Min and Max Range")
    double rRem() default 10.5;

    @AttributeDefinition(name = "Command Settings", description = "If you wish you can add additional Commands which will be handled"
            + "Remote needs to selected of not using AutoAdapt.",
            options = {
                    @Option(label = "Remote", value = "Remote"),
                    @Option(label = "Start", value = "Start"),
                    @Option(label = "Stop", value = "Stop"),
                    @Option(label = "MinMotorCurve", value = "MinMotorCurve"),
                    @Option(label = "MaxMotorCurve", value = "MaxMotorCurve"),
                    @Option(label = "ConstFrequency", value = "ConstFrequency"),
                    @Option(label = "ConstPressure", value = "ConstPressure"),
                    @Option(label = "AutoAdapt", value = "AutoAdapt")
            })
    String[] commands() default {"Remote", "Start", "ConstFrequency"};

    String webconsole_configurationFactory_nameHint() default "Controller Heat Pump [{id}]";
}