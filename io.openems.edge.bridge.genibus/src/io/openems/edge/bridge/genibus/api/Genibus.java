package io.openems.edge.bridge.genibus.api;

public interface Genibus {

    void addTask(String deviceId, GenibusTask task);

    void removeTask(String id);

    void addDevice(String id, int address);

    void removeDevice(String id);

}
