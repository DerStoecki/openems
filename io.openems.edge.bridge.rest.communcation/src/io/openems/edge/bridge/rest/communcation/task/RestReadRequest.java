package io.openems.edge.bridge.rest.communcation.task;

public interface RestReadRequest extends RestRequest {
    void setResponse(boolean succ, String answer);
}
