package io.openems.edge.bridge.lmnwired.api;

public class DataRequest {
    protected byte serialNumber[];
    protected String lmnBridge;

    public DataRequest(String lmnBridge, byte serialNumber[]) {
        this.lmnBridge = lmnBridge;
        this.serialNumber = serialNumber;
    }
}
