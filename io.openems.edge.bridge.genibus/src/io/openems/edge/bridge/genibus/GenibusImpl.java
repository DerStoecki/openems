package io.openems.edge.bridge.genibus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.genibus.api.*;
import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;
import io.openems.edge.common.channel.Channel;
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
    private boolean debug;

    private final Worker worker = new Worker();

    private String portName;
    boolean connectionOk;

    private Handler handler;
    //PumpDevice and their Address
    private Map<String, Integer> devices = new HashMap<>();
    //PumpDevice and their Genibustasks
    private Map<String, Map<Integer, List<GenibusTask>>> tasks = new ConcurrentHashMap<>();


    public GenibusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GenibusChannel.ChannelId.values());
        handler = new Handler(this);
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        debug = config.debug();
        applyDefaultApduHeaderOperation();
        portName = config.portName();
        connectionOk = handler.start(portName);
        this.worker.activate(config.id());

        //default


    }

    private void applyDefaultApduHeaderOperation() {
        getApduConfigurationParameters().setNextValue(2);
        getApduMeasuredData().setNextValue(0);
        getApduReferenceValues().setNextValue(2);
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

        /**
         * for each pump Device registered in the task list.
         * Create a Telegram. this will be filled with apdu.
         * Depending on the added tasks, they will be put together. Information Values will be gathered
         * as well as values.
         * APDUs for each case will be created and put to the telegram if information/tasks are available.
         * Disclaimer: ATM only 6 APDU Frames are possible to read
         */
        @Override
        protected void forever() {

            // Check connection.
            if (!handler.checkStatus()) {
                // If checkStatus() returns false, the connection is lost. Try to reconnect
                connectionOk = handler.start(portName);
            }

            if (connectionOk) {
                getTasks().keySet().forEach(pumpDevice -> {

                    Telegram telegram = new Telegram();
                    telegram.setStartDelimiterDataRequest();
                    //get the Address via Id
                    telegram.setDestinationAddress(devices.get(pumpDevice));
                    telegram.setSourceAddress(0x01);

                    ApplicationProgramDataUnit apduMeasuredDataInfo = new ApplicationProgramDataUnit();
                    apduMeasuredDataInfo.setHeadClassMeasuredData();
                    apduMeasuredDataInfo.setOSInfo();

                    ApplicationProgramDataUnit apduMeasuredData = new ApplicationProgramDataUnit();
                    apduMeasuredData.setHeadClassMeasuredData();
                    apduMeasuredData.setHeadOSACK(getApduMeasuredData().getNextValue().get());

                    ApplicationProgramDataUnit apduCommands = new ApplicationProgramDataUnit();
                    apduCommands.setHeadClassCommands();
                    apduCommands.setOSSet();

                    ApplicationProgramDataUnit apduConfigurationParametersInfo = new ApplicationProgramDataUnit();
                    apduConfigurationParametersInfo.setHeadClassConfigurationParameters();
                    apduConfigurationParametersInfo.setOSInfo();

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

                    getTasks().get(pumpDevice).forEach((key, value) -> {
                        //ATTENTION! ATM only 6 Apdu frames are possible to read --> current implementation --> case 2, 3, 4
                        //and 5 are only needed.
                        switch (key) {
                            case 2:
                                addData(apduMeasuredDataInfo, value, telegram);
                                addData(apduMeasuredData, value, telegram);
                                break;
                            case 3:
                                addData(apduCommands, value, telegram);
                                break;
                            case 4:
                                addData(apduConfigurationParametersInfo, value, telegram);
                                addData(apduConfigurationParameters, value, telegram);
                                break;
                            case 5:
                                //addData(apduReferenceValuesInfo, value, telegram);
                                addData(apduReferenceValues, value, telegram);
                                break;
                            case 7:
                                addData(apduAsciiStrings, value, telegram);
                                break;
                        }
                    });

                    handleTelegram(pumpDevice, telegram);
                });
            }


        }
    }

    /**
     * Adds the Data of the specific genibustasks, sorted by the HeadClass to the APDU and then to the Telegram.
     *
     * @param apdu         Given ApplicationProgramDataUnit by the run task.
     * @param genibusTasks genibusTasks of the heatpump.Only a part is given for specific APDU Frame.
     *                     Identified via HeatPump Id and HeadClass.
     * @param telegram     Telegram created beforehand, gets the filled apdu frame.
     */
    private void addData(ApplicationProgramDataUnit apdu, List<GenibusTask> genibusTasks, Telegram telegram) {

        genibusTasks.forEach(value -> {
            //--->commands
            if (apdu.getHeadClass() == 3) {
                if (value.getRequest() != 0) {
                    apdu.putDataField(value.getAddress());
                }
                //WRITE TASK
                //InformationAvailable --> Information data is available so the task can calc the byte data as a response
            } else if (apdu.getHeadOSACKforRequest() == 2) {

                int valueRequest = value.getRequest();
                if (valueRequest > -256) {
                    apdu.putDataField(value.getAddress());
                    apdu.putDataField((byte) valueRequest);
                }
                //INFORMATION DATA
                // either no information request OR info request w.o. information data  available
                // } else if (apdu.getHeadOSACKforRequest() != 3 || !value.InformationDataAvailable()) {
            } else {
                apdu.putDataField(value.getAddress());
            }
        });
        //at least 1 info was added
        if (apdu.getBytes().length >= 3) {
            telegram.getProtocolDataUnit().putAPDU(apdu);
        }

    }

    /**
     * <p>Handles the telegram. created by the forever() method.
     *
     * @param pumpDevice Unique Id of the pump Device.
     * @param telegram   telegram created beforehand.
     *                   </p>
     *
     *                   <p>The request apdu is needed to compare the head of the apdu frame. Since the response does
     *                   not carry any identification.
     *                   The Way the Request Telegram is send, the information/data will be returned.
     *                   Every Head needs a certain treatment, so every head needs to be checked of the request Apdu.
     *                   If you have a look at the if(osAck == 2) there are 2 possible cases since the information
     *                   data length is either 1 or 4 byte long. Depending on that, the task method
     *                   setOneByteInformation or setFourByteInformation is called.
     *                   </p>
     */
    private void handleTelegram(String pumpDevice, Telegram telegram) {
        //check OSACK --> infomration, request, data
        List<ApplicationProgramDataUnit> requestApdu = telegram.getProtocolDataUnit().getApplicationProgramDataUnitList();
        Telegram responseTelegram = handler.writeTelegram(400, telegram, debug);
        if (responseTelegram == null) {
            return;
        }
        List<ApplicationProgramDataUnit> responseApdu = responseTelegram.getProtocolDataUnit().getApplicationProgramDataUnitList();

        AtomicInteger listCounter = new AtomicInteger();
        listCounter.set(0);
        responseApdu.forEach(apdu -> {
            byte[] data = apdu.getBytes();
            //for the GenibusTask list --> index
            int taskCounter = 0;
            // always on [0]
            int headClass = data[0];
            // on correct position get the header.

            int osAck = requestApdu.get(listCounter.get()).getHeadOSACKforRequest();
            if (osAck == 2) {
                listCounter.getAndIncrement();

            } else {
                for (int byteCounter = 2; byteCounter < data.length; ) {
                    /* TODO responseApdu.get(listCounter).getHeadOSACKShifted(); for further information
                     */
                    GenibusTask geniTask = tasks.get(pumpDevice).get(headClass).get(taskCounter);
                    //if info is already available current task is wrong
                    if (osAck == 3) {
                        //if geniTask information available --> this data field is not for this task.
                        //if (!geniTask.InformationDataAvailable()) {
                        //vi bit 4
                        int vi = (data[byteCounter] & 0x10);
                        //bo bit 5
                        int bo = (data[byteCounter] & 0x20);
                        //sif on bit 0 and 1
                        int sif = (data[byteCounter] & 0x03);
                        //only 1 byte of data
                        if (sif == 0 || sif == 1) {
                            geniTask.setOneByteInformation(vi, bo, sif);
                            byteCounter++;
                            //only 4byte data
                        } else {
                            if (byteCounter >= data.length - 3) {
                                this.logWarn(this.log, "Incorrect Data Length to SIF-->prevented Out of Bounds Exception");
                                break;
                            }
                            geniTask.setFourByteInformation(vi, bo, sif,
                                    data[byteCounter + 1], data[byteCounter + 2], data[byteCounter + 3]);
                            //bc of 4 byte data additional 3 byte incr.
                            byteCounter += 4;
                        }
                        //}
                    } else {
                        //TODO Check if its only 1 byte data --> later.
                        geniTask.setResponse(data[byteCounter]);
                        byteCounter++;
                    }
                    taskCounter++;
                    if (tasks.get(pumpDevice).get(headClass).size() < taskCounter) {
                        break;
                    }
                }
                listCounter.getAndIncrement();
            }
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

    /**
     * Adds Genibustask to the tasks map. First Key is the Device id and second key is the Headclass of the task.
     * If either the device id is not in map or the headclass, the key will be put with new Map containing headclass and
     * a new List of Genibustasks.
     *
     * @param deviceId Unique Id of the Heatpump. Comes from config.
     * @param task     Task created by the Heatpump.
     */
    public void addTask(String deviceId, GenibusTask task) {
        if (this.tasks.containsKey(deviceId)) {
            if (this.tasks.get(deviceId).keySet().stream().anyMatch(header -> header.equals(task.getHeader()))) {
                this.tasks.get(deviceId).keySet().stream().filter(header -> header.equals(task.getHeader())).findFirst().ifPresent(
                        header -> this.tasks.get(deviceId).get(header).add(task));
            } else {
                List<GenibusTask> taskForNewHead = new ArrayList<>();
                taskForNewHead.add(task);
                this.tasks.get(deviceId).put(task.getHeader(), taskForNewHead);
            }
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

    /**
     * Adds a device in map devices, for address resolution.
     * @param id Unique id of HeatPump.
     * @param address address of HeatPump.
     */
    @Override
    public void addDevice(String id, int address) {
        this.devices.put(id, address);
    }

    @Override
    public void removeDevice(String id) {
        this.tasks.remove(id);
        this.devices.remove(id);
    }

    @Override
    public Channel<Integer> getConfigurationParameterChannel() {
        return getApduConfigurationParameters();
    }

    private Map<String, Map<Integer, List<GenibusTask>>> getTasks() {
        return tasks;
    }

    @Override
    public void logDebug(Logger log, String message) {
        super.logDebug(log, message);
    }

    @Override
    public void logInfo(Logger log, String message) {
        super.logInfo(log, message);
    }

    @Override
    public void logWarn(Logger log, String message) {
        super.logWarn(log, message);
    }

    @Override
    public void logError(Logger log, String message) {
        super.logError(log, message);
    }
}
