package io.openems.edge.heatpump.device.tempapi;


import io.openems.edge.common.channel.WriteChannel;


//Interface for Controller using extended Data Value or other
public interface HeatPumpTask {

    int getData();

    boolean isVi();

    boolean isBo();

    WriteChannel<Double> getWriteChannel();

    int getSif();

    UnitTable getUnitTable();

    int getScaleFactorHighOrder();

    int getScaleFactorLowOrder();

    int getZeroScaleFactor();

    int getRangeScaleFactor();

}
