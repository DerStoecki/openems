package io.openems.edge.heatpump.device;

public enum HeatPumpType {
    MAGNA_3(0x23, 2, 28, 2, 30, 2, 34, 2, 37,
            2, 39, 2, 58, 2, 112, 2,
            154, 2, 156, 2, 158,
            2, 159, 2, 160, 2, 161,
            2, 162, 2, 105, 4, 106,
            4, 101, 4, 103, 4, 104,
            4, 7, 3, 6, 3, 5, 3,
            25, 3, 26, 3, 22, 3);

    //read Measured Data
    //diff pressure head
    private int hDiff;
    private int hDiffHeadClass;
    //temperature Electronics
    private int tE;
    private int tEheadClass;
    //current motor
    private int iMo;
    private int imoHeadClass;
    //powerConsumption
    private int plo;
    private int ploHeadClass;
    //pressure
    private int h;
    private int hHeadClass;
    //pump flow
    private int q;
    private int qHeadClass;
    //pumped Water medium temperature
    private int tW;
    private int tWHeadClass;
    //control Mode
    private int controlMode;
    private int controlModeHeadClass;
    //alarm code
    private int alarmCodePump;
    private int alarmCodePumpHeadClass;
    //warnCode
    private int warnCode;
    private int warnCodeHeadClass;
    //alarmCode
    private int alarmCode;
    private int alarmCodeHeadClass;
    //warnBits

    private int warnBits1;
    private int warnBits1HeadClass;
    private int warnBits2;
    private int warnBits2HeadClass;
    private int warnBits3;
    private int warnBits3HeadClass;
    private int warnBits4;
    private int warnBits4HeadClass;

    //Write
    //Pump flow Config Params
    private int qMaxHi;
    private int qMaxHiHeadClass;
    private int qMaxLo;
    private int qMaxLowClass;
    //pressure Config Params
    private int deltaH;
    private int deltaHheadClass;
    private int hMaxHi;
    private int hMaxHiHeadClass;
    private int hMaxLo;
    private int hMaxLoHeadClass;
    //Commands
    private int remote;
    private int remoteHeadClass;
    private int start;
    private int startHeadClass;
    private int stop;
    private int stopHeadClass;
    private int minMotorCurve;
    private int minMotorCurveHeadClass;
    private int maxMotorCurve;
    private int maxMotorCurveHeadClass;
    private int constFrequency;
    private int constFrequencyHeadClass;


    HeatPumpType(int hDiff, int hDiffHeadClass, int tE, int tEheadClass, int iMo, int imoHeadClass, int plo,
                 int ploHeadClass, int h, int hHeadClass, int q, int qHeadClass, int tW, int tWHeadClass,
                 int controlMode, int controlModeHeadClass, int alarmCodePump,
                 int alarmCodePumpHeadClass, int warnCode, int warnCodeHeadClass, int alarmCode, int alarmCodeHeadClass,
                 int warnBits1, int warnBits1HeadClass, int warnBits2, int warnBits2HeadClass, int warnBits3,
                 int warnBits3HeadClass, int warnBits4, int warnBits4HeadClass, int qMaxHi, int qMaxHiHeadClass,
                 int qMaxLo, int qMaxLowClass, int deltaH, int deltaHheadClass, int hMaxHi, int hMaxHiHeadClass,
                 int hMaxLo, int hMaxLoHeadClass, int remote, int remoteHeadClass, int start, int startHeadClass,
                 int stop, int stopHeadClass, int minMotorCurve, int minMotorCurveHeadClass,
                 int maxMotorCurve, int maxMotorCurveHeadClass, int constFrequency, int constFrequencyHeadClass) {
        this.hDiff = hDiff;
        this.hDiffHeadClass = hDiffHeadClass;
        this.tE = tE;
        this.tEheadClass = tEheadClass;
        this.iMo = iMo;
        this.imoHeadClass = imoHeadClass;
        this.plo = plo;
        this.ploHeadClass = ploHeadClass;
        this.h = h;
        this.hHeadClass = hHeadClass;
        this.q = q;
        this.qHeadClass = qHeadClass;
        this.tW = tW;
        this.tWHeadClass = tWHeadClass;
        this.controlMode = controlMode;
        this.controlModeHeadClass = controlModeHeadClass;
        this.alarmCodePump = alarmCodePump;
        this.alarmCodePumpHeadClass = alarmCodePumpHeadClass;
        this.warnCode = warnCode;
        this.warnCodeHeadClass = warnCodeHeadClass;
        this.alarmCode = alarmCode;
        this.alarmCodeHeadClass = alarmCodeHeadClass;
        this.warnBits1 = warnBits1;
        this.warnBits1HeadClass = warnBits1HeadClass;
        this.warnBits2 = warnBits2;
        this.warnBits2HeadClass = warnBits2HeadClass;
        this.warnBits3 = warnBits3;
        this.warnBits3HeadClass = warnBits3HeadClass;
        this.warnBits4 = warnBits4;
        this.warnBits4HeadClass = warnBits4HeadClass;
        this.qMaxHi = qMaxHi;
        this.qMaxHiHeadClass = qMaxHiHeadClass;
        this.qMaxLo = qMaxLo;
        this.qMaxLowClass = qMaxLowClass;
        this.deltaH = deltaH;
        this.deltaHheadClass = deltaHheadClass;
        this.hMaxHi = hMaxHi;
        this.hMaxHiHeadClass = hMaxHiHeadClass;
        this.hMaxLo = hMaxLo;
        this.hMaxLoHeadClass = hMaxLoHeadClass;
        this.remote = remote;
        this.remoteHeadClass = remoteHeadClass;
        this.start = start;
        this.startHeadClass = startHeadClass;
        this.stop = stop;
        this.stopHeadClass = stopHeadClass;
        this.minMotorCurve = minMotorCurve;
        this.minMotorCurveHeadClass = minMotorCurveHeadClass;
        this.maxMotorCurve = maxMotorCurve;
        this.maxMotorCurveHeadClass = maxMotorCurveHeadClass;
        this.constFrequency = constFrequency;
        this.constFrequencyHeadClass = constFrequencyHeadClass;
    }

