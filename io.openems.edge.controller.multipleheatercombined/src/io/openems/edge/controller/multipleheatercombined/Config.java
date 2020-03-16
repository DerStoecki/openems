package io.openems.edge.controller.multipleheatercombined;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "Controller Consolinno Passing",
        description = "This Controller regulates the Pump and Valves for Heating."
)
@interface Config {

    String service_pid();

    @AttributeDefinition(name = "Controller Name", description = "Unique Name of the Controller.")
    String id() default "MultipleHeaterCombined0";

    @AttributeDefinition(name = "alias", description = "Human readable name of Controller.")
    String alias() default "3-In-1";


    @AttributeDefinition(name = "HeatMeter Id", description = "Unique Id of the HeatMeter")
    String heatMeter_Id() default "HeatMeterMBus0";

    @AttributeDefinition(name = "Communication via MBus?", description = "Is the Bus Communciating via Mbus")
    boolean communicating_mbus() default true;

    @AttributeDefinition(name = "Heating Device 1 Name", description = "Unique Id of the first Heating Device.")
    String chp_Id() default "Chp0";

    @AttributeDefinition(name = "Max Performance Heating Device 1", description = "Maximum Heat Power of the Heater Unit: kW.")
    int heater_1_max_performance() default 100;

    @AttributeDefinition(name = "Heating Device 2 Name", description = "Unique Id of the second Heating Device.")
    String woodChip_Id() default "WoodChipHeater0";

    @AttributeDefinition(name = "Max Performance Heating Device 2", description = "Maximum Heat Power of the Heater Unit: kW.")
    int heater_2_max_performance() default 50;


    @AttributeDefinition(name = "Heating Device 3 Name", description = "Unique Id of the third Heating Device.")
    String gasBoiler_Id() default "GasBoiler0";

    @AttributeDefinition(name = "Max Performance Heating Device 3", description = "Maximum Heat Power of the Heater Unit: kW.")
    int heater_3_max_performance() default 30;



    @AttributeDefinition(name = "Heating Device 1 MAX Temperature in dC", description = "Threshold of the Heating Device 1 should be turned OFF(in dC --> 1°C == 10°dC).")
    int chp_Temperature_max() default 800;
    @AttributeDefinition(name = "Heating Device 1 MIN Temperature in dC", description = "Threshold of the Heating Device 1 should be turned ON(in dC --> 1°C == 10°dC).")
    int chp_Temperature_min() default 600;

    @AttributeDefinition(name = "HeatingDevice 1 TemperatureSensor MAX", description = "The Temperature-Sensor for the Heating Device 1 Temperature MAX.")
    String chp_TemperatureSensor_max() default "TemperatureSensor0";

    @AttributeDefinition(name = "HeatingDevice 1 TemperatureSensor MIN", description = "The Temperature-Sensor for the Heating Device 1 Temperature MIN.")
    String chp_TemperatureSensor_min() default "TemperatureSensor1";




    @AttributeDefinition(name = "Heating Device 2 MAX Temperature in dC", description = "Threshold of the Heating Device 2 should be turned OFF(in dC --> 1°C == 10°dC).")
    int woodChip_Temperature_max() default 800;
    @AttributeDefinition(name = "Heating Device 2 MIN Temperature in dC", description = "Threshold of the Heating Device 2 should be turned ON(in dC --> 1°C == 10°dC).")
    int woodChip_Temperature_min() default 600;

    @AttributeDefinition(name = "HeatingDevice 2 TemperatureSensor MAX", description = "The Temperature-Sensor for the Heating Device 2 Temperature MAX.")
    String woodChip_TemperatureSensor_max() default "TemperatureSensor2";
    @AttributeDefinition(name = "HeatingDevice 2 TemperatureSensor MIN", description = "The Temperature-Sensor for the Heating Device 2 Temperature MIN.")
    String woodChip_TemperatureSensor_min() default "TemperatureSensor3";



    @AttributeDefinition(name = "Heating Device 3 MAX Temperature in dC", description = "Threshold of the Heating Device 3 should be turned OFF(in dC --> 1°C == 10°dC).")
    int gasBoiler_Temperature_max() default 800;
    @AttributeDefinition(name = "Heating Device 3 MIN Temperature in dC", description = "Threshold of the Heating Device 3 should be turned ON(in dC --> 1°C == 10°dC).")
    int gasBoiler_Temperature_min() default 600;

    @AttributeDefinition(name = "HeatingDevice 3 TemperatureSensor MAX", description = "The Temperature-Sensor for the Heating Device 3 Temperature MAX.")
    String gasBoiler_TemperatureSensor_max() default "TemperatureSensor4";
    @AttributeDefinition(name = "HeatingDevice 3 TemperatureSensor MIN", description = "The Temperature-Sensor for the Heating Device 3 Temperature MIN.")
    String gasBoiler_TemperatureSensor_min() default "TemperatureSensor5";



    @AttributeDefinition(name = "BufferTemperatureMin in dC", description = "What's the minimum Buffer Temperature(1°C = 10dC).")
    int minTemperatureForBuffer() default 600;

    @AttributeDefinition(name = "BufferTemperatureMax in dC", description = "What's the maximum Buffer Temperature(1°C = 10dC).")
    int maxTemperatureForBuffer() default 800;


    @AttributeDefinition(name = "Buffer_Factor_Min_Temperature", description = "The Maximum Puffer Factor for extra Heating (Minimal Threshold).")
            float minTemperatureBufferValue() default 1.2f;

    @AttributeDefinition(name = "Buffer_Factor_InBetween", description = "The in Between Puffer Factor for extra Heating (In Between Max and Min Threshold).")
            float inBetweenBufferValue() default 1.1f;

    @AttributeDefinition(name = "Buffer_Factor_Max", description = "The minimum Puffer Factor, where no extra Heating is considered (Above Max Threshold).")
            float maxTemperatureBufferValue() default 1.0f;

    boolean enabled() default true;

    String webconsole_configurationFactory_nameHint() default "Controller Multiple Heater Combined [{id}]";

}