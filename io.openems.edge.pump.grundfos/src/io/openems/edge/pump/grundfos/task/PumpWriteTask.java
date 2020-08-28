package io.openems.edge.pump.grundfos.task;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.pump.grundfos.api.PumpTask;
import io.openems.edge.pump.grundfos.api.UnitTable;

public class PumpWriteTask extends AbstractPumpTask implements PumpTask {

    private WriteChannel<Double> channel;
    private int readData;

    public PumpWriteTask(int address, int headerNumber, WriteChannel<Double> channel, String unitString) {
        super(address, headerNumber, unitString);
        this.channel = channel;
        // The method "getRequest()" does not work without first calling "setFourByteInformation()". Normally
        // "setFourByteInformation()" is executed when an info response APDU is processed. For ref_rem (5, 1) we can call
        // "setFourByteInformation()" manually since the parameters are fix and don't change. This way we do not need
        // to send an info APDU for ref_rem to be able to use "getRequest()".
        if (headerNumber == 5 && address == 1) {
            super.setFourByteInformation(0,0,3, (byte)0b11110, (byte)0, (byte)0b1100100);
        }
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
