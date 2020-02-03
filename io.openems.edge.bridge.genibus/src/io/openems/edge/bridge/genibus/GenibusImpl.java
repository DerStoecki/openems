package io.openems.edge.bridge.genibus;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.genibus.api.Bridge;
import io.openems.edge.bridge.genibus.api.Device;
import io.openems.edge.bridge.genibus.api.Handler;
import io.openems.edge.bridge.genibus.api.GenibusTask;
import io.openems.edge.bridge.genibus.api.TaskDataRequest;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(name = "io.openems.edge.bridge.genibus", //
        immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
        })
public class GenibusImpl extends AbstractOpenemsComponent implements Bridge, OpenemsComponent, EventHandler {

    private final Map<String, GenibusTask> tasks = new HashMap<>();
    private final Map<Byte, Device> deviceList = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(GenibusImpl.class);

    private final Worker worker = new Worker();

    Config config;

    private Handler handler = new Handler();

    public GenibusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Bridge.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.worker.activate(config.id());

        this.config = config;

        handler.start(config.portName());
        log.debug("Component started");

        activateConnectionRequestTask();
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        worker.deactivate();
        handler.stop();
    }

    private class Worker extends AbstractCycleWorker {

        @Override
        public void activate(String name) {
            super.activate(name);
        }

        @Override
        public void deactivate() {
            super.deactivate();
        }

        @Override
        protected void forever() {

            if (!handler.checkStatus()) {
                //try to reconnect
                handler.start(config.portName());
                return;
            }

            for (GenibusTask task : tasks.values()) {
                task.getRequest();
            }

        }
    }

    public Map<Byte, Device> getDeviceList() {
        return deviceList;
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.getTopic()) {
            case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
                this.worker.triggerNextRun();
                break;
        }
    }

    public void addTask(String sourceId, GenibusTask task) {
        this.tasks.put(sourceId, task);
    }

    public void removeTask(String sourceId) {
        this.tasks.remove(sourceId);
    }

    protected void activateConnectionRequestTask() {
        // Live / repeated
//    	tasks.put("ConnectionRequestTask", new TaskConnectionRequest(handler, deviceList));
//    	tasks.put("DataRequest", new TaskDataRequest(handler, new Device(0x20)));
        // Test
//		log.info("Send Connection Request at start for device list");
//		Task connectionRequestTask = new TaskConnectionRequest(handler, deviceList);
//		connectionRequestTask.getRequest();
        GenibusTask dataTask = new TaskDataRequest(handler, new Device(0x20));
        dataTask.getRequest();

    }


}
