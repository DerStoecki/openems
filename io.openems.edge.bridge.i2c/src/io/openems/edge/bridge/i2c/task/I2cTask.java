package io.openems.edge.bridge.i2c.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.channel.WriteChannel;

public interface I2cTask {

    WriteChannel<Float> getFloatPowerLevel();

    void setFloatPowerLevel(float powerLevel) throws OpenemsError.OpenemsNamedException;

    int getPinPosition();

    boolean isInverse();

    int calculateDigit(int digitRange);

    String getPwmModuleId();

    String getDeviceId();

    boolean hasLed();

    int ledPosition();

}
