package io.openems.edge.bridge.genibus.api;

import io.openems.edge.common.channel.Channel;

public interface Genibus {

    void addTask(String deviceId, GenibusTask task);

    void removeTask(String id);

    void addDevice(String id, int address);

    void removeDevice(String id);

    Channel<Integer> getConfigurationParameterChannel();


}
