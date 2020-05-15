package io.openems.edge.bridge.rest.communcation.task;

public interface RestRequest {
    //Return DeviceId + Channel
    String getRequest();

    String getAutoAdaptRequest();

    String getMasterId();

    String getSlaveId();

    String getDeviceId();

    String getRealDeviceId();

    boolean isMaster();

    String getDeviceType();

    boolean isAutoAdapt();

    boolean setAutoAdaptResponse(boolean succ, String answer);

    boolean isInverseSet();

}
