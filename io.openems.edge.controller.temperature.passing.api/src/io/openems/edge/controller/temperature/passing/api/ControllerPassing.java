package io.openems.edge.controller.temperature.passing.api;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.relais.api.RelaisActuator;

public interface ControllerPassing {

    boolean readyToChangeValve();

    void valveClose();

    void controlRelais(boolean activate, String whichRelais);

    RelaisActuator getValveClose();

    int getTimeValveNeedsToOpenAndClose();

    long getTimeStampValve();

    boolean isNoError();

    boolean isActive();

    WriteChannel<Boolean> onOrOffChannel();

    Channel<Integer> minTemperature();
}
