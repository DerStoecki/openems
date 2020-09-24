package io.openems.edge.bridge.genibus.api.task;


import io.openems.edge.bridge.genibus.api.PumpDevice;
import io.openems.edge.common.taskmanager.ManagedTask;

public interface GenibusTask extends ManagedTask {


    default int getRequest(int byteCounter) {
        return -1;
    };

    void setResponse(byte data);

    byte getAddress();

    int getHeader();

    void setOneByteInformation(int vi, int bo, int sif);

    void setFourByteInformation(int vi, int bo, int sif, byte unitIndex, byte scaleFactorZeroOrHigh, byte scaleFactorRangeOrLow);

    //boolean wasAdded();

    boolean InformationDataAvailable();

    void setPumpDevice(PumpDevice pumpDevice);

    int getDataByteSize();

    void setApduIdentifier(int identifier);

    int getApduIdentifier();

}
