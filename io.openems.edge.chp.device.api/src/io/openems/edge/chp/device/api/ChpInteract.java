package io.openems.edge.chp.device.api;

public interface ChpInteract {
    //return true if write in channel was successful
    boolean setOnOff(int percentage);

    int getPowerValue();

    int getForward();

    int getRewind();

    //return electricalPower in kW
    float getElectricalPower();

    boolean isError();

    String getErrorMessage();

    boolean isWarnMessage();

    String getWarnMessage();

    boolean isReady();
}
