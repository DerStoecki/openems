package io.openems.edge.heatpump.device.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public enum UnitTable {
    //only pressure,temperature watt and rotations/time atm.
    //temperature, Watt, bar
    Standard_Unit_Table(
            new int[]{
                    //temperature
                    20, 21, 57, 84, 110, 111,
                    //powerActive
                    7, 8, 9, 44, 45,
                    //pressure
                    51,27,28,29,61,55,60
            },
            new String[]{
                    //temperature
                    "Celsius/10", "Celsius", "Fahrenheit", "Kelvin/100", "Kelvin/100", "Kelvin",
                    //powerActive
                    "Watt", "Watt*10", "Watt*100", "kW", "kW*10",
                    //pressure
                    "bar/1000", "bar/100", "bar/10", "bar", "kPa", "psi", "psi*10"
            });

    private Map<Integer, String> informationData = new HashMap<>();

    UnitTable(int[] keys, String[] values) {
        AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        Arrays.stream(keys).forEach(key -> {
            this.informationData.put(key, values[counter.get()]);
            counter.getAndIncrement();
        });

    }

    public Map<Integer, String> getInformationData() {
        return informationData;
    }
}

