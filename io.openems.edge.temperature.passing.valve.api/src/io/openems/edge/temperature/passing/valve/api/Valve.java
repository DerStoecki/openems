package io.openems.edge.temperature.passing.valve.api;

public interface Valve {
    void valveClose();

    void valveOpen();

    void controlRelais(boolean activate, String whichRelais);

    boolean readyToChangeValve();

    void calculatePercentageState();

    boolean changeValvePositionByPercentage(double percentage);

}
