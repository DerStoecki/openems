package io.openems.edge.bridge.lucidcontrol.api;

import io.openems.edge.bridge.lucidcontrol.task.LucidControlBridgeTask;

public interface LucidControlBridge {

    void addPath(String id, String path);

    void addVoltage(String id, String voltage);

    void removeModule(String id);

    void removeTask(String id);

    void addLucidControlTask(String id, LucidControlBridgeTask lucid);

    String getPath(String moduleId);

    String getVoltage(String moduleId);


}
