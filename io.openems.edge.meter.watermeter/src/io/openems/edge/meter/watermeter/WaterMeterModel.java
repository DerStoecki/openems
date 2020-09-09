package io.openems.edge.meter.watermeter;

public enum WaterMeterModel {
    PAD_PULS_M2(0,1),
    ITRON_BM_M(1,4),
    ;

    int volAddress;
    int timeStampAddress;

    WaterMeterModel(int volume, int time) {
        this.volAddress = volume;
        this.timeStampAddress = time;
    }

    public int getVolAddress(){ return volAddress; }
    public int getTimeStampAddress(){ return timeStampAddress; }
}
