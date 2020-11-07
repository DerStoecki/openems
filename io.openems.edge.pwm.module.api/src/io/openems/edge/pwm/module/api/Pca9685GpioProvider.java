package io.openems.edge.pwm.module.api;
/*
 * so...
 * This Class is somewhat copied of the official pi4j GpioProvider,
 * got problems with it (Devices won't activate in Osgi bc of it)
 * That's why I'm using same/edited functions etc of those classes.
 *
 * Credit still goes to the awesome Pi4j Team. So please check them out and support them :)
 * Thank you!*
 *
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.pi4j.io.gpio.exception.ValidationException;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class Pca9685GpioProvider extends AbstractPcaGpioProvider implements PcaGpioProvider {

    private static final int INTERNAL_CLOCK_FREQ = 25 * 1000 * 1000; // 25 MHz
    private static final BigDecimal MIN_FREQUENCY = new BigDecimal("24");
    private static final BigDecimal MAX_FREQUENCY = new BigDecimal("1600");
    private static final BigDecimal ANALOG_SERVO_FREQUENCY = new BigDecimal("45.454");
    private static final BigDecimal DIGITAL_SERVO_FREQUENCY = new BigDecimal("90.909");
    private static final BigDecimal DEFAULT_FREQUENCY;
    private static final int PWM_STEPS = 4096;
    //
    private static final int PCA9685A_MODE1 = 0x00;
    private static final int PCA9685A_PRESCALE = 0xFE;
    //    private static final int PCA9685A_LED0_ON_L = 0x06;
    //    private static final int PCA9685A_LED0_ON_H = 0x07;
    //    private static final int PCA9685A_LED0_OFF_L = 0x08;
    //    private static final int PCA9685A_LED0_OFF_H = 0x09;

    private boolean i2cBusOwner;
    private I2CBus bus;
    private I2CDevice device;
    private BigDecimal frequency;
    private BigDecimal frequencyCorrectionFactor;
    private int periodDurationMicros;
    private int maxPinPos = 18;
    private boolean wasRemoved = false;
    private int address;

    public Pca9685GpioProvider(I2CBus bus, int address, BigDecimal targetFrequency, BigDecimal frequencyCorrectionFactor) throws IOException {
        this.i2cBusOwner = false;
        this.bus = bus;
        basicSetup(address, targetFrequency, frequencyCorrectionFactor);
    }

    private void basicSetup(int address, BigDecimal targetFrequency, BigDecimal frequencyCorrectionFactor) throws IOException {
        this.address = address;
        this.device = this.bus.getDevice(address);
        this.device.write(PCA9685A_MODE1, (byte) 0);
        this.frequency = targetFrequency;
        this.frequencyCorrectionFactor = frequencyCorrectionFactor;
        //if not working try (PCA9685A_MODE1, (byte) 5);
        this.setFrequency(targetFrequency, frequencyCorrectionFactor);

    }

    /**
     * Sets the Pwm signal.
     *
     * @param pinPos Location of the PwmDevice
     * @param onPos  where the highflankstarts --> != 0 --> offset, so normally it's 0
     * @param offPos where the highflank ends --> low Flank starts
     *
     *               <p>The device write starts with 6 7 8 9 because the pca9685 is made that way: setting LED on and off
     *               and then writing the value as a byte.</p>
     */
    @Override
    public void setPwm(int pinPos, int onPos, int offPos) {
        checkForRemoval();

        if (pinPos > maxPinPos) {
            throw new IllegalArgumentException("pinPosition too Large, Check: Max Pin Value is: " + this.maxPinPos);
        }
        this.validatePositionPwmRange(onPos);
        this.validatePositionPwmRange(offPos);
        if (onPos == offPos) {
            throw new ValidationException("ON [" + onPos + "] and OFF [" + offPos + "] values must be different.");
        } else {
            try {
                //6789 bc of Pin Adress arrangement -->LEDX_ON_L: LEDX_ON_H; LED_OFF_L; LED_OFF_H; X == Pin Position
                this.device.write(6 + 4 * pinPos, (byte) (onPos & 0xFF));
                this.device.write(7 + 4 * pinPos, (byte) (onPos >> 8));
                this.device.write(8 + 4 * pinPos, (byte) (offPos & 0xFF));
                this.device.write(9 + 4 * pinPos, (byte) (offPos >> 8));
            } catch (IOException var6) {
                this.wasRemoved = true;
                throw new RuntimeException("Unable to write to PWM channel [" + pinPos + "] values for ON [" + onPos + "] and OFF [" + offPos + "] position.", var6);
            }

        }

    }

    private void checkForRemoval() {
        if (this.wasRemoved) {
            try {
                basicSetup(this.address, this.frequency, this.frequencyCorrectionFactor);
                this.wasRemoved = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * sets the Pwm Signal.
     *
     * @param pinPos position of the Pwm Device
     * @param offPos offPosition of the PwmSignal.
     *               <p>  Note: On Position will be Set to 0 --> No Offset.</p>
     */
    @Override
    public void setPwm(int pinPos, int offPos) {
        this.setPwm(pinPos, 0, offPos);
    }


    @Override
    public void validatePositionPwmRange(int onOrOffPos) {

        if (onOrOffPos < 0 || onOrOffPos > 4095) {
            throw new ValidationException("PWM position value [" + onOrOffPos + "] must be between 0 and 4095.");
        }

    }

    /**
     * Setting the pwm-signal to a continuous high-flank.
     *
     * @param pinPos location of the pwm device.
     */
    @Override
    public void setAlwaysOn(int pinPos) {
        checkForRemoval();
        try {
            this.device.write(6 + 4 * pinPos, (byte) 0);
            this.device.write(7 + 4 * pinPos, (byte) 16);
            this.device.write(8 + 4 * pinPos, (byte) 0);
            this.device.write(9 + 4 * pinPos, (byte) 0);
        } catch (IOException var6) {
            this.wasRemoved = true;
            throw new RuntimeException("Error while trying to set channel [" + pinPos + "] always ON.", var6);
        }
    }

    /**
     * Setting the pwm-signal to a continuous low-flank.
     *
     * @param pinPos location of the pwm device.
     */
    @Override
    public void setAlwaysOff(int pinPos) {
        checkForRemoval();
        try {
            this.device.write(6 + 4 * pinPos, (byte) 0);
            this.device.write(7 + 4 * pinPos, (byte) 0);
            this.device.write(8 + 4 * pinPos, (byte) 0);
            this.device.write(9 + 4 * pinPos, (byte) 16);
        } catch (IOException var6) {
            this.wasRemoved = true;
            throw new RuntimeException("Error while trying to set channel [" + pinPos + "] always OFF.", var6);
        }
    }

    /**
     * Sets the Frequency of the device.
     *
     * @param targetFrequency           wanted frequency.
     * @param frequencyCorrectionFactor actual measured frequency / deviation.
     */
    @Override
    public void setFrequency(BigDecimal targetFrequency, BigDecimal frequencyCorrectionFactor) {
        this.validateFrequency(targetFrequency);
        this.frequency = targetFrequency;
        this.periodDurationMicros = this.calculatePeriodDuration();
        int prescale = this.calculatePrescale(frequencyCorrectionFactor);

        try {
            int oldMode = this.device.read(0);
            int newMode = (oldMode & 0x7F) | 0x10;
            this.device.write(PCA9685A_MODE1, (byte) newMode); //go to sleep
            this.device.write(PCA9685A_PRESCALE, (byte) prescale);
            this.device.write(PCA9685A_MODE1, (byte) oldMode);
            Thread.sleep(1L);
            this.device.write(0, (byte) (oldMode | 0x80));
        } catch (IOException e) {
            throw new RuntimeException("Unable to set prescale value [" + prescale + "]", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //following stuff is copy pasted from :
    //https://github.com/Pi4J/pi4j/tree/master/pi4j-gpio-extension/src/main/java/com/pi4j/gpio/extension/pca
    private void validateFrequency(BigDecimal frequency) {
        if (frequency.compareTo(MIN_FREQUENCY) == -1 || frequency.compareTo(MAX_FREQUENCY) == 1) {
            throw new ValidationException("Frequency [" + frequency + "] must be between 40.0 and 1600.0 Hz.");
        }
    }

    private int calculatePeriodDuration() {
        return (new BigDecimal("1000000")).divide(this.frequency, 0, RoundingMode.HALF_UP).intValue();
    }

    private int calculatePrescale(BigDecimal frequencyCorrectionFactor) {
        BigDecimal theoreticalPrescale = BigDecimal.valueOf(INTERNAL_CLOCK_FREQ);
        theoreticalPrescale = theoreticalPrescale.divide(BigDecimal.valueOf(PWM_STEPS), 3, RoundingMode.HALF_UP);
        theoreticalPrescale = theoreticalPrescale.divide(this.frequency, 0, RoundingMode.HALF_UP);
        theoreticalPrescale = theoreticalPrescale.subtract(BigDecimal.ONE);
        return theoreticalPrescale.multiply(frequencyCorrectionFactor).intValue();
    }

    static {
        DEFAULT_FREQUENCY = ANALOG_SERVO_FREQUENCY;
    }

    public static int getInternalClockFreq() {
        return INTERNAL_CLOCK_FREQ;
    }

    public static BigDecimal getMinFrequency() {
        return MIN_FREQUENCY;
    }

    public static BigDecimal getMaxFrequency() {
        return MAX_FREQUENCY;
    }

    public static BigDecimal getAnalogServoFrequency() {
        return ANALOG_SERVO_FREQUENCY;
    }

    public static BigDecimal getDigitalServoFrequency() {
        return DIGITAL_SERVO_FREQUENCY;
    }

    public static BigDecimal getDefaultFrequency() {
        return DEFAULT_FREQUENCY;
    }

    public static int getPwmSteps() {
        return PWM_STEPS;
    }
}
