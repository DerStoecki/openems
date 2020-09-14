package io.openems.edge.controller.emvtest;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
        name = "Controller Consolinno EMVtest",
        description = "This Controller cycles the relays, the analogue output and the PWM for testing."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Passing Controller.")
    String id() default "ControllerEMVtest0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Controller.")
    String alias() default "EMVtest";

    @AttributeDefinition(name = "RelaysId", description = "Select RelaysDevices",
            options = {
                    @Option(label = "Relays0", value = "Relays0"),
                    @Option(label = "Relays1", value = "Relays1"),
                    @Option(label = "Relays2", value = "Relays2"),
                    @Option(label = "Relays3", value = "Relays3"),
                    @Option(label = "Relays4", value = "Relays4"),
                    @Option(label = "Relays5", value = "Relays5"),
                    @Option(label = "Relays6", value = "Relays6"),
                    @Option(label = "Relays7", value = "Relays7"),

            })
    String[] relaysDeviceList();

    @AttributeDefinition(name = "CycleRelays", description = "Turn all relays on and off one after the other, continuously.")
    boolean cycle_relays() default true;

    @AttributeDefinition(name = "CycleRelaysOnTime", description = "Time each relay stays activated, in s.")
    int relays_cycle_on_time() default 10;

    @AttributeDefinition(name = "CycleRelaysBreakTime", description = "Time of break between relay activation, in s.")
    int relays_cycle_break_time() default 0;

    @AttributeDefinition(name = "DacDeviceId", description = "Select DacDevices",
            options = {
                    @Option(label = "DacDevice0", value = "DacDevice0"),
                    @Option(label = "DacDevice1", value = "DacDevice1"),
                    @Option(label = "DacDevice2", value = "DacDevice2"),
                    @Option(label = "DacDevice3", value = "DacDevice3")
            })
    String[] DacDeviceList();

    @AttributeDefinition(name = "RampDAC", description = "Ramp the DACs up and down.")
    boolean ramp_dac() default true;

    @AttributeDefinition(name = "RampDacStepTime", description = "Time between each step of the DAC ramp, in s.")
    int dac_ramp_step_time() default 10;

    @AttributeDefinition(name = "RampDacStep", description = "Increment/Decrement of the ramp per step, in percent of maximum power (type 4 for 4%).")
    int dac_ramp_step_value() default 4;

    @AttributeDefinition(name = "RampDacMax", description = "Peak current of the DAC ramp, in percent of maximum power (type 20 for 20%). Maximum value 100.")
    int dac_ramp_max_value() default 20;

    @AttributeDefinition(name = "PwmDeviceId", description = "Select PwmDevices",
            options = {
                    @Option(label = "PwmDevice0", value = "PwmDevice0"),
                    @Option(label = "PwmDevice1", value = "PwmDevice1"),
                    @Option(label = "PwmDevice2", value = "PwmDevice2"),
                    @Option(label = "PwmDevice3", value = "PwmDevice3"),
                    @Option(label = "PwmDevice4", value = "PwmDevice4"),
                    @Option(label = "PwmDevice5", value = "PwmDevice5"),
                    @Option(label = "PwmDevice6", value = "PwmDevice6"),
                    @Option(label = "PwmDevice7", value = "PwmDevice7")

            })
    String[] PwmDeviceList();

    @AttributeDefinition(name = "RampPWM", description = "Ramp the PWMs up and down, from 0% to 100%.")
    boolean ramp_pwm() default true;

    @AttributeDefinition(name = "RampPwmStepTime", description = "Time between each step of the PWM ramp, in s.")
    int pwm_ramp_step_time() default 10;

    @AttributeDefinition(name = "RampPwmStep", description = "Increment/Decrement of the ramp per step, in percentage of max (type 5 for 5%).")
    int pwm_ramp_step_value() default 5;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Consolinno EMVtest [{id}]";

}

