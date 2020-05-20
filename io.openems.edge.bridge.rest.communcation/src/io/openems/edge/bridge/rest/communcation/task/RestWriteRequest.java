package io.openems.edge.bridge.rest.communcation.task;

public interface RestWriteRequest extends RestRequest {

    String getPostMessage();

    void wasSuccess(boolean succ, String response);

    boolean readyToWrite();

    void nextValueSet();
    //allow Requests
    boolean setReadyToWrite(boolean ready);

    boolean valueHasChanged();

    boolean unitWasSet();

    void setUnit(boolean succ, String answer);
}
