package io.openems.edge.pump.grundfos.task;

import io.openems.common.channel.Unit;
import io.openems.edge.common.channel.Channel;

public class PumpReadTask extends AbstractPumpTask {

    private Channel<Double> channel;

    public PumpReadTask(int address, int headerNumber, Channel<Double> channel, String unitString) {
        super(address, headerNumber, unitString);
        this.channel = channel;
    }

    @Override
    public int getRequest() {
        return -1;
    }

    @Override
    public void setResponse(byte data) {

        // When vi == 0 (false), then 0xFF means "data not available".
        if (super.vi == false) {
            if ((data & 0xFF) == 0xFF) {
                this.channel.setNextValue(null);
                return;
            }
        }

        int actualData = Byte.toUnsignedInt(data);

        int range = 254;
        double tempValue;
        if (super.vi) {
            range = 255;
        }

        switch (super.sif) {
            case 2:
                //value w.o considering Channel
                tempValue = (super.zeroScaleFactor + actualData * ((double) super.rangeScaleFactor / (double) range)) * super.unitCalc;
                this.channel.setNextValue(correctValueForChannel(tempValue));
                break;
            case 3:
                // Extended precision, not yet implemented.
                break;
            case 1:
            case 0:
            default:
                this.channel.setNextValue(actualData);
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
                    return (tempValue - 273.15) * 10;

                case "Fahrenheit":
                    //dC
                    return ((tempValue - 32) * (5.d / 9.d)) * temperatureFactor;

                case "m/10000":
                case "m/100":
                case "m/10":
                case "m":
                case "m*10":
                    if (this.channel.channelDoc().getUnit().equals(Unit.BAR)) {
                        return tempValue / 10;
                    }
                    return tempValue;
            }
        }

        return tempValue;
    }
}
