package io.openems.edge.bridge.lucidcontrol.task;

public interface LucidControlBridgeTask {

    void setResponse(double voltageRead);

    String getDeviceId();

    String getModuleId();

    String getPath();

    int getPinPos();

}
