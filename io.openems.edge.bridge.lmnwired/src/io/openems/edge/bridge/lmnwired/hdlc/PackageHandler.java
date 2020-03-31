package io.openems.edge.bridge.lmnwired.hdlc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

import io.openems.edge.bridge.lmnwired.BridgeLMNWiredImpl;
import io.openems.edge.bridge.lmnwired.api.Device;
import io.openems.edge.bridge.lmnwired.api.task.LMNWiredTask;

/**
 * Handle Addressing and keep alive packages
 *
 * @author Leonid Verhovskij
 *
 */
public class PackageHandler {

    protected SerialPort serialPort;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService servicePresenceCheckEnd = Executors.newSingleThreadScheduledExecutor();
    private final Logger log = LoggerFactory.getLogger(PackageHandler.class);
    private String testRequestDevicesOnZero = "Request device registration with zero devices in list.";
    private String testRequestDevicesWithExisting = "Request device registration with devices in list.";
    private String testRequestDevicePresenceCheck = "Request device presence check.";
    private HdlcFrameAddressingOnEmptyList hdlcFrameAddressingOnEmptyList;
    private HdlcFrameAddressingOnDevicesInList hdlcFrameAddressingOnDevicesInList;
    private HdlcFrameCheckDevicesInList hdlcFrameCheckDevicesInList;
    protected long timeStampAddressing = 0;
    protected long timeStampAddressingEnd;
    protected long timeStampCheckPresence = 0;
    protected long timeStampCheckPresenceEnd;
    protected long timeStampDataRequested = 0;
    protected int timeslotsTime;
    protected int timeSlots;
    protected boolean addressingInProgress = false;
    protected boolean presenceCheckInProgress = false;
    protected boolean dataRequestInProgress = false;
    protected Queue<HdlcDataRequest> hdlcDataRequestList = new LinkedList<HdlcDataRequest>();
    protected HdlcDataRequest currentHdlcDataRequest;

    protected BridgeLMNWiredImpl bridgeLMNWiredImpl;


    @Reference
    protected ConfigurationAdmin cm;

    /**
     *
     * @param serialPort
     */
    public PackageHandler(SerialPort serialPort, int timeSlots, int timeslotsTime,
                          BridgeLMNWiredImpl bridgeLMNWiredImpl) {
        this.bridgeLMNWiredImpl = bridgeLMNWiredImpl;
        this.serialPort = serialPort;
        hdlcFrameAddressingOnEmptyList = new HdlcFrameAddressingOnEmptyList((byte) timeSlots);
        this.timeslotsTime = timeslotsTime;
        this.timeSlots = timeSlots;

        if (!serialPort.isOpen()) {
            log.debug("SerialPort not open.");
            return;
        }

        startServiceHandler();
    }

