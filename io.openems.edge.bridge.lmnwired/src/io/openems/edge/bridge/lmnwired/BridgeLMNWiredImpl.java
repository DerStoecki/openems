package io.openems.edge.bridge.lmnwired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.lmnwired.api.BridgeLMNWired;
import io.openems.edge.bridge.lmnwired.api.Device;
import io.openems.edge.bridge.lmnwired.api.task.LMNWiredTask;
import io.openems.edge.bridge.lmnwired.hdlc.PackageHandler;
import io.openems.edge.bridge.lmnwired.hdlc.HdlcDataRequest;
import io.openems.edge.bridge.lmnwired.hdlc.HdlcFrame;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(name = "io.openems.edge.bridge.lmnwired", //
        immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
        })

public class BridgeLMNWiredImpl extends AbstractOpenemsComponent
        implements BridgeLMNWired, OpenemsComponent, EventHandler {

    private SerialPort serialPort;

    private final Logger log = LoggerFactory.getLogger(BridgeLMNWiredImpl.class);

    private final LMNWiredWorker worker = new LMNWiredWorker();
    private final Map<String, LMNWiredTask> tasks = new HashMap<>();

    private PackageHandler packageHandler;

    private ByteArrayOutputStream concatData;

    int timeslotsTime;
    int timeslots;
    int timeSlotDurationInMs;
    int currentHDLCPackageLength;
    int crc;
    NumberFormat numberFormat = new DecimalFormat("0.0");
    Config config;

    boolean serialPortFound;

    @Reference
    protected ConfigurationAdmin cm;

    private boolean serialDataListenerActive = false;
    private boolean serialDataRequestActive = false;

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    public BridgeLMNWiredImpl() {
        super(//
                OpenemsComponent.ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        this.worker.activate(config.id());
        this.config = config;

        timeslotsTime = config.timeSlots() * 2 * config.timeSlotDurationInMs();
        timeslots = config.timeSlots();
        timeSlotDurationInMs = config.timeSlotDurationInMs();

        numberFormat.setRoundingMode(RoundingMode.DOWN);

        activateSerialPort();

        packageHandler = new PackageHandler(serialPort, config.timeSlots(), timeslotsTime, this);
    }

    public void activateSerialPort() {

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        serialPortFound = false;
        for (SerialPort tmpSerialPort : serialPorts) {
            String portName = "/dev/" + tmpSerialPort.getSystemPortName();
            if (portName.equals(config.portName())) {
                serialPort = tmpSerialPort;
                serialPortFound = true;
            }
        }

        if (!serialPortFound) {
            log.info("SerialPort not found");
            return;
        }

        serialPort.setNumDataBits(8);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(1);
        serialPort.setBaudRate(config.baudRate());
        serialPort.openPort();

        timeslotsTime = config.timeSlots() * 2 * config.timeSlotDurationInMs();
        timeslots = config.timeSlots();
        timeSlotDurationInMs = config.timeSlotDurationInMs();

        activateSerialDataListener();
    }

    @Deactivate
    protected void deactivate() {
        super.deactivate();
        packageHandler.shutdown();
        deactivateSerialDataListener();
        serialPort.closePort();
        worker.deactivate();
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.getTopic()) {
            case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
                this.worker.triggerNextRun();
                break;
        }
    }

    @Override
    public void addTask(String sourceId, LMNWiredTask task) {
        this.tasks.put(sourceId, task);
    }

    @Override
    public void removeTask(String sourceId) {
        this.tasks.remove(sourceId);
    }

    private class LMNWiredWorker extends AbstractCycleWorker {

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

            for (LMNWiredTask task : tasks.values()) {
                task.getRequest();
            }

        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public boolean isSerialDataRequestInProgress() {
        return serialDataRequestActive;
    }

    /**
     * Mark new package
     */
    public void startSerialDataRequest() {
        serialDataRequestActive = true;
    }

    /**
     * Package handling in progress after endSerialDataRequest called
     */
    public void endSerialDataRequest() {
        serialDataRequestActive = false;
    }


    public boolean isSerialDataListenerActive() {
        return serialDataListenerActive;
    }

    /**
     * Start watch for serial data listener, if no reset occur restart needed
     */
    public void startSerialDataListener() {
        serialDataListenerActive = true;
    }

    /**
     * End watch, prevent restart of serial data listener
     */
    public void endSerialDataListener() {
        serialDataListenerActive = false;
    }


    /**
     * Activate data listener for incoming packages
     *
     * @param serialPort
     */
    public void activateSerialDataListener() {

        serialDataListenerActive = serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {

                byte[] newData = event.getReceivedData();
                if (newData == null) {
                    return;
                }
//				log.debug("Num Bytes received: " + newData.length);

//				System.out.println(bytesToHex(newData) + " ");

//				log.debug("Current time: " + (System.nanoTime() / 1000000));

                // Lookup start or end byte flag 0x7e
                if (newData[0] == 0x7e && isSerialDataRequestInProgress()) { // means start

                    endSerialDataRequest(); //Package request handling started
                    //System.out.println("First Frame: " + bytesToHex(newData));
                    //Get package length def from first bytes
                    currentHDLCPackageLength = HdlcFrame.getCurrentPackageLength(newData);
//					System.out.println("Package length: " + currentHDLCPackageLength);

                    concatData = new ByteArrayOutputStream();
                    try {
                        concatData.write(newData);
                        //      System.out.println("First ConcatData Frame: " + bytesToHex(concatData.toByteArray()));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    if (newData[newData.length - 1] == 0x7e && (concatData.toByteArray().length) >= currentHDLCPackageLength) { // For Short Package including start and end in one
                        handleReturn(concatData.toByteArray());
                    }
                } else if (newData[newData.length - 1] == 0x7e && (concatData.toByteArray().length + newData.length) >= currentHDLCPackageLength) { // means end

                    try {
                        //        System.out.println("Last Data Frame: " + bytesToHex(newData));
//                        if(newData.length>=2){
//                            crc = newData[newData.length - 2];
//                        }
                        concatData.write(newData);
                        //          System.out.println("Last ConcatData Frame: " + bytesToHex(concatData.toByteArray()));
                        handleReturn(concatData.toByteArray());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    try {
                        //           System.out.println("Infix Data Frame: " + bytesToHex(newData));
                        //                  crc = newData[newData.length - 1];
                        concatData.write(newData);
                        //            System.out.println("Infix ConcatData Frame: " + bytesToHex(concatData.toByteArray()));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

                //Stop watcher to prevent restart of SerialDataListener
                endSerialDataListener();
            }

        });

        if (serialDataListenerActive) {
            log.debug("Serial DataListener started.");
        } else {
            log.debug("Serial DataListener not started!.");
        }
    }

    public void deactivateSerialDataListener() {
        serialPort.removeDataListener();
    }

    public void resetConcatData() {
        concatData = null;
    }

    protected void handleReturn(byte[] data) {
        HdlcFrame hdlcFrame = HdlcFrame.createHdlcFrameFromByte(data);
        startSerialDataRequest();
        if (hdlcFrame != null) {
//			log.info("HDLC Frame received, party hard!");
            // Measure Time
            long receiveTimeMeasure = packageHandler.setTimeStampAddressingEnd();

            //Debug output
//			log.debug("Current time after send: " + System.nanoTime());

            long timeDiff = receiveTimeMeasure - packageHandler.getTimeStampAddressing();
            long usedTimeSlotBeta = (timeDiff - packageHandler.getTimeStampAddressing() + timeSlotDurationInMs)
                    / (2 * timeSlotDurationInMs);
            double usedTimeSlot = Math.floor(usedTimeSlotBeta);
            double tmpTimeSlotFactor = usedTimeSlotBeta - usedTimeSlot;
            //Handle new device return
            if (packageHandler.getCurrentHdlcDataRequest().getRequestSource() == HdlcDataRequest.RequestSource.REGISTER) {
                if (tmpTimeSlotFactor <= 0.5) { // in data time window
                    log.debug("HDLC Frame is new device data!");
                    // Add device to List
                    Device device = new Device(hdlcFrame.getSource(), Arrays.copyOfRange(hdlcFrame.getData(), 2, 16));
                    // Check if device is already in list
                    boolean deviceInList = false;
                    for (Device tmpDevice : deviceList) {
                        if (tmpDevice.getHdlcAddress() == device.getHdlcAddress()) {
                            deviceInList = true;
                        }
                    }
                    if (!deviceInList) { // Finally add to list
                        System.out.println("Device added: " + device.getHdlcAddress());
                        deviceList.add(device);
                    }
                } else { // in guard time, ignore package
                    log.debug("HDLC Frame is new device data but in guard time!");
                }

            } else if (packageHandler.getCurrentHdlcDataRequest().getRequestSource() == HdlcDataRequest.RequestSource.PRESENCE) {
                log.debug("HDLC Frame presence check response");
                Device device = new Device(hdlcFrame.getSource(), Arrays.copyOfRange(hdlcFrame.getData(), 2, 16));

                log.debug(new String(device.getSerialNumber()));

                for (Device tmpDevice : deviceList) {
                    if (tmpDevice.getHdlcAddress() == device.getHdlcAddress()) {
                        String text;
                        text = new String(tmpDevice.getSerialNumber(), StandardCharsets.UTF_8);
                        System.out.println(text);

                        if (!Arrays.equals(tmpDevice.getSerialNumber(), device.getSerialNumber())) {
                            tmpDevice.setSerialNumber(device.getSerialNumber());
                        }
                        tmpDevice.setPresent();
                    }
                }

            } else if (packageHandler.getCurrentHdlcDataRequest().getRequestSource() == HdlcDataRequest.RequestSource.DATA) {
                log.debug("HDLC Frame is data");

                //Get current task
                LMNWiredTask currentTask = packageHandler.getCurrentHdlcDataRequest().getLmnWiredTask();
                if (currentTask != null && currentTask.getDevice().getHdlcAddress() == hdlcFrame.getSource()) {
//						log.debug("task found");
                    currentTask.setResponse(hdlcFrame);
                }

            }
        } else {
            log.debug("HDLC Frame received, check HCS or FCS");
        }

    }

    public PackageHandler getPackageHandler() {
        return packageHandler;
    }

    @Override
    public SerialPort getSerialConnection() {
        return serialPort;
    }

    @Override
    public List<Device> getDeviceList() {
        return deviceList;
    }

    public void updateConfigDevices() {
        ArrayList<String> configDevices = new ArrayList<String>();
        for (Device tmpDevice : deviceList) {
            configDevices.add(new String(tmpDevice.getSerialNumber()));
        }
        Dictionary<String, ArrayList<String>> map = new Hashtable<String, ArrayList<String>>();
        map.put("devices", configDevices);
        try {
            cm.getConfiguration(this.servicePid()).update(map);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.debug(e.getMessage());
        }
    }

}
