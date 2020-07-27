package io.openems.edge.bridge.lmnwired.api.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.bridge.lmnwired.api.AbstractOpenEmsLMNWiredComponent;
import io.openems.edge.bridge.lmnwired.api.BridgeLMNWired;
import io.openems.edge.bridge.lmnwired.api.Device;
import io.openems.edge.bridge.lmnwired.hdlc.HdlcDataRequest;
import io.openems.edge.bridge.lmnwired.hdlc.HdlcFrame;
import io.openems.edge.bridge.lmnwired.hdlc.HdlcFrameDeviceDataRequest;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.meter.api.SymmetricMeter;

import java.util.HashMap;

public class LMNWiredTask {

    protected AbstractOpenEmsLMNWiredComponent abstractOpenEmsLMNWiredComponent;
    protected BridgeLMNWired bridgeLMNWired;
    Device device;
    String obisPart;
    String obis;
    String serialNumber;
    HdlcFrameDeviceDataRequest hdlcFrameDeviceDataRequest;
    byte hdlcData[];
    int hdlcDataLength;
    SymmetricMeter.ChannelId channelId;
    int millisForTimeout = 5;
    private final Logger log = LoggerFactory.getLogger(LMNWiredTask.class);
    public boolean timeOutOccured = true;
    Float fData;

    long lastDataTimestamps[] = new long[10];
    float lastDatas[] = new float[10];
    Channel<Float> channel;
    HashMap<String, Long> channelLastTimeStamp = new HashMap<String, Long>();
    HashMap<String, Float> channelLastValue = new HashMap<String, Float>();
    float lastValue = 0;
    HashMap<String, SymmetricMeter.ChannelId> obisChannelMapping;
    String[] obisList;

    public LMNWiredTask(AbstractOpenEmsLMNWiredComponent abstractOpenEmsLMNWiredComponent,
                        BridgeLMNWired bridgeLMNWired, String serialNumber, String[] obisList,
                        HashMap<String, SymmetricMeter.ChannelId> obisChannelMapping) {
        this.abstractOpenEmsLMNWiredComponent = abstractOpenEmsLMNWiredComponent;
        this.bridgeLMNWired = bridgeLMNWired;
        this.obisList = obisList;
        this.obisChannelMapping = obisChannelMapping;
        this.serialNumber = serialNumber;
    }

    public Device getDevice() {
        return device;
    }

    public HdlcFrameDeviceDataRequest gethdlcFrameDeviceDataRequest() {
        return hdlcFrameDeviceDataRequest;
    }

    public byte[] getHdlcData() {
        return hdlcData;
    }

    public int getHdlcDataLength() {
        return hdlcDataLength;
    }

    public boolean getRequest() {

        if (!bridgeLMNWired.getDeviceList().isEmpty())
            for (Device tmpDevice : bridgeLMNWired.getDeviceList()) {
                if (new String(tmpDevice.getSerialNumber()).equals(serialNumber)) {
                    device = tmpDevice;

                    log.info("Request Data for channels (" + String.join(";", obisList) + ")");

                    hdlcFrameDeviceDataRequest = new HdlcFrameDeviceDataRequest(device, String.join(";", obisList));
                    bridgeLMNWired.getPackageHandler().addHdlcDataRequest(hdlcFrameDeviceDataRequest, 10,
                            HdlcDataRequest.RequestSource.DATA, this);

                    return true;
                }
            }

        return false;
    }

    /**
     *
     * @param hdlcFrame Raw Data Frame
     */
    public void setResponse(HdlcFrame hdlcFrame) {
        try {

            String [] lines = new String(hdlcFrame.getData()).split("\n");

            float sumGrid = 0;
            float sumNegativeActive = 0;
            float sumConsumption = 0;
            float val = 0;

            for (String line : lines) {

                String[] tmpString = line.split(";");
                String[] arrData = tmpString[1].split("\\*");

                //Save given values into mapped channels
                try {
                    channelId = obisChannelMapping.get(tmpString[0]);
                    channel = this.abstractOpenEmsLMNWiredComponent.channel(channelId);
                    if (arrData[1].contentEquals("kWh")) {
                        channel.setNextValue((Float.parseFloat(arrData[0]))); // KWH in WH and Wh to Ws
                    }else {
                        channel.setNextValue(Float.parseFloat(arrData[0]));
                    }
                }catch (Exception e) {
                    log.info(line);
                    continue;
                }

                //Convert kWh Values to W
                try {
                    if (arrData[1].contentEquals("kWh")) {
                        // Update value to Ws from kWh if needed
                        fData = (Float.parseFloat(arrData[0])); // KWH in WH and Wh to Ws

                        // Calculate current power Ws -> W from history data, use channel related data only

                        // First start timer and first value for delta
                       /* if (channelLastTimeStamp.containsKey(tmpString[0]) != true) {
                            channelLastTimeStamp.put(tmpString[0], System.currentTimeMillis());
                            channelLastValue.put(tmpString[0], fData);
                            continue;
                        }

                        float deltaT = System.currentTimeMillis() - channelLastTimeStamp.get(tmpString[0]);

                        val = (fData - channelLastValue.get(tmpString[0])) / (deltaT / 1000);*/
                        val = fData;
                        //Save current value and timestamp for next run
                        channelLastTimeStamp.put(tmpString[0], System.currentTimeMillis());
                        channelLastValue.put(tmpString[0], fData);
                    } else {
                        //Skip next steps if not a kWh / W Value
                        continue;
                    }
                } catch (Exception e) {
                    log.info(line);
                    continue;
                }

                //Add

                if (channelId == SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TOTAL) {
                    sumGrid += val;
                }

                if (channelId == SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_ONE) {
                    sumGrid += val;
                }

                if (channelId == SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_TWO) {
                    sumGrid += val;
                }

                if (channelId == SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TOTAL) {
                    sumNegativeActive += val;
                }

                if (channelId == SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_ONE) {
                    sumNegativeActive += val;
                }

                if (channelId == SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_TWO) {
                    sumNegativeActive += val;
                }

            }

            sumConsumption =  (int) (sumGrid - sumNegativeActive);

            //set ActiveProductionPower, ActiveConsumptionPower, ActivePower

            this.abstractOpenEmsLMNWiredComponent.channel(SymmetricMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY)
                    .setNextValue(sumNegativeActive);
            this.abstractOpenEmsLMNWiredComponent.channel(SymmetricMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY)
                    .setNextValue(sumConsumption);
            this.abstractOpenEmsLMNWiredComponent.channel(SymmetricMeter.ChannelId.ACTIVE_POWER)
                    .setNextValue(sumGrid);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }

}
