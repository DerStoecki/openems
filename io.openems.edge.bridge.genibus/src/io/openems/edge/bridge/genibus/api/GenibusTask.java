package io.openems.edge.bridge.genibus.api;


public interface GenibusTask {


    int getRequest();

    void setResponse(byte data);

    byte getAddress();


    int getHeader();

    void setOneByteInformation(int vi, int bo, int sif);

    void setFourByteInformation(int vi, int bo, int sif, byte unitIndex, byte scaleFactorZeroOrHigh, byte scaleFactorRangeOrLow);

    boolean wasAdded();

    boolean InformationDataAvailable();
}
