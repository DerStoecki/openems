package io.openems.edge.heatpump.device.task;

import io.openems.edge.bridge.genibus.api.GenibusTask;
import io.openems.edge.heatpump.device.api.UnitTable;


public abstract class AbstractHeatPumpTask implements GenibusTask {

    double unitCalc;
    String unitString;
    UnitTable unitTable;
    private byte address;
    private int headerNumber;
    //Scale information Factor
    int sif;
    //Value interpretation
    //
    boolean vi;
    int range = 254;
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

    public AbstractHeatPumpTask(int address, int headerNumber, String unitString) {
        this.address = (byte) address;
        this.headerNumber = headerNumber;
        switch (unitString) {
            case "Standard":
            default:
                this.unitTable = UnitTable.Standard_Unit_Table;
        }

    }

    @Override
    public byte getAddress() {
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
        if (this.vi) {
            range = 255;
        } else {
            range = 254;
        }
    }

    @Override
    public void setFourByteInformation(int vi, int bo, int sif, byte unitIndex, byte scaleFactorZeroOrHigh, byte scaleFactorRangeOrLow) {
        setOneByteInformation(vi, bo, sif);
        this.unitIndex = (unitIndex & 127);
        if (sif == 3) {
            this.scaleFactorHighOrder = Byte.toUnsignedInt(scaleFactorZeroOrHigh);
            this.scaleFactorLowOrder = Byte.toUnsignedInt(scaleFactorRangeOrLow);
        } else {
            this.zeroScaleFactor = Byte.toUnsignedInt(scaleFactorZeroOrHigh);
            this.rangeScaleFactor = Byte.toUnsignedInt(scaleFactorRangeOrLow);
        }
        if (this.unitIndex > 0) {
            this.unitString = this.unitTable.getInformationData().get(this.unitIndex);

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
