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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Rest",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class RestBridgeImpl extends AbstractOpenemsComponent implements RestBridge, OpenemsComponent, EventHandler {
    @Reference
    ComponentManager cpm;

    private RestBridgeCycleWorker worker = new RestBridgeCycleWorker();

    private Map<String, RestRequest> tasks = new ConcurrentHashMap<>();
    private String loginData;
    private String ipAddressAndPort;

    public RestBridgeImpl() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        if (config.enabled()) {
            this.loginData = "Basic " + Base64.getEncoder().encodeToString((config.username() + ":" + config.password()).getBytes());
            this.ipAddressAndPort = config.ipAddress() + ":" + config.port();
            this.worker.activate(super.id());
        }
    }


    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    /**
     * Adds the RestRequest to the tasks map.
     *
     * @param id      identifier == remote device Id usually from Remote Device config
     * @param request the RestRequest created by the Remote Device.
     * @throws ConfigurationException if the id is already in the Map.
     */
    @Override
    public void addRestRequest(String id, RestRequest request) throws ConfigurationException {

        if (this.tasks.containsKey(id)) {
            throw new ConfigurationException(id, "Already in RemoteTasks Check your UniqueId please.");
        } else {
            this.tasks.put(id, request);
        }

    }

    @Override
    public void removeRestRemoteDevice(String deviceId) {
        this.tasks.remove(deviceId);
    }

    @Override
    public RestRequest getRemoteRequest(String id) {
        return this.tasks.get(id);
    }

    @Override
    public Map<String, RestRequest> getAllRequests() {
        return this.tasks;
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
            tasks.forEach((key, entry) -> {

                try {
                    if (entry instanceof RestReadRequest) {
                        handleReadRequest((RestReadRequest) entry);

                    } else if (entry instanceof RestWriteRequest) {
                        ((RestWriteRequest) entry).nextValueSet();
                        //Important for Controllers --> if Ready To Write set True and give Value to channel
                        handlePostRequest((RestWriteRequest) entry);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    /**
     * handles PostRequests called by the CycleWorker.
     *
     * @param entry the RestWriteRequest given by the CycleWorker. from this.tasks
     *              <p>
     *              Creates URL and if ReadyToWrite (can be changed via Interface) && isAudoadapt --> AutoAdaptRequest.
     *              AutoAdaptRequests is only necessary if Device is a Relays. --> IsCloser will be asked.
     *              Bc Opener and Closer have Inverse Logic. A Closer is Normally Open and an Opener is NormallyClosed,
     *              Therefor Changes in Relays needs to be Adapted. "ON" means true with closer but false with opener and
     *              vice versa.
     *              </p>
     * @throws IOException Bc of URL and connection.
     */
    private void handlePostRequest(RestWriteRequest entry) throws IOException {
        URL url = new URL("http://" + this.ipAddressAndPort + "/rest/channel/" + entry.getRequest());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", this.loginData);

        if (entry.readyToWrite()) {
            if(!entry.unitWasSet()){
                handleUnitGet(entry, connection);
            }
            if (entry.isAutoAdapt()) {
                if (!autoAdaptSuccess(entry)) {
                    return;
                }
            }
            String msg = entry.getPostMessage();
            if (!entry.valueHasChanged() || msg.equals("NoValueDefined") || msg.equals("NotReadyToWrite")) {
                return;
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(msg.getBytes());
            os.flush();
            os.close();
            //Task can check if everythings ok --> good for Controller etc; ---> Check Channel
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                entry.wasSuccess(true, response.toString());
            } else {
                entry.wasSuccess(false, "POST NOT WORKED");
            }
        }
    }

    private void handleUnitGet(RestWriteRequest entry, HttpURLConnection connection) throws IOException {
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
            entry.setUnit(true, response.toString());
            //---------------------//
        } else {
            entry.setUnit(false, "ERROR WITH CONNECTION");
        }
    }

    /**
     * Gets a RestRequest and creates the GET Rest Method.
     *
     * @param entry entry the RestWriteRequest given by the CycleWorker. from this.tasks
     * @throws IOException bc of URL requests etc.
     *                     <p>
     *                     Gets a Request via Cycleworker. Creates the URL and reacts if HTTP_OK is true
     *                     If that's the case, the response will be set to entry.
     *                     </p>
     */
    private void handleReadRequest(RestReadRequest entry) throws IOException {
        URL url = new URL("http://" + this.ipAddressAndPort + "/rest/channel/" + entry.getRequest());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", this.loginData);
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
            entry.setResponse(true, response.toString());
            //---------------------//
        } else {
            entry.setResponse(false, "ERROR WITH CONNECTION");
        }
    }

    /**
     * Called if WriteRequest has autoAdapt set to true.
     *
     * @param entry current RestRequest entry.
     * @return true if autoAdapt was sucessfully set.
     * @throws IOException if URL Incorrect or ConnectionFails etc.
     *
     *                     <p>
     *                     AutoAdaptSucces is used by Remote devices who access a Relays; to get their IsCloser channel and see if
     *                     the device isACloser, if not --> inverse Logic
     *                     However this will only happens one time (one success time), after that always return true.
     *                     </p>
     */

    private boolean autoAdaptSuccess(RestRequest entry) throws IOException {
        if (entry.isInverseSet()) {
            return true;
        }
        URL url = new URL("http://" + this.ipAddressAndPort + "/rest/channel/" + entry.getAutoAdaptRequest());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", this.loginData);

        RestWriteRequest temp = (RestWriteRequest) entry;
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
            return temp.setAutoAdaptResponse(true, response.toString());
            //---------------------//
        } else {
            temp.setAutoAdaptResponse(false, "ERROR WITH CONNECTION");
            return false;
        }
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)) {
            this.worker.triggerNextRun();
        }

    }

}
