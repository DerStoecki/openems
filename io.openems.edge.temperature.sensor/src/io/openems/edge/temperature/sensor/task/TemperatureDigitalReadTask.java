package io.openems.edge.temperature.sensor.task;

import io.openems.edge.bridge.spi.task.SpiTask;
import io.openems.edge.bridge.spi.task.AbstractSpiTask;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.spi.mcp.api.Adc;
import io.openems.edge.temperature.module.api.TemperatureModuleVersions;


//Only for one Pin
public class TemperatureDigitalReadTask extends AbstractSpiTask implements SpiTask {

    private final Channel<Integer> channel;
    private double regressionValueA;
    private double regressionValueB;
    private double regressionValueC;
    private int lastValue = -666;
    private long lastTimestamp = 0;
    //10 °C
    private static int TEMPERATURE_CHANGE = 1000;
    private static int TIMESTAMP = 300;


    private long pinValue;

    public TemperatureDigitalReadTask(Channel<Integer> channel, String version, Adc adc, int pin) {
        super(adc.getSpiChannel());
        this.channel = channel;
        pinValue = adc.getPins().get(pin).getValue();
        allocateRegressionValues(version);
    }

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

    }

    //to avoid to big temperature fluctuations (measured within sec)
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

        if (Math.abs(lastValue) - Math.abs(value) > TEMPERATURE_CHANGE || Math.abs(lastValue) - Math.abs(value) < -(TEMPERATURE_CHANGE) && lastTimestamp - System.currentTimeMillis() < TIMESTAMP) {
            return;
        }
        lastTimestamp = System.currentTimeMillis();
        lastValue = value;
    }

}
