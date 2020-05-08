package io.openems.edge.bridge.rest.communcation.api;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import org.osgi.service.cm.ConfigurationException;

public interface RestBridge {

    void addCommunicator(String id, String ip, String port, String header) throws ConfigurationException;

    void removeCommunicator(String id);

    void addRestRequest(String id, RestRequest request, String identifier) throws ConfigurationException, OpenemsError.OpenemsNamedException;

    void removeRestRemoteDevice(String id);
}
