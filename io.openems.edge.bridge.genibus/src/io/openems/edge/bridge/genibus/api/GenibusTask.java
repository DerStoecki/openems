package io.openems.edge.bridge.genibus.api;



public interface GenibusTask {

    void setResponse(double data);

    int getAddress();

    boolean isWriteable();

    int getHeader();

}
