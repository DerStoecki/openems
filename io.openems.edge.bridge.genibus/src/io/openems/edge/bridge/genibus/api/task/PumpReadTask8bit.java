package io.openems.edge.bridge.genibus.api.task;

import io.openems.common.channel.Unit;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.taskmanager.Priority;

public class PumpReadTask8bit extends AbstractPumpTask {

    private Channel<Double> channel;

    private final Priority priority;

    private final double channelMultiplier;

    private int refreshInfoCounter = 0;

    public PumpReadTask8bit(int address, int headerNumber, Channel<Double> channel, String unitString, Priority priority, double channelMultiplier) {
        super(address, headerNumber, unitString, 1);
        this.channel = channel;
        this.priority = priority;
        this.channelMultiplier = channelMultiplier;
    }

    public PumpReadTask8bit(int address, int headerNumber, Channel<Double> channel, String unitString, Priority priority) {
        super(address, headerNumber, unitString, 1);
        this.channel = channel;
        this.priority = priority;
        this.channelMultiplier = 1;
    }

    @Override
    public void setResponse(byte data) {

        // ref_norm changes INFO if control mode is changed. If task is ref_norm (2, 49), regularly update INFO.
        if (getHeader() == 2 && getAddress() == 49) {
            refreshInfoCounter++;
            if (refreshInfoCounter >= 5) {
                super.resetInfo();
                refreshInfoCounter = 0;
            }
        }

        // When vi == 0 (false), then 0xFF means "data not available".
        if (super.vi == false) {
            if ((data & 0xFF) == 0xFF) {
                this.channel.setNextValue(null);
                return;
            }
        }

        int actualData = Byte.toUnsignedInt(data);

        // Process buffer length readout.
        if (super.getHeader() == 0 && super.getAddress() == 2) {
            pumpDevice.setDeviceByteBufferLength(actualData);
        }

        int range = 254;
        double tempValue;
        if (super.vi) {
            range = 255;
        }

        switch (super.sif) {
            case 2:
                //value w.o considering Channel
                tempValue = (super.zeroScaleFactor + actualData * ((double) super.rangeScaleFactor / (double) range)) * super.unitCalc;
                this.channel.setNextValue(correctValueForChannel(tempValue) * channelMultiplier);
                break;
            case 3:
                // Extended precision, 8 bit.
                tempValue = ((256 * super.scaleFactorHighOrder + super.scaleFactorLowOrder) + actualData) * super.unitCalc;
                this.channel.setNextValue(correctValueForChannel(tempValue) * channelMultiplier);
                break;
            case 1:
            case 0:
            default:
                this.channel.setNextValue(actualData * channelMultiplier);
                break;

        }
    }

    private double correctValueForChannel(double tempValue) {
        //unitString
        if (super.unitString != null) {
            // Channel unit is dC.
            int temperatureFactor = 10;

            switch (super.unitString) {
                case "Celsius/10":
                case "Celsius":
                    //dC
                    return tempValue * temperatureFactor;
                case "Kelvin/100":
                case "Kelvin":
                    //dC
                    return (tempValue - 273.15) * temperatureFactor;

                case "Fahrenheit":
                    //dC
                    return ((tempValue - 32) * (5.d / 9.d)) * temperatureFactor;

                case "m/10000":
                case "m/100":
                case "m/10":
                case "m":
                case "m*10":
                    if (this.channel.channelDoc().getUnit().equals(Unit.BAR)) {
                        return tempValue / 10.0;
                    }
                    return tempValue;
            }
        }

        return tempValue;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }
}
