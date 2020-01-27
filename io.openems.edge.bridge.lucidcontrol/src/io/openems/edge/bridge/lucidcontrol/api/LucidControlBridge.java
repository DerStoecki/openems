package io.openems.edge.bridge.lucidcontrol.api;

public interface LucidControlBridge {

    void addAddress(String id, String path);

    void addVoltage(String id, int voltage);

    void removeModule(String id);

    void removeTask(String id);

    String getDeviceId();

    String getModuleId();

}
