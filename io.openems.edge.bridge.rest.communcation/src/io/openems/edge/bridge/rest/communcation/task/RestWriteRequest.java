package io.openems.edge.bridge.rest.communcation.task;

public interface RestWriteRequest extends RestRequest {

    String getPostMessage();

    void wasSuccess(Boolean succ, String response);

    boolean readToWrite();

    void setReadyToWrite(boolean ready);
}
