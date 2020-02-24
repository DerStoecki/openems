package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.heatpump.device.api.UnitTable;
import io.openems.edge.heatpump.device.api.HeatPumpTask;

public class HeatPumpWriteTask extends AbstractHeatPumpTask implements HeatPumpTask {

    private WriteChannel<Double> channel;
    private int readData;

    public HeatPumpWriteTask(int address, int headerNumber, WriteChannel<Double> channel, String unitString) {
        super(address, headerNumber, unitString);
        this.channel = channel;
    }

    @Override
    public int getRequest() {
        int request = -256;
        if (super.informationAvailable && this.channel.getNextWriteValue().isPresent()) {
            //NOTE! The Correct Calculated Digit will be calc by a Controller!
            double dataOfChannel = this.channel.getNextWriteValue().get();
            return (byte) dataOfChannel;
            }
        return request;
    }

    @Override
    public void setResponse(byte data) {
        this.readData = Byte.toUnsignedInt(data);
    }


    @Override
    public int getData() {
        return this.readData;
    }
    @Override
    public boolean isVi() {
        return vi;
    }

    @Override
    public boolean isBo() {
        return bo;
    }

    @Override
    public WriteChannel<Double> getWriteChannel() {
        return this.channel;
    }

    @Override
    public int getSif() {
        return sif;
    }

    @Override
    public UnitTable getUnitTable() {
        return unitTable;
    }

    @Override
    public int getScaleFactorHighOrder() {
        return scaleFactorHighOrder;
    }

    @Override
    public int getScaleFactorLowOrder() {
        return scaleFactorLowOrder;
    }

    @Override
    public int getZeroScaleFactor() {
        return zeroScaleFactor;
    }

    @Override
    public int getRangeScaleFactor() {
        return rangeScaleFactor;
    }
}
