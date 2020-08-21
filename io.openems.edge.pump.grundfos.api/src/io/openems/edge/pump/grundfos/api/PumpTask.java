package io.openems.edge.pump.grundfos.api;


import io.openems.edge.common.channel.WriteChannel;


/**
 * Interfacce for HeatPumpTask, for future. If a Controller/Task should calculate the Values of Hi and Lo values.
 * Only getter needed.
 */
public interface PumpTask {

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
