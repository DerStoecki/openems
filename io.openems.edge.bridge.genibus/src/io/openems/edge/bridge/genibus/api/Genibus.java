package io.openems.edge.bridge.genibus.api;

public interface Genibus {

    void addTask(String deviceId, int ListPosition, GenibusTask task);

    void removeTask(String id);

    void addDevice(String id, int address);

}
