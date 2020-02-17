package io.openems.edge.heatpump.device.task;

import io.openems.edge.bridge.genibus.api.GenibusTask;
import io.openems.edge.common.channel.Channel;


public abstract class HeatPumpTask implements GenibusTask {

    private int address;
    private int headerNumber;
    //Scale information Factor
    int sif;
    //Value interpretation
    //
    boolean vi;
    //Byte Order 0 = HighOrder 1 = Low Order
    //
    boolean bo;
    int unitIndex = -66;
    int scaleFactorHighOrder;
    int scaleFactorLowOrder;
    int zeroScaleFactor;
    int rangeScaleFactor;
    boolean informationAvailable;
    boolean wasAdded;

    public HeatPumpTask(int address, int headerNumber) {
        this.address = address;
        this.headerNumber = headerNumber;

    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public int getHeader() {
        return headerNumber;
    }

    @Override
    public void setOneByteInformation(int vi, int bo, int sif) {
        this.vi = vi != 0;
        this.bo = bo != 0;
        this.sif = sif;
        this.informationAvailable = true;
    }

    @Override
    public void setFourByteInformation(int vi, int bo, int sif, byte unitIndex, byte scaleFactorZeroOrHigh, byte scaleFactorRangeOrLow) {
        setOneByteInformation(vi, bo, sif);
        this.unitIndex = unitIndex & 127;
        if (sif == 3) {
            this.scaleFactorHighOrder = Byte.toUnsignedInt(scaleFactorZeroOrHigh);
            this.scaleFactorLowOrder = Byte.toUnsignedInt(scaleFactorRangeOrLow);
        } else {
            this.zeroScaleFactor = Byte.toUnsignedInt(scaleFactorZeroOrHigh);
            this.rangeScaleFactor = Byte.toUnsignedInt(scaleFactorRangeOrLow);
        }
    }

    @Override
    public boolean wasAdded() {
        return wasAdded;
    }

    @Override
    public boolean InformationDataAvailable() {
        return informationAvailable;
    }

}
