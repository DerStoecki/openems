package io.openems.edge.bridge.genibus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    //PumpDevice and their Address
    private Map<String, Integer> devices = new HashMap<>();
    //PumpDevice and their Genibustasks
    private Map<String, Map<Integer, List<GenibusTask>>> tasks = new ConcurrentHashMap<>();



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


        //foreach pumpDevice --> For each Data class --> Add to Request Protocol for Apdu --> set Response
        @Override
        protected void forever() {

            if (!handler.checkStatus()) {
                //try to reconnect
                handler.start(portName);
                return;
            }
            tasks.keySet().forEach(pumpDevice -> {
                Telegram telegram = new Telegram();
                telegram.setStartDelimiterDataRequest();

                //get the Address via Id
                telegram.setDestinationAddress(devices.get(pumpDevice));
                telegram.setSourceAddress(0x01);

                ApplicationProgramDataUnit apduMeasuredDataInfo = new ApplicationProgramDataUnit();
                apduMeasuredDataInfo.setHeadClassMeasuredData();
                apduMeasuredDataInfo.setHeadOSACK(3);

                ApplicationProgramDataUnit apduMeasuredData = new ApplicationProgramDataUnit();
                apduMeasuredData.setHeadClassMeasuredData();
                apduMeasuredData.setHeadOSACK(getApduMeasuredData().getNextValue().get());


                ApplicationProgramDataUnit apduCommandsInfo = new ApplicationProgramDataUnit();
                apduCommandsInfo.setHeadClassCommands();
                apduCommandsInfo.setHeadOSACK(3);

                ApplicationProgramDataUnit apduCommands = new ApplicationProgramDataUnit();
                apduCommands.setHeadClassCommands();
                apduCommands.setHeadOSACK(getApduMeasuredData().getNextValue().get());

                ApplicationProgramDataUnit apduConfigurationParametersInfo = new ApplicationProgramDataUnit();
                apduConfigurationParametersInfo.setHeadClassConfigurationParameters();
                apduConfigurationParametersInfo.setHeadOSACK(3);

                ApplicationProgramDataUnit apduConfigurationParameters = new ApplicationProgramDataUnit();
                apduConfigurationParameters.setHeadClassConfigurationParameters();
                apduConfigurationParameters.setHeadOSACK(getApduConfigurationParameters().getNextValue().get());

                ApplicationProgramDataUnit apduReferenceValuesInfo = new ApplicationProgramDataUnit();
                apduReferenceValuesInfo.setHeadClassReferenceValues();
                apduReferenceValuesInfo.setHeadOSACK(3);

                ApplicationProgramDataUnit apduReferenceValues = new ApplicationProgramDataUnit();
                apduReferenceValues.setHeadClassReferenceValues();
                apduReferenceValues.setHeadOSACK(getApduReferenceValues().getNextValue().get());

                ApplicationProgramDataUnit apduAsciiStrings = new ApplicationProgramDataUnit();
                apduAsciiStrings.setHeadClassASCIIStrings();
                apduAsciiStrings.setHeadOSACK(getAsciiStrings().getNextValue().get());


                tasks.values().forEach(value -> value.keySet().forEach(key -> {
                    switch (key) {
                        case 2:
                            addData(apduMeasuredDataInfo, value.get(key), telegram);
                            addData(apduMeasuredData, value.get(key), telegram);
                            break;
                        case 3:
                            addData(apduCommandsInfo, value.get(key), telegram);
                            addData(apduCommands, value.get(key), telegram);
                            break;
                        case 4:
                            addData(apduConfigurationParametersInfo, value.get(key), telegram);
                            addData(apduConfigurationParameters, value.get(key), telegram);
                            break;
                        case 5:
                            addData(apduReferenceValuesInfo, value.get(key), telegram);
                            addData(apduReferenceValues, value.get(key), telegram);
                            break;
                        case 7:
                            addData(apduAsciiStrings, value.get(key), telegram);
                            break;
                    }

                }));
                handleTelegram(pumpDevice, telegram);
            });

        }
    }

    private void addData(ApplicationProgramDataUnit apdu, List<GenibusTask> genibusTasks, Telegram telegram) {
        /*
         * TODO if apdu AOSACK is 2 --> getRequest return byte if return is null --> no add --> was added False
         *  TODO --> bool wasAdded
         * */


        genibusTasks.forEach(value -> {
            //InformationAvailable --> Information data is available so the task can calc the byte data as a response
            if (apdu.getHeadOSACKShifted() == 2 && value.InformationDataAvailable()) {
                byte valueRequest = value.getRequest();
                if (valueRequest >= 0) {
                    apdu.putDataField(value.getAddress());
                    apdu.putDataField(valueRequest);
                }
            } else if (apdu.getHeadOSACKShifted() == 3 && !value.InformationDataAvailable()) {
                apdu.putDataField(value.getAddress());
            } else {
                apdu.putDataField(value.getAddress());
            }
        });
        telegram.getProtocolDataUnit().putAPDU(apdu);

    }

    private void handleTelegram(String pumpDevice, Telegram telegram) {
        //check OSACK --> infomration, request, data
        List<ApplicationProgramDataUnit> requestApdu = telegram.getProtocolDataUnit().getApplicationProgramDataUnitList();
        List<ApplicationProgramDataUnit> responseApdu = handler.writeTelegram(400, telegram).getProtocolDataUnit().getApplicationProgramDataUnitList();
        AtomicInteger listCounter = new AtomicInteger();
        responseApdu.forEach(apdu -> {
            byte[] data = apdu.getBytes();
            //for the GenibusTask list --> index
            int taskCounter = 0;
            // always on [0]
            int headClass = data[0];
            // on correct position get the header.
            int osAck = requestApdu.get(listCounter.get()).getHeadOSACKShifted();
            for (int byteCounter = 2; byteCounter < data.length; ) {
                /* TODO responseApdu.get(listCounter).getHeadOSACKShifted(); for further information
                 */
                //if info is already available current task is wrong
                if (osAck == 3 && !tasks.get(pumpDevice).get(headClass).get(taskCounter).InformationDataAvailable()) {
                    //vi bit 4
                    int vi = (data[byteCounter] & 0x10);
                    //bo bit 5
                    int bo = (data[byteCounter] & 0x20);
                    int sif = (data[byteCounter] & 0x03);
                    //only 1 byte of data
                    if (sif == 0 || sif == 1) {
                        tasks.get(pumpDevice).get(headClass).get(taskCounter).setOneByteInformation(vi, bo, sif);
                        byteCounter++;
                        //only 4byte data
                    } else {
                        tasks.get(pumpDevice).get(headClass).get(taskCounter).setFourByteInformation(vi, bo, sif,
                                data[byteCounter + 1], data[byteCounter + 2], data[byteCounter + 3]);
                        //bc of 4 byte data additional 3 byte incr.
                        byteCounter += 4;
                    }
                } else if (osAck != 2) {
                    //TODO Check if its only 1 byte data --> later
                    tasks.get(pumpDevice).get(headClass).get(taskCounter).setResponse(data[byteCounter]);
                    byteCounter++;
                }
                taskCounter++;

            }
            listCounter.getAndIncrement();
        });

    }


    @Override
    public void handleEvent(Event event) {
        switch (event.getTopic()) {
            case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
                this.worker.triggerNextRun();
                break;
        }
    }

    public void addTask(String deviceId, GenibusTask task) {
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
            list.add(task);
            Map<Integer, List<GenibusTask>> map = new HashMap<>();
            map.put(task.getHeader(), list);
            this.tasks.put(deviceId, map);
        }
    }

    public void removeTask(String sourceId) {
        this.tasks.remove(sourceId);
    }

    @Override
    public void addDevice(String id, int address) {
        this.devices.put(id, address);
    }

    @Override
    public void removeDevice(String id) {
        this.tasks.remove(id);
        this.devices.remove(id);
    }

}
