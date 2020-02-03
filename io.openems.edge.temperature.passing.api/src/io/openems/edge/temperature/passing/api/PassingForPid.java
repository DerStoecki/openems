package io.openems.edge.temperature.passing.api;

public interface PassingForPid extends PassingChannel {

    boolean readyToChange();

    boolean changeByPercentage(double percentage);

}
