package io.openems.edge.bridge.rest.communcation;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.rest.communcation.api.RestBridge;
import io.openems.edge.bridge.rest.communcation.task.RestReadRequest;
import io.openems.edge.bridge.rest.communcation.task.RestRequest;
import io.openems.edge.bridge.rest.communcation.task.RestWriteRequest;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Rest",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class RestBridgeImpl extends AbstractOpenemsComponent implements RestBridge, OpenemsComponent, EventHandler {
    @Reference
    ComponentManager cpm;

    //authorization
    private Map<String, String> deviceIdHeader = new ConcurrentHashMap<>();
    //Ip+Port as String
    private Map<String, String> deviceIdIpAndPort = new ConcurrentHashMap<>();

    //can handle Read and Write from one Device
    private Map<String, List<RestRequest>> tasks = new ConcurrentHashMap<>();

    private RestBridgeCycleWorker worker = new RestBridgeCycleWorker();

    public RestBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            this.worker.activate(super.id());
        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
        this.deviceIdIpAndPort.keySet().forEach(this::removeCommunicator);
    }

    @Override
    public void addCommunicator(String id, String ip, String port, String header) throws ConfigurationException {
        if (!this.deviceIdHeader.containsKey(id) && !this.deviceIdIpAndPort.containsKey(id)) {
            this.deviceIdHeader.put(id, "Basic " + header);
            this.deviceIdIpAndPort.put(id, ip + ":" + port);
        } else {
            throw new ConfigurationException(id, "Already in Device List, Please Change the Unique Id of " + id);
        }
    }

    @Override
    public void removeCommunicator(String id) {

        this.deviceIdIpAndPort.remove(id);
        this.tasks.remove(id);
        this.deviceIdHeader.remove(id);
    }

    @Override
    public void addRestRequest(String id, RestRequest request) {

        if (!this.tasks.containsKey(id)) {
            List<RestRequest> tempRequest = new ArrayList<>();
            tempRequest.add(request);
            this.tasks.put(id, tempRequest);
        } else {
            this.tasks.get(id).add(request);
        }

    }

    //Important! Not a Single Request will be removed but a Whole device ---> Can contain multiple tasks & requests
    @Override
    public void removeRestRemoteDevice(String deviceId, String communicatorId) {
        AtomicInteger index = new AtomicInteger();
        if (this.tasks.get(communicatorId).stream().anyMatch(request -> {
            if (request.getDeviceId().equals(deviceId)) {
                index.set(this.tasks.get(communicatorId).indexOf(request));
                return true;
            }
            return false;
        })) {
            this.tasks.get(communicatorId).remove(index.intValue());
        }


    }

    @Override
    public List<RestRequest> getRequests(String slaveMasterCommunicator) {
        return this.tasks.get(slaveMasterCommunicator);
    }


    private class RestBridgeCycleWorker extends AbstractCycleWorker {

        @Override
        public void activate(String name) {
            super.activate(name);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }

        @Override
        public void forever() throws Throwable {
            tasks.forEach((key, value) -> {
                value.forEach(entry -> {
                    String header;
                    String ipAddress;

                    if (entry.isMaster()) {
                        header = deviceIdHeader.get(entry.getMasterId());
                        ipAddress = deviceIdIpAndPort.get(entry.getMasterId());
                    } else {
                        header = deviceIdHeader.get(entry.getSlaveId());
                        ipAddress = deviceIdIpAndPort.get(entry.getSlaveId());
                    }
                    try {
                        URL url = new URL("http://" + ipAddress + "/rest/channel/" + entry.getRequest());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Authorization", header);

                        if (entry instanceof RestReadRequest) {
                            RestReadRequest temp = (RestReadRequest) entry;
                            connection.setRequestMethod("GET");
                            int responseCode = connection.getResponseCode();

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                String readLine;
                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(connection.getInputStream()));

                                StringBuilder response = new StringBuilder();
                                while ((readLine = in.readLine()) != null) {
                                    response.append(readLine);
                                }
                                in.close();
                                //---------------------//
                                temp.setResponse(true, response.toString());
                                //---------------------//
                            } else {
                                temp.setResponse(false, "ERROR WITH CONNECTION");
                            }
                        } else if (entry instanceof RestWriteRequest) {
                            RestWriteRequest tempEntry = ((RestWriteRequest) entry);
                            //Important for Controllers --> if Ready To Write set True and give Value to channel
                            if (tempEntry.readToWrite()) {
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setDoOutput(true);
                                OutputStream os = connection.getOutputStream();
                                os.write(tempEntry.getPostMessage().getBytes());
                                os.flush();
                                os.close();
                                //Task can check if everythings ok --> good for Controller etc; ---> Check Channel
                                int responseCode = connection.getResponseCode();

                                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                            connection.getInputStream()));
                                    String inputLine;
                                    StringBuilder response = new StringBuilder();
                                    while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                    }
                                    in.close();
                                    tempEntry.wasSuccess(true, response.toString());
                                } else {
                                    tempEntry.wasSuccess(false, "POST NOT WORKED");
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                });

            });

        }

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.worker.triggerNextRun();
        }

    }

}
