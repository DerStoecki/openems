package io.openems.edge.bridge.genibus.api;


public interface GenibusTask {


    byte getRequest();

    void setResponse(byte data);

    int getAddress();

    boolean isWriteable();

    int getHeader();

    void setInformationData(byte data);

    void setInformationData(byte[] data, int meaning);

    void setOneByteInformation(int vi, int bo, int sif);

    void setFourByteInformation(int vi, int bo, int sif, byte datum, byte datum1, byte datum2);

    boolean wasAdded();
}