    public int gethDiff() {
        return hDiff;
    }

    public int gethDiffHeadClass() {
        return hDiffHeadClass;
    }

    public int gettE() {
        return tE;
    }

    public int gettEheadClass() {
        return tEheadClass;
    }

    public int getiMo() {
        return iMo;
    }

    public int getImoHeadClass() {
        return imoHeadClass;
    }

    public int getPlo() {
        return plo;
    }

    public int getPloHeadClass() {
        return ploHeadClass;
    }

    public int getH() {
        return h;
    }

    public int gethHeadClass() {
        return hHeadClass;
    }

    public int getQ() {
        return q;
    }

    public int getqHeadClass() {
        return qHeadClass;
    }

    public int gettW() {
        return tW;
    }

    public int gettWHeadClass() {
        return tWHeadClass;
    }

    public int getControlMode() {
        return controlMode;
    }

    public int getControlModeHeadClass() {
        return controlModeHeadClass;
    }

    public int getAlarmCodePump() {
        return alarmCodePump;
    }

    public int getAlarmCodePumpHeadClass() {
        return alarmCodePumpHeadClass;
    }

    public int getWarnCode() {
        return warnCode;
    }

    public int getWarnCodeHeadClass() {
        return warnCodeHeadClass;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public int getAlarmCodeHeadClass() {
        return alarmCodeHeadClass;
    }

    public int getWarnBits1() {
        return warnBits1;
    }

    public int getWarnBits1HeadClass() {
        return warnBits1HeadClass;
    }

    public int getWarnBits2() {
        return warnBits2;
    }

    public int getWarnBits2HeadClass() {
        return warnBits2HeadClass;
    }

    public int getWarnBits3() {
        return warnBits3;
    }

    public int getWarnBits3HeadClass() {
        return warnBits3HeadClass;
    }

    public int getWarnBits4() {
        return warnBits4;
    }

    public int getWarnBits4HeadClass() {
        return warnBits4HeadClass;
    }

    public int getqMaxHi() {
        return qMaxHi;
    }

    public int getqMaxHiHeadClass() {
        return qMaxHiHeadClass;
    }

    public int getqMaxLo() {
        return qMaxLo;
    }

    public int getqMaxLowClass() {
        return qMaxLowClass;
    }


    public int getDeltaH() {
        return deltaH;
    }

    public int getDeltaHheadClass() {
        return deltaHheadClass;
    }

    public int gethMaxHi() {
        return hMaxHi;
    }

    public int gethMaxHiHeadClass() {
        return hMaxHiHeadClass;
    }

    public int gethMaxLo() {
        return hMaxLo;
    }

    public int gethMaxLoHeadClass() {
        return hMaxLoHeadClass;
    }

    //commands


    public int getRemote() {
        return remote;
    }

    public int getRemoteHeadClass() {
        return remoteHeadClass;
    }

    public int getStart() {
        return start;
    }

    public int getStartHeadClass() {
        return startHeadClass;
    }

    public int getStop() {
        return stop;
    }

    public int getStopHeadClass() {
        return stopHeadClass;
    }

    public int getMinMotorCurve() {
        return minMotorCurve;
    }

    public int getMinMotorCurveHeadClass() {
        return minMotorCurveHeadClass;
    }

    public int getMaxMotorCurve() {
        return maxMotorCurve;
    }

    public int getMaxMotorCurveHeadClass() {
        return maxMotorCurveHeadClass;
    }

    public int getConstFrequency() {
        return constFrequency;
    }

    public int getConstFrequencyHeadClass() {
        return constFrequencyHeadClass;
    }
}
