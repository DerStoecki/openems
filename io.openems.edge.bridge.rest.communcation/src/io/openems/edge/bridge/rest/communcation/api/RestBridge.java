package io.openems.edge.bridge.rest.communcation.api;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import org.osgi.service.cm.ConfigurationException;

import java.util.List;
import java.util.Map;

public interface RestBridge {

    void addRestRequest(String id, RestRequest request) throws ConfigurationException, OpenemsError.OpenemsNamedException;

    void removeRestRemoteDevice(String deviceId);

    RestRequest getRemoteRequest(String id);
    Map<String, RestRequest> getAllRequests();
}
