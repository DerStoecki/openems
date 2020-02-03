package io.openems.edge.bridge.genibus.api;

public interface Genibus {

    void addTask(String id, GenibusTask task);
    void removeTask(String id);

}
