package io.openems.edge.bridge.rest.communcation.task;

public interface RestRequest {
    //Return DeviceId + Channel
    String getRequest();

    String getAutoAdaptRequest();

    String getMasterId();

    String getSlaveId();

    String getDeviceId();

    boolean isMaster();

    String getDeviceType();

    boolean isAutoAdapt();

}
