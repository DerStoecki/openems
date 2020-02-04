package io.openems.edge.bridge.genibus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.openems.edge.bridge.genibus.api.*;
import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;
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
public class GenibusImpl extends AbstractOpenemsComponent implements GenibusChannel, OpenemsComponent, EventHandler, Genibus {


    private final Logger log = LoggerFactory.getLogger(GenibusImpl.class);

    private final Worker worker = new Worker();

    private String portName;

    private Handler handler = new Handler();

    private Map<String, Integer> devices = new HashMap<>();
    private Map<String, Map<Integer, List<GenibusTask>>> tasks = new ConcurrentHashMap<>();
    private Map<Integer, Double> response = new ConcurrentHashMap<>();

    //TODO response

    public GenibusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GenibusChannel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.worker.activate(config.id());
        handler.start(config.portName());
        //default


        applyDefaultApduHeaderOperation();
    }

    private void applyDefaultApduHeaderOperation() {
        getApduCommands().setNextValue(2);
        getApduConfigurationParameters().setNextValue(0);
        getApduMeasuredData().setNextValue(0);
        getApduReferenceValues().setNextValue(0);
        getAsciiStrings().setNextValue(0);
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
                handler.start(portName);
                return;
            }
            tasks.keySet().forEach(task -> {
                Telegram telegram = new Telegram();
                telegram.setStartDelimiterDataRequest();

                //get the Address via Id
                telegram.setDestinationAddress(devices.get(task));
                telegram.setSourceAddress(0x01);
                ApplicationProgramDataUnit apduMeasuredData = new ApplicationProgramDataUnit();
                apduMeasuredData.setHeadClassMeasuredData();
                apduMeasuredData.setHeadOSACK(getApduMeasuredData().getNextValue().get());

                ApplicationProgramDataUnit apduCommands = new ApplicationProgramDataUnit();
                apduCommands.setHeadClassCommands();
                apduCommands.setHeadOSACK(getApduCommands().getNextValue().get());

                ApplicationProgramDataUnit apduConfigurationParameters = new ApplicationProgramDataUnit();
                apduConfigurationParameters.setHeadClassConfigurationParameters();
                apduConfigurationParameters.setHeadOSACK(getApduConfigurationParameters().getNextValue().get());

                ApplicationProgramDataUnit apduReferenceValues = new ApplicationProgramDataUnit();
                apduReferenceValues.setHeadClassReferenceValues();
                apduReferenceValues.setHeadOSACK(getApduReferenceValues().getNextValue().get());

                ApplicationProgramDataUnit apduAsciiStrings = new ApplicationProgramDataUnit();
                apduAsciiStrings.setHeadClassASCIIStrings();
                apduAsciiStrings.setHeadOSACK(getAsciiStrings().getNextValue().get());

                tasks.values().forEach(value -> value.keySet().forEach(key -> {
                    switch (key) {
                        case 2:
                            addData(apduMeasuredData, value.get(key), telegram);
                            break;
                        case 3:
                            addData(apduCommands, value.get(key), telegram);
                            break;
                        case 4:
                            addData(apduConfigurationParameters, value.get(key), telegram);
                            break;
                        case 5:
                            addData(apduReferenceValues, value.get(key), telegram);
                            break;
                        case 7:
                            addData(apduAsciiStrings, value.get(key), telegram);
                            break;
                    }

                }));



            });

        }
    }

    private void addData(ApplicationProgramDataUnit apdu, List<GenibusTask> genibusTasks, Telegram telegram) {
        genibusTasks.forEach(value -> {
            apdu.putDataField(value.getAddress());
        });
        telegram.getProtocolDataUnit().putAPDU(apdu);

        handleTelegram(telegram, apdu.getHead());
    }

    private void handleTelegram(Telegram telegram, short head) {

    }


    @Override
    public void handleEvent(Event event) {
        switch (event.getTopic()) {
            case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
                this.worker.triggerNextRun();
                break;
        }
    }

    public void addTask(String deviceId, int listPosition, GenibusTask task) {
        if (this.tasks.containsKey(deviceId)) {
            this.tasks.values().forEach(headerTaskMap -> {
                if (headerTaskMap.containsKey(task.getHeader())) {
                    headerTaskMap.get(task.getHeader()).add(task);
                } else {
                    List<GenibusTask> genibusList = new ArrayList<>();
                    genibusList.add(task);
                    headerTaskMap.put(task.getHeader(), genibusList);
                }

            });
        } else {
            List<GenibusTask> list = new ArrayList<>();
            list.add(listPosition, task);
            Map<Integer, List<GenibusTask>> map = new HashMap<>();
            map.put(task.getHeader(), list);
            this.tasks.put(deviceId, map);
        }
    }

    public void removeTask(String sourceId) {
        this.tasks.remove(sourceId);
        this.devices.remove(sourceId);
    }

    @Override
    public void addDevice(String id, int address) {
        this.devices.put(id, address);
    }

    //    protected void activateConnectionRequestTask() {
    //        // Live / repeated
    //        //    	tasks.put("ConnectionRequestTask", new TaskConnectionRequest(handler, deviceList));
    //        //    	tasks.put("DataRequest", new TaskDataRequest(handler, new Device(0x20)));
    //        // Test
    //        //		log.info("Send Connection Request at start for device list");
    //        //		Task connectionRequestTask = new TaskConnectionRequest(handler, deviceList);
    //        //		connectionRequestTask.getRequest();
    //        GenibusTask dataTask = new TaskDataRequest(handler, new Device(0x20));
    //        dataTask.getRequest();
    //
    //    }


}
