package io.openems.edge.bridge.genibus.api.task;


import io.openems.edge.bridge.genibus.api.PumpDevice;
import io.openems.edge.common.taskmanager.ManagedTask;

public interface GenibusTask extends ManagedTask {


    // The point of the boolean "write" is to tell the method if this is an actual write or just a test to see if there
    // is something to write. If it is a write, the write channel associated with the task will have it's nextWriteValue
    // reset to null. Any further calls of the method will then return "nothing to write" (-1 or -256), unless a
    // value was put in nextWriteValue of the channel again.
    default int getRequest(int byteCounter, boolean write) {
        return -1;
    };

    void setResponse(byte data);

    byte getAddress();

    int getHeader();

    void setOneByteInformation(int vi, int bo, int sif);

    void setFourByteInformation(int vi, int bo, int sif, byte unitIndex, byte scaleFactorZeroOrHigh, byte scaleFactorRangeOrLow);

    boolean InformationDataAvailable();

    void resetInfo();

    void setPumpDevice(PumpDevice pumpDevice);

    int getDataByteSize();

    void setApduIdentifier(int identifier);

    int getApduIdentifier();

    String printInfo();

}
