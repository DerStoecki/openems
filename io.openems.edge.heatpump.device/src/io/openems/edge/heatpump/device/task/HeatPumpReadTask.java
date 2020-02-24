package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.Channel;

public class HeatPumpReadTask extends AbstractHeatPumpTask {

    private Channel<Double> channel;


    private double unitCalc = 1;

    public HeatPumpReadTask(int address, int headerNumber, Channel<Double> channel, String unitString) {
        super(address, headerNumber, unitString);
        this.channel = channel;
    }

    @Override
    public byte getRequest() {
        return -1;
    }

    @Override
    public void setResponse(byte data) {
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
            //dC
            int temperatureFactor = 10;
            //watt
            int powerFactor = 1000;
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

                case "kW":
                case "kW*10":
                    return tempValue * powerFactor;

                case "psi*10":
                case "psi":
                    //1 psi ca. 0.069
                    return tempValue * 0.069;
                case "kPa":
                    //1bar = 100kPa
                    return tempValue * 0.01;
            }
        }

        return tempValue;
    }
}
