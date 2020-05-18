package io.openems.edge.bridge.rest.communcation.task;

public interface RestRequest {
    //Return DeviceId + Channel
    String getRequest();

    String getAutoAdaptRequest();

    String getDeviceId();

    String getRealDeviceId();

    String getDeviceType();

    boolean isAutoAdapt();

    boolean setAutoAdaptResponse(boolean succ, String answer);

    boolean isInverseSet();

}
