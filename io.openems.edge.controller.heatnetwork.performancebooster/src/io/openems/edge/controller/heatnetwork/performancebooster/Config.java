package io.openems.edge.controller.heatnetwork.performancebooster;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Heatnetwork PerformanceBooster",
        description = "A Controller controlling a heat mixer depending on certain Heaters."
)
@interface Config {
    String service_pid();

    @AttributeDefinition(name = "HeatnetworkPerformanceBooster-ID", description = "ID of Heatnetwork PerformanceBooster.")
    String id() default "HeatNetworkBooster0";

    @AttributeDefinition(name = "Alias", description = "Human readable Name.")
    String alias() default "GLT-Booster-Controller";

    @AttributeDefinition(name = "MinTemperatureSetPoint", description = "Min Temperature --> min threshold in dC! for Buffer")
    int minTemp() default 400;

    @AttributeDefinition(name = "MaxTemperatureSetPoint", description = "Max Temperature --> max threshold in dC! for Buffer")
    int maxTemp() default 500;

    @AttributeDefinition(name = "Heat Mixer SetPoint", description = "Percentage Value of Valve when Controller activates.")
    int valvePercent() default 48;

    @AttributeDefinition(name = "Heat Mixer addition", description = "Percentage increase if Error occurred in primary Heater ")
    int valvePercentAdditional() default 20;

    @AttributeDefinition(name = "Heater Backup Performance", description = "If the Temperature drops, the backup heater will be set to this %.")
    int backUpPercent() default 30;

    @AttributeDefinition(name = "Percent Increase Performance", description = "Additional Percentage Increase if Error occurred in Primary Heater.")
    int backUpPercentAdditional() default 20;

    @AttributeDefinition(name = "TemperatureSensors", description = "Temperaturesensors to overlook the Temperature.")
    String[] thermometer() default {"TemperatureSensor0", "TemperatureSensor1"};

    @AttributeDefinition(name = "Reference Thermometer", description = "Reference Thermometer to be a start/endpoint for activation")
    String referenceThermometer() default "TemperatureSensor7";

    @AttributeDefinition(name = "Temperature Reference SetPoint", description = "Temperature for Activation")
    int activationTemp() default 450;
    @AttributeDefinition(name = "Heatnetwork TemperatureSensor Primary Forward", description = "Temperature Sensor at the Heat mixer --> Primary Forward")
    String primaryForward() default "TemperatureSensor8";

    @AttributeDefinition(name = "Heatnetwork TemperatureSensor Primary Forward", description = "Temperature Sensor at the Heat mixer --> Primary Rewind")
    String primaryRewind() default "TemperatureSensor9";

    @AttributeDefinition(name = "Heatnetwork TemperatureSensor Primary Forward", description = "Temperature Sensor at the Heat mixer --> Secondary Forward")
    String secondaryForward() default "TemperatureSensor10";

    @AttributeDefinition(name = "Heatnetwork TemperatureSensor Primary Forward", description = "Temperature Sensor at the Heat mixer --> Secondary Rewind")
    String secondaryRewind() default "TemperatureSensor11";

    @AttributeDefinition(name = "ErrorInputHeater Type 2", description = "ErrorInputs via SignalSensorSpi for Heater Type 2 ( e.g. BiomassHeater).")
    String[] errorInputHeater1() default {"SignalSensorSpi3", "SignalSensorSpi4"};

    @AttributeDefinition(name = "ErrorInputHeater Type 1", description = "ErrorInputs via SignalSensorsSpi for Heater Type 1 ( e.g. GasBoiler)")
    String[] errorInputHeater2() default {"SignalSensorSpi0", "SignalSensorSpi1", "SignalSensorSpi2"};

    @AttributeDefinition(name = "Heat Mixer (== Valve)", description = "Temperature to be set when HeatingRequest is incoming: Unit is dC.")
    String valve() default "Valve0";

    @AttributeDefinition(name = "LucidControl Devices", description = "Position on LucidControl where Gasboiler are connected to.")
    String[] heaters() default {"LucidControlDeviceOutput0, LucidControlDeviceOutput1, LucidControlDeviceOutput2"};

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Heatnetwork PerformanceBooster [{id}]";
}