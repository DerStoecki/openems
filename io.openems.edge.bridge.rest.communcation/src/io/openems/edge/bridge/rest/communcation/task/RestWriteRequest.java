package io.openems.edge.bridge.rest.communcation.task;

public interface RestWriteRequest {

    String getPostMessage();

    void wasSuccess(Boolean succ, String response);

    boolean readToWrite();

    void setReadyToWrite(boolean ready);
}
