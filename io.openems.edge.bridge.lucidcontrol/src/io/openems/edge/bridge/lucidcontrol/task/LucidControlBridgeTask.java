package io.openems.edge.bridge.lucidcontrol.task;

public interface LucidControlBridgeTask {

    int getRequest();

    void setResponse(double voltageRead);

}
