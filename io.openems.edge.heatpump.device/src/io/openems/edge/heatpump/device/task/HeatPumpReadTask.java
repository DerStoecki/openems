package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.heatpump.device.UnitTable;

public class HeatPumpReadTask extends HeatPumpTask {

    private Channel<Double> channel;

    private UnitTable unitTable;
    private double unitCalc = 1;
    private String unitString;

    public HeatPumpReadTask(int address, int headerNumber, Channel<Double> channel, String dataUnit) {
        super(address, headerNumber);
        this.channel = channel;
        switch (dataUnit) {
            case "Standard":
            default:
                this.unitTable = UnitTable.Standard_Unit_Table;
        }

    }

    @Override
    public byte getRequest() {
        return -1;
    }

    @Override
    public void setResponse(byte data) {
        int actualData = Byte.toUnsignedInt(data);
        if (super.unitIndex >= 0) {
            this.unitString = this.unitTable.getInformationData().get(super.unitIndex);
            if (this.unitString != null) {
                switch (unitString) {

                    case "Celsius/10":
                    case "bar/10":
                        unitCalc = 0.1;
                        break;

                    case "Kelvin/100":
                    case "bar/100":
                        unitCalc = 0.01;
                        break;

                    case "bar/1000":
                        unitCalc = 0.001;
                        break;

                    case "Watt*10":
                    case "kW*10":
                    case "psi*10":
                        unitCalc = 10;
                        break;

                    case "Watt*100":
                        unitCalc = 100;
                        break;

                    case "Celsius":
                    case "Fahrenheit":
                    case "Kelvin":
                    case "Watt":
                    case "kW":
                    case "bar":
                    case "kPa":
                    case "psi":
                    default:
                        unitCalc = 1;
                }
            }
        }
        int range = 254;
        double tempValue;
        if (super.vi) {
            range = 255;
        }

        switch (super.sif) {
            case 2:
                //value w.o considering Channel
                tempValue = (super.zeroScaleFactor + actualData * ((double) super.rangeScaleFactor / (double) range)) * unitCalc;
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
        if (unitString != null) {
            switch (unitString) {
                case "Celsius/10":
                case "Celsius":
                    //dC
                    return tempValue * 10;
                case "Kelvin/100":
                case "Kelvin":
                    //dC
                    return (tempValue - 273.15) * 10;

                case "Fahrenheit":
                    //dC
                    return ((tempValue - 32) * (5.d / 9.d)) * 10;

                case "kW":
                case "kW*10":
                    return tempValue * 1000;

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
