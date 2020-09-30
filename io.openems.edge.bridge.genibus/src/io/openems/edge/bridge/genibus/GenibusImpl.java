package io.openems.edge.bridge.genibus;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.genibus.api.GenibusChannel;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.bridge.genibus.api.PumpDevice;
import io.openems.edge.bridge.genibus.api.task.GenibusTask;
import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final GenibusWorker worker = new GenibusWorker(this);

    protected String portName;
    protected boolean connectionOk = true;  // Start with true because this boolean is also used to track if an error message should be sent.

    protected Handler handler;

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
        if (this.isEnabled()) {
            this.worker.activate(config.id());
        }
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


    /**
     * <p>Handles the telegram. This is called by the GenibusWorker forever() method.
     *
     * @param telegram   telegram created beforehand.
     *                   </p>
     *
     *                   <p>The request apdu is needed to compare the head of the apdu frame, since the response does
     *                   not carry any identification.
     *                   The Way the Request Telegram is send, the information/data will be returned.
     *                   Every Head needs a certain treatment, so every head needs to be checked of the request Apdu.
     *                   If you have a look at the if(osAck == 2) there are 2 possible cases since the information
     *                   data length is either 1 or 4 byte long. Depending on that, the task method
     *                   setOneByteInformation or setFourByteInformation is called.
     *                   </p>
     */
    protected void handleTelegram(Telegram telegram) {
        //check OSACK --> infomration, request, data
        List<ApplicationProgramDataUnit> requestApdu = telegram.getProtocolDataUnit().getApplicationProgramDataUnitList();

        int telegramByteLength = Byte.toUnsignedInt(telegram.getLength()) - 2;  // Subtract crc
        int telegramEstimatedTimeMillis = (int) (33 + telegramByteLength * telegram.getPumpDevice().getMillisecondsPerByte());
        Telegram responseTelegram = handler.writeTelegram(telegramEstimatedTimeMillis, telegram, debug);

        // No answer received -> error handling
        // This will happen if the pump is switched off. Assume that is the case. Reset the device, so once data is
        // again received from this address, it is handled like a new pump (which might be the case).
        // A reset means all priority once tasks are sent again and all INFO is requested again. So if any of these were
        // in this failed telegram, they are not omitted.
        if (responseTelegram == null) {
            telegram.getPumpDevice().setConnectionOk(false);
            telegram.getPumpDevice().resetDevice();
            return;
        }

        telegram.getPumpDevice().setConnectionOk(true);
        List<ApplicationProgramDataUnit> responseApdu = responseTelegram.getProtocolDataUnit().getApplicationProgramDataUnitList();

        //if (debug) { this.logInfo(this.log, "--Reading Response--"); }

        AtomicInteger listCounter = new AtomicInteger();
        listCounter.set(0);
        telegram.getTelegramTaskList().forEach((key, taskList) -> {

            // ToDO: test if responseApdu has the same number of apdus as the telegramTaskList. Otherwise out of bounds error.
            byte[] data = responseApdu.get(listCounter.get()).getBytes();

            //for the GenibusTask list --> index
            int taskCounter = 0;

            // on correct position get the header.
            int osAck = requestApdu.get(listCounter.get()).getHeadOSACKforRequest();

            //if (debug) { this.logInfo(this.log, "Apdu " + (listCounter.get() + 1) + ", Apdu byte number: " + data.length + ", Apdu identifier: " + key + ", osAck: " + osAck + ", Tasklist length: " + taskList.size()); }

            if (osAck == 2) {
                listCounter.getAndIncrement();

            } else {
                /*
                if (debug) {
                    this.logInfo(this.log, "" + Byte.toUnsignedInt(data[0]));
                    this.logInfo(this.log, "" + Byte.toUnsignedInt(data[1]));
                }
                */
                for (int byteCounter = 2; byteCounter < data.length; ) {
                    /* TODO responseApdu.get(listCounter).getHeadOSACKShifted(); for further information
                     */

                    //if (debug) { this.logInfo(this.log, "" + Byte.toUnsignedInt(data[byteCounter])); }

                    GenibusTask geniTask = taskList.get(taskCounter);

                    // Read ASCII. Just one ASCII task per apdu.
                    if (geniTask.getHeader() == 7) {
                        for (int i = 2; i < data.length; i++) {
                            geniTask.setResponse(data[i]);
                        }
                        break;
                    }

                    if (osAck == 3) {
                        //vi bit 4
                        int vi = (data[byteCounter] & 0x10);
                        //bo bit 5
                        int bo = (data[byteCounter] & 0x20);
                        //sif on bit 0 and 1
                        int sif = (data[byteCounter] & 0x03);
                        //only 1 byte of data
                        if (sif == 0 || sif == 1) {
                            geniTask.setOneByteInformation(vi, bo, sif);
                            // Support for 16bit tasks
                            byteCounter += geniTask.getDataByteSize();
                            //only 4byte data
                        } else {
                            // Multi byte tasks have a 4 byte INFO for the hi byte and 1 byte info for the folllowing lo bytes
                            if (byteCounter >= data.length - 2 - geniTask.getDataByteSize()) {
                                this.logWarn(this.log, "Incorrect Data Length to SIF-->prevented Out of Bounds Exception");
                                break;
                            }
                            geniTask.setFourByteInformation(vi, bo, sif,
                                    data[byteCounter + 1], data[byteCounter + 2], data[byteCounter + 3]);
                            //bc of 4 byte data additional 3 byte incr. (or more for 16+ bit tasks)
                            byteCounter += 3 + geniTask.getDataByteSize();
                        }
                        if (debug) {
                            this.logInfo(this.log, geniTask.printInfo());
                        }
                    } else {
                        // If task is more than 8 bit, read more than one byte.
                        int byteAmount = geniTask.getDataByteSize();
                        if (byteCounter >= data.length - (byteAmount - 1)) {
                            this.logWarn(this.log, "Error reading data from response telegram. Apdu does not contain the expected number of bytes.");
                            break;
                        }
                        for (int i = 0; i < byteAmount; i++) {
                            geniTask.setResponse(data[byteCounter]);
                            byteCounter++;
                        }
                    }
                    taskCounter++;
                    if (taskList.size() <= taskCounter) {
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
     * Adds a pumpDevice to the GENIbus.
     * @param pumpDevice the PumpDevice object.
     */
    @Override
    public void addDevice(PumpDevice pumpDevice) {
        worker.addDevice(pumpDevice);
    }

    @Override
    public void removeDevice(String deviceId) {
        worker.removeDevice(deviceId);
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

    public boolean getDebug() { return debug; }
}
