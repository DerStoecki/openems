package io.openems.edge.controller.pump.grundfos;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(//
        name = "Controller Pump Grundfos", //
        description = "Controller to operate a Gundfos pump in constant pressure mode over GENIbus.")
@interface Config {

    @AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
    String id() default "ControllerPumpGrundfos0";

    @AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
    String alias() default "";

    @AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
    boolean enabled() default true;

    @AttributeDefinition(name = "PumpId", description = "Unique Id of the Pump.")
    String pumpId() default "Pump0";

    @AttributeDefinition(name = "Control mode", description = "Control mode - 'constant pressure' or 'constant frequency'")
    ControlMode controlMode() default ControlMode.CONST_PRESSURE;

     @AttributeDefinition(name = "Maximum pumping head (Förderhöhe)", description = "Maximum pumping head in meters. "
             + "If this value is wrong, actual pressure to setpoint will be wrong by the same factor. This can be used to calculate the correct maximum pumping head. "
             + "E.g. if the setpoint is 100 mBar but the actual pressure is 200 mBar, then the actual maximum pumping head is twice the entered value.")
    int maxPumpingHead() default 20;

    @AttributeDefinition(name = "Setpoint interval minimum", description = "The setpoint is limited to this interval. Unit is the same as setpoint.")
    double hIntervalMin() default 0;

    @AttributeDefinition(name = "Setpoint interval maximum", description = "The setpoint is limited to this interval. Unit is the same as setpoint.")
    double hIntervalMax() default 20;

    @AttributeDefinition(name = "Pumping head (Förderhöhe) setpoint", description = "Setpoint for the pumping head in meters. Equivalent to pressure. 1 m = 100 mBar.")
    double setpoint() default 2;

    @AttributeDefinition(name = "Stop the pump", description = "Stops the pump")
    boolean stopPump() default false;

    @AttributeDefinition(name = "Write pump status to log", description = "Write pump status parameters in the log.")
    boolean printPumpStatus() default false;

    /*
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
    */

    String webconsole_configurationFactory_nameHint() default "Controller Pump Grundfos [{id}]";
}