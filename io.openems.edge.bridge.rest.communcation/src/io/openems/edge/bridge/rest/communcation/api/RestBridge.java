package io.openems.edge.bridge.rest.communcation.api;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import org.osgi.service.cm.ConfigurationException;

import java.util.List;

public interface RestBridge {
    /**
     * Adds a Communicator AKA LeafletModule/With its name/ Sets the ip and says if it is Master Or Slave Device.
     *
     * @param id DeviceId usually from Config of calling class.
     * @param ip Ip of the registered device.
     * @param port port of the Device, (Configured in REST Controller ---> what Port is opened)
     * @param header encoded Authorization Header
     *
     * @throws ConfigurationException if DeviceId already in Map.
     *
     * */
    void addCommunicator(String id, String ip, String port, String header) throws ConfigurationException;

    void removeCommunicator(String id);

    void addRestRequest(String id, RestRequest request) throws ConfigurationException, OpenemsError.OpenemsNamedException;

    void removeRestRemoteDevice(String deviceId, String communicatorId);

    List<RestRequest> getRequests(String slaveMasterCommunicator);
}
