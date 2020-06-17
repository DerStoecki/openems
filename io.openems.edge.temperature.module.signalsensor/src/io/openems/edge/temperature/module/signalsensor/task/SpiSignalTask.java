package io.openems.edge.temperature.module.signalsensor.task;

import io.openems.edge.bridge.spi.task.AbstractSpiTask;
import io.openems.edge.bridge.spi.task.SpiTask;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.spi.mcp.api.Adc;
import io.openems.edge.temperature.module.api.TemperatureModuleVersions;

public class SpiSignalTask extends AbstractSpiTask implements SpiTask {

    private final Channel<Integer> channel;
    private final Channel<Boolean> signalActive;
    private double regressionValueA;
    private double regressionValueB;
    private double regressionValueC;
    private int lastValue = -666;
    private long lastTimestamp = 0;
    //10 °C
    private static int TEMPERATURE_CHANGE = 200;
    private static int TIMESTAMP = 5000;

    private long pinValue;

    public SpiSignalTask(Channel<Integer> temperature, Channel<Boolean> signalActive, String versionId,
                         Adc adcForTemperature, int pinPosition) {
        super(adcForTemperature.getSpiChannel());
        this.channel = temperature;
        this.signalActive = signalActive;
        this.pinValue = adcForTemperature.getPins().get(pinPosition).getValue();
        allocateRegressionValues(versionId);
    }

    /**
     * Regression Values are needed to calculate Correct temperature.
     * <p>
     *     The Regressionvalues are for Temperature(x) = ax²+bx+c
     *     Depending on the version of the Temperature Module, regression values can vary.
     * </p>
     * */
    private void allocateRegressionValues(String version) {
        switch (version) {
            //more to come with further versions
            case "1":
                this.regressionValueA = TemperatureModuleVersions.TEMPERATURE_MODULE_V_1.getRegressionValueA();
                this.regressionValueB = TemperatureModuleVersions.TEMPERATURE_MODULE_V_1.getRegressionValueB();
                this.regressionValueC = TemperatureModuleVersions.TEMPERATURE_MODULE_V_1.getRegressionValueC();
                break;
        }

    }

    /**
     * returns the pinValue in a byte array, needed by the SpiWiringPi --> temperature.
     *
     * @return PinValue as byte array
     */
    @Override
    public byte[] getRequest() {
        long output = this.pinValue;
        byte[] data = {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            data[2 - i] = (byte) (output % 256);
            output = output >> 8;
        }
        return data;
    }

    /**
     * Calculates the Temperature written in the pin; using the SpiWiringPi before.
     * regressionValues are given by the developers of the Temperature Module.
     * If the Temperature > 100°C Signal will be active/True.
     * Remember that Temperature is in dC not C.
     *
     */
    @Override
    public void setResponse(byte[] data) {
        int digit = (data[1] << 8) + (data[2] & 0xFF);
        digit &= 0xFFF;
        int value = (int) (((this.regressionValueA * digit * digit)
                + (this.regressionValueB * digit)
                + (this.regressionValueC)) * 10);
        compareLastValueWithCurrent(value);
        if (lastValue == value) {
            this.channel.setNextValue(value);
        } else {
            this.channel.setNextValue(lastValue);
        }

        if (this.channel.getNextValue().get() > 1000) {
            this.signalActive.setNextValue(true);
        } else {
            this.signalActive.setNextValue(false);
        }

    }

    /**
     * to avoid to big temperature fluctuations (measured within sec).
     *
     * @param value the calculated temperature in setResponse.
     *              if the change is way too big in a too short time --> it won't be displayed.
     */

    private void compareLastValueWithCurrent(int value) {

        if (lastTimestamp == 0) {
            lastTimestamp = System.currentTimeMillis();
        }
        if (lastValue == -666) {
            if (value == 0) {
                return;
            }
            lastValue = value;
        }

        if (Math.abs(lastValue) - Math.abs(value) > TEMPERATURE_CHANGE && Math.abs(lastValue) - Math.abs(value) < -(TEMPERATURE_CHANGE) && lastTimestamp - System.currentTimeMillis() < TIMESTAMP) {
            return;
        }
        lastTimestamp = System.currentTimeMillis();
        lastValue = value;
    }

}
