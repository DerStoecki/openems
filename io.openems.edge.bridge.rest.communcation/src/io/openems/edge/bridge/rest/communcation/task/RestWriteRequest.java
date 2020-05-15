package io.openems.edge.bridge.rest.communcation.task;

public interface RestWriteRequest extends RestRequest {

    String getPostMessage();

    void wasSuccess(Boolean succ, String response);

    boolean readyToWrite();

    void nextValueSet();
    //allow Requests
    boolean setReadyToWrite(boolean ready);

    boolean valueHasChanged();
}