    Runnable runnableSendPackages = new Runnable() {

        public void run() {

            try {
                // Start next hdlc frame if guard time exceeded
                if (System.currentTimeMillis() > timeStampDataRequested) {
                    //Restart SerialPort if not open
                    if(!serialPort.isOpen()) {
                        log.info("SerialPort not ready, restarting");
                        serialPort.openPort();
                        bridgeLMNWiredImpl.activateSerialDataListener();
                    }

                    if (!hdlcDataRequestList.isEmpty()) {
                        // poll hdlc frame
                        currentHdlcDataRequest = hdlcDataRequestList.poll();

                        // Check if listener has probably crashed and restart serial data listener
//						if (bridgeLMNWiredImpl.isSerialDataListenerActive()) {
//							bridgeLMNWiredImpl.deactivateSerialDataListener();
//							bridgeLMNWiredImpl.activateSerialDataListener();
//						}
                        /// send request

                        bridgeLMNWiredImpl.startSerialDataRequest(); // Start new package mark
                        bridgeLMNWiredImpl.startSerialDataListener();// Start serial data listener watch
                        try {
                            serialPort.writeBytes(currentHdlcDataRequest.getHdlcFrame().getBytes(),
                                    currentHdlcDataRequest.getHdlcFrame().getLength());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        System.out.println("Data Request send");

                        timeStampDataRequested = System.currentTimeMillis() + currentHdlcDataRequest.getGuardTime();

                    }
                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    };

    // Lookup new devices
    Runnable runnableInviteNewDevices = new Runnable() {
        public void run() {

            try {
//
//				bridgeLMNWiredImpl.deactivateSerialDataListener();
//				bridgeLMNWiredImpl.activateSerialDataListener();
                hdlcDataRequestList.clear();

                // Noch kein Teilnehmer vorhanden
                if (bridgeLMNWiredImpl.getDeviceList().isEmpty()) {
                    log.info(testRequestDevicesOnZero);

                    // Debug output
//					log.info("Current time before send: " + System.nanoTime());

                    hdlcDataRequestList.add(new HdlcDataRequest(hdlcFrameAddressingOnEmptyList, 320,
                            HdlcDataRequest.RequestSource.REGISTER));

                } else { // Mindestens 1 Teilnehmer vorhanden
                    log.info(testRequestDevicesWithExisting + bridgeLMNWiredImpl.getDeviceList().size());

                    hdlcFrameAddressingOnDevicesInList = new HdlcFrameAddressingOnDevicesInList((byte) timeSlots,
                            bridgeLMNWiredImpl.getDeviceList());

                    hdlcDataRequestList.add(new HdlcDataRequest(hdlcFrameAddressingOnDevicesInList, 350,
                            HdlcDataRequest.RequestSource.REGISTER));
                }

                setTimeStampAddressing();
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

    };

    Runnable runnableCheckDevicePresenceEnd = new Runnable() {

        public void run() {
            try {
//				if (currentHdlcDataRequest.getRequestSource().equals(HdlcDataRequest.RequestSource.PRESENCE)) {
                clearSilentDevices();
//				}
            } catch (Exception e) {
                log.info(e.getMessage());
            }

        }

    };

    // Check device presence
    Runnable runnableCheckDevicePresence = new Runnable() {

        public void run() {
            try {

                if (!bridgeLMNWiredImpl.getDeviceList().isEmpty()) {
                    hdlcDataRequestList.clear();
                    log.info(testRequestDevicePresenceCheck);

                    hdlcFrameCheckDevicesInList = new HdlcFrameCheckDevicesInList((byte) timeSlots,
                            bridgeLMNWiredImpl.getDeviceList());

                    for(Device device: bridgeLMNWiredImpl.getDeviceList()) {
                        device.setAbsent();
                    }

                    hdlcDataRequestList.add(new HdlcDataRequest(hdlcFrameCheckDevicesInList, 350,
                            HdlcDataRequest.RequestSource.PRESENCE));

                    setTimeStampCheckPresence();

                    servicePresenceCheckEnd.schedule(runnableCheckDevicePresenceEnd, 700, TimeUnit.MILLISECONDS);

                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }

        }

    };

    public HdlcDataRequest getCurrentHdlcDataRequest() {
        return currentHdlcDataRequest;
    }

    public void addHdlcDataRequest(HdlcFrame hdlcFrame, long guardTime, HdlcDataRequest.RequestSource requestType,
                                   LMNWiredTask lmnWiredTask) {
        hdlcDataRequestList.add(new HdlcDataRequest(hdlcFrame, guardTime, requestType, lmnWiredTask));
    }

    protected void startServiceHandler() {
        // Live
        service.scheduleAtFixedRate(runnableInviteNewDevices, 0, 10, TimeUnit.SECONDS);
        service.scheduleAtFixedRate(runnableCheckDevicePresence, 5, 10, TimeUnit.SECONDS);

        service.scheduleAtFixedRate(runnableSendPackages, 0, 30, TimeUnit.MILLISECONDS);

        // Testing
//		service.scheduleAtFixedRate(runnableInviteNewDevices, 0, 200, TimeUnit.MILLISECONDS);
//				service.schedule(runnableInviteNewDevices, 0,TimeUnit.SECONDS);
//				service.schedule(runnableCheckDevicePresence, 5, TimeUnit.SECONDS);
//				service.scheduleAtFixedRate(runnableTestDataRequest, 0, 1, TimeUnit.SECONDS);
//		service.scheduleAtFixedRate(runnableRestSerialDataListener, 0, 10, TimeUnit.MILLISECONDS);

    }

    public boolean clearSilentDevices() {
        boolean deviceRemoved = false;
        if (!bridgeLMNWiredImpl.getDeviceList().isEmpty())
            for (Device tmpDevice : bridgeLMNWiredImpl.getDeviceList()) {
                if (!tmpDevice.isPresent()) {
                    bridgeLMNWiredImpl.getDeviceList().remove(tmpDevice);
                    deviceRemoved = true;
                }
            }

        return deviceRemoved;
    }

    public long setTimeStampAddressing() {
        timeStampAddressing = System.nanoTime();
        return timeStampAddressing;
    }

    public long getTimeStampAddressing() {
        return timeStampAddressing;
    }

    public long setTimeStampAddressingEnd() {
        timeStampAddressingEnd = System.nanoTime();
        return timeStampAddressingEnd;
    }

    public long setTimeStampCheckPresence() {
        timeStampCheckPresence = System.nanoTime();
        return timeStampCheckPresence;
    }

    public long getTimeStampCheckPresence() {
        return timeStampCheckPresence;
    }

    public long setTimeStampCheckPresenceEnd() {
        timeStampCheckPresenceEnd = System.nanoTime();
        return timeStampCheckPresenceEnd;
    }

    public void shutdown() {
        service.shutdown();
    }

    /**
     * Calc if last addressing request ended
     *
     * @return
     */
    public boolean isAddressingInProgress() {
        return addressingInProgress;
    }

    /**
     * Calc if last presence check request ended
     *
     * @return
     */
    public boolean isCheckupInProgress() {
        return presenceCheckInProgress;
    }

    /**
     * Calc if last presence check request ended
     *
     * @return
     */
    public boolean isDataRequestInProgress() {
        return dataRequestInProgress;
    }

}
