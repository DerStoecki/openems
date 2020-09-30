package io.openems.edge.pump.grundfos.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.*;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.bridge.genibus.api.PumpDevice;

public interface PumpGrundfosChannels extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /*
        * Disclaimer:
        * When you get the Information / Data from the pump, the Units may vary.
        * They will be converted to default Channel Unit.
        *
        * PumpType:  <Section> <Number> <identifier>
        --> In the Data sheet it's listed in the section, Numbers and identifier
        *
        *
        * Hi and Lo values can be read etc but calculation for
        * concrete value (16bit) not needed and thus not implemented yet.
         * */

        //GET//

        // Protocol Data //
        /**
         * Length of communication buffer. How many bytes can be sent to this pump in one telegram. Minimum is 70,
         * which is one full APDU of 63 bytes + header and crc.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 0, 2 buf_len
         * </ul>
         * */
        BUF_LEN(Doc.of(OpenemsType.DOUBLE)),

        // Measured Data //
        /**
         * Currently used setpoint.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 2, 48 ref_act
         * </ul>
         * */
        REF_ACT(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Normalized setpoint.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 2, 49 ref_norm
         * </ul>
         * */
        REF_NORM(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Control source bits.
         * Currently active control source. From which source the pump is currently taking commands.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 2, 90  contr_source
         * </ul>
         * */
        CONTR_SOURCE(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Currently active control source. From which source the pump is currently taking commands.
         * The control source bits parsed to a text message.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 2, 90  contr_source
         * </ul>
         * */
        CONTR_SOURCE_STRING(Doc.of(OpenemsType.STRING)),
        /**
         * Differential Pressure Head.
         * <ul>
         * <li>Interface: PumpGrundfosChannels
         * <li>Type: Double
         * <li> Magna3: 8 bit Measured Data: 2,23 h_diff
         * </ul>
         * */
        DIFFERENTIAL_PRESSURE_HEAD(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Electronics Temperature.
         * <ul>
         *     <li> Interface: PumpGrundfosChannels
         *     <li> Type: Double
         *     <li> Unit: dC
         *     <li> Magna3: 8 bit Measured Data: 2,28 t_e
         * </ul>
         * */
        ELECTRONICS_TEMPERATURE(Doc.of(OpenemsType.DOUBLE).unit(Unit.DEZIDEGREE_CELSIUS)),
        /**
         * Current Motor.
         * <ul>
         *     <li> Interface: PumpGrundfosChannels
         *     <li> Type: Double
         *     <li> Magna3: 8 bit Measured Data: 2, 30 i_mo
         * </ul>
         * */
        CURRENT_MOTOR(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Power Consumption.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Unit: Watt
         *      <li> Magna3: 8 bit Measured Data: 2,34 p_lo
         * </ul>
         *
         * */
        POWER_CONSUMPTION(Doc.of(OpenemsType.DOUBLE).unit(Unit.WATT)),
        /**
         * Current Pressure.
         * Pressure/Head/level.
         * <ul>
         *     <li> Interface: PumpGrundfosChannels
         *     <li> Type: Double
         *     <li> Unit: Bar
         *     <li> Magna3: 8 bit Measured Data: 2, 37 h
         * </ul>
         * */
        CURRENT_PRESSURE(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR)),
        /**
         * Current Pump Flow.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Unit: m³/h
         *      <li> Magna3: 8 bit Measured Data: 2,39 q
         * </ul>
         * */
        CURRENT_PUMP_FLOW(Doc.of(OpenemsType.DOUBLE).unit(Unit.CUBICMETER_PER_HOUR)),
        /**
         * Pumped water medium Temperature.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Unit: dC
         *      <li> Magna3: 8 bit Measured Data: 2,58 t_w
         * </ul>
         * */
        PUMPED_WATER_MEDIUM_TEMPERATURE(Doc.of(OpenemsType.DOUBLE).unit(Unit.DEZIDEGREE_CELSIUS)),
        /**
         * Relative speed/frequency applied to motor.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Unit: Hz
         *      <li> Magna3: 8 bit Measured Data: 2, 32 f_act
         * </ul>
         * */
        MOTOR_FREQUENCY(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ)),
        /**
         * Actual Control Mode bits.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Magna3: 8 bit Measured Data: 2,112, control_mode
         * </ul>
         *
         * */
        ACTUAL_CONTROL_MODE(Doc.of(OpenemsType.DOUBLE)),
        /**
         * The Actual Control Mode bits parsed to a text message.
         * <ul>
         *      <li> Interface: PumpGrundfosChannels
         *      <li> Type: Double
         *      <li> Magna3: 8 bit Measured Data: 2,112, control_mode
         * </ul>
         *
         * */
        ACTUAL_CONTROL_MODE_ENUM(Doc.of(ControlMode.values())),
        /**
         * Alarm Code Pump. Manual says this is just for setups with multiple pumps.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Double
         *       <li> Magna3: 8 bit Measured Data: 2, 154 alarm_code_pump
         * </ul>
         *
         * */
        ALARM_CODE_PUMP(Doc.of(OpenemsType.DOUBLE)),
        /** Warn Code.
         *  <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Measured Data: 2, 156 warn_code
         *  </ul>
         * */
        WARN_CODE(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Alarm Code.
         *  <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Measured Data: 2, 158 alarm_code
         *  </ul>
         * */
        ALARM_CODE(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Warn Bits 1-4. See "Warn Bits" For Further Information
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Double
         *       <li> Magna3: 8 bit Measured Data: 2, 159-162 warn_bits1,2,3,4
         * </ul>
         * */
        WARN_BITS_1(Doc.of(OpenemsType.DOUBLE)),
        WARN_BITS_2(Doc.of(OpenemsType.DOUBLE)),
        WARN_BITS_3(Doc.of(OpenemsType.DOUBLE)),
        WARN_BITS_4(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Warn message. Warn Bits cumulative channel. Contains the messages from all warn bits.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: String
         * </ul>
         * */
        WARN_MESSAGE(Doc.of(OpenemsType.STRING)),
        /**
         * Alarm log 1-5. Contains the code for the last 5 logged alarms. Newest is in 1.
         *  <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Measured Data: 2, 163-167 alarm_log_1 to alarm_log_5
         *  </ul>
         * */
        ALARM_LOG_1(Doc.of(OpenemsType.DOUBLE)),
        ALARM_LOG_2(Doc.of(OpenemsType.DOUBLE)),
        ALARM_LOG_3(Doc.of(OpenemsType.DOUBLE)),
        ALARM_LOG_4(Doc.of(OpenemsType.DOUBLE)),
        ALARM_LOG_5(Doc.of(OpenemsType.DOUBLE)),

        // reference Values //
        /**
         * Minimum allowed reference setting.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Double
         *       <li> Magna3: 8 bit Measured Data: 2,76 r_min
         * </ul>
         * */
        R_MIN(Doc.of(OpenemsType.DOUBLE)),
        /**
         * Maximum allowed reference setting.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Double
         *       <li> Magna3: 8 bit Measured Data: 2,77 r_max
         * </ul>
         * */
        R_MAX(Doc.of(OpenemsType.DOUBLE)),

        //WRITE//

        // config params //
        //Hi and Lo Value with specific calc. == pump flow
        /**
         * Pump maximum flow. Hi value
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Unit: m³/h
         *        <li> Magna3: 16 bit split into two 8 bit Configuration Parameters: 4, 105 q_max_hi and 106 q_max_lo
         * </ul>
         * */
        SET_PUMP_MAX_FLOW(Doc.of(OpenemsType.DOUBLE).unit(Unit.CUBICMETER_PER_HOUR).accessMode(AccessMode.READ_WRITE)),
        /**
         * Low flow stop dead band relative to actual setpoint.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Double
         *       <li> Unit: Percent
         *       <li> Magna3: 8bit Configuration Parameters: 4,101 delta_h
         * </ul>
         * */
        SET_PRESSURE_DELTA(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT)),
        /**
         * Pump maximum head/pressure.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Unit: Bar
         *        <li> Magna3: 16 bit split into two 8 bit Configuration Parameters: 4,103 h_max_hi and 104 h_max_lo
         * </ul>
         * */
        SET_MAX_PRESSURE(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE).unit(Unit.BAR)),
        /**
         * Constant Pressure Mode minimum reference. INFO reads unit = 30 = 1%, min = 0, range = 100.
         * Values for this parameter are 0% - 100% (write 0 - 1.0 in channel), where % is % of the range interval
         * of the pressure sensor. The range interval of the pressure sensor is the one transmitted by INFO for h_diff.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters:  4, 83 h_const_ref_min
         * </ul>
         *
         * */
        H_CONST_REF_MIN(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 84 h_const_ref_max
         * </ul>
         * */
        H_CONST_REF_MAX(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Pump rotation frequency f_upper. Maximum frequency, hardware limit.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 30 f_upper
         * </ul>
         * */
        FREQUENCY_F_UPPER(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ).accessMode(AccessMode.READ_WRITE)),
        /**
         * Pump rotation frequency f_nom.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 31 f_nom
         * </ul>
         * */
        FREQUENCY_F_NOM(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ).accessMode(AccessMode.READ_WRITE)),
        /**
         * Pump rotation frequency f_min.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 34 f_min
         * </ul>
         * */
        FREQUENCY_F_MIN(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ).accessMode(AccessMode.READ_WRITE)),
        /**
         * Pump rotation frequency f_max.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 35 f_max
         * </ul>
         * */
        FREQUENCY_F_MAX(Doc.of(OpenemsType.DOUBLE).unit(Unit.HERTZ).accessMode(AccessMode.READ_WRITE)),

        // Sensor configuration
        /**
         * Analogue input 1 function. Enum with values 0-3.
         * 0: Not active
         * 1: Control loop feedback -> sys_fb
         * 2: Reference influence: F(ana_in_1) -> ref_att
         * 3: Other (extra measurement)
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 229 ana_in_1_func
         * </ul>
         * */
        ANA_IN_1_FUNC(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Analogue input 1 application. Which values is this sensor mapped to? For example h_diff.
         * Value 0-255, see table 8.3 on page 47 in manual.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 226 ana_in_1_applic
         * </ul>
         * */
        ANA_IN_1_APPLIC(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Analogue input 1 unit. Value 0-22, see table 8.2 on page 45 in manual.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 208 ana_in_1_unit
         * </ul>
         * */
        ANA_IN_1_UNIT(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Analogue input 1 minimum range value.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 16 bit split into two 8 bit Configuration Parameters: 4, 209-210 ana_in_1_min_hi/lo
         * </ul>
         * */
        ANA_IN_1_MIN(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Analogue input 1 maximum range value.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 16 bit split into two 8 bit Configuration Parameters: 4, 211-212 ana_in_1_max_hi/lo
         * </ul>
         * */
        ANA_IN_1_MAX(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Grundfos pressure sensor value.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Measured Data: 2, 127 grf_sensor_press
         * </ul>
         * */
        GRF_SENSOR_PRESS(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR).accessMode(AccessMode.READ_ONLY)),
        /**
         * Grundfos pressure sensor function.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Configuration Parameters: 4, 238 grf_sensor_press_func
         * </ul>
         * */
        GRF_SENSOR_PRESS_FUNC(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),
        /**
         * Maximum pressure range (=20m).
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Measured Data: 4, 254 h_range
         * </ul>
         * */
        H_RANGE(Doc.of(OpenemsType.DOUBLE).unit(Unit.BAR).accessMode(AccessMode.READ_WRITE)),

        // commands //
        /**
         * Start the Motor.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Boolean
         *        <li> Magna3: Commands: 3,6 START
         * </ul>
         * */
        START(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Stops the motor.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Boolean
         *        <li> Magna3: Commands: 3,5 STOP
         * </ul>
         * */
        STOP(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Switch to Remote Mode.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Boolean
         *        <li> Magna3: Commands: 3,7 REMOTE
         * </ul>
         * */
        REMOTE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Motor running on min Curve.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Boolean
         *        <li> Magna3: Commands: 3,25 MIN
         * </ul>
         *
         * */
        MIN_MOTOR_CURVE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Motor running on max Curve.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Boolean
         *        <li> Magna3: Commands: 3, 26 MAX
         * </ul>
         * */
        MAX_MOTOR_CURVE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Switch to control mode const. Frequency.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         *       <li> Magna3: Commands: 3,22 CONST_FREQ
         * </ul>
         *
         * */
        CONST_FREQUENCY(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Switch to control mode const. Pressure.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         *       <li> Magna3: Commands: 3, 24 CONST_PRESS
         * </ul>
         * */
        CONST_PRESSURE(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Switch the motor in control mode AutoAdapt.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         *       <li> Magna3: Commands: 3, 52 AUTO_ADAPT
         * </ul>
         *
         * */
        AUTO_ADAPT(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Turn on center LED flashing.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         *       <li> Magna3: Commands: 3, 121 wink_on
         * </ul>
         *
         * */
        WINK_ON(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),
        /**
         * Turn off center LED flashing.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         *       <li> Magna3: Commands: 3, 122 wink_off
         * </ul>
         *
         * */
        WINK_OFF(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)),

        // Reference Values //
        /**
         * Remote Reference (GENIbus set point). INFO reads unit = 30 = 1%, min = 0, range = 100.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: Double
         *        <li> Magna3: 8 bit Reference Values: 5,1 ref_rem
         * </ul>
         * */
        REF_REM(Doc.of(OpenemsType.DOUBLE).accessMode(AccessMode.READ_WRITE)),

        // Strings
        /**
         * Device product number.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: String
         *        <li> Magna3: ASCII Value: 7, 8 device_prod_no
         * </ul>
         * */
        DEVICE_PROD_NO(Doc.of(OpenemsType.STRING).accessMode(AccessMode.READ_ONLY)),
        /**
         * Device serial number in production.
         * <ul>
         *        <li> Interface: PumpGrundfosChannels
         *        <li> Type: String
         *        <li> Magna3: ASCII Value: 7, 9 serial_no
         * </ul>
         * */
        SERIAL_NO(Doc.of(OpenemsType.STRING).accessMode(AccessMode.READ_ONLY)),

        // Other, not GENIbus.
        /**
         * Connection status. Is the controller currently receiving data from the pump or not.
         * <ul>
         *       <li> Interface: PumpGrundfosChannels
         *       <li> Type: Boolean
         * </ul>
         *
         * */
        CONNECTION_OK(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }


    default Channel<Double> getBufferLength() {
        return this.channel(ChannelId.BUF_LEN);
    }

    default Channel<Double> getRefAct() {
        return this.channel(ChannelId.REF_ACT);
    }

    default Channel<Double> getRefNorm() {
        return this.channel(ChannelId.REF_NORM);
    }

    default Channel<Double> getControlSourceBits() {
        return this.channel(ChannelId.CONTR_SOURCE);
    }

    default Channel<String> getControlSource() {
        return this.channel(ChannelId.CONTR_SOURCE_STRING);
    }

    default Channel<Double> getDiffPressureHead() { return this.channel(ChannelId.DIFFERENTIAL_PRESSURE_HEAD); }

    default Channel<Double> getElectronicsTemperature() {
        return this.channel(ChannelId.ELECTRONICS_TEMPERATURE);
    }

    default Channel<Double> getCurrentMotor() {
        return this.channel(ChannelId.CURRENT_MOTOR);
    }

    default Channel<Double> getPowerConsumption() {
        return this.channel(ChannelId.POWER_CONSUMPTION);
    }

    default Channel<Double> getCurrentPressure() {
        return this.channel(ChannelId.CURRENT_PRESSURE);
    }

    default Channel<Double> getCurrentPumpFlow() {
        return this.channel(ChannelId.CURRENT_PUMP_FLOW);
    }

    default Channel<Double> getPumpedWaterMediumTemperature() {
        return this.channel(ChannelId.PUMPED_WATER_MEDIUM_TEMPERATURE);
    }

    default Channel<Double> getMotorFrequency() {
        return this.channel(ChannelId.MOTOR_FREQUENCY);
    }

    default Channel<Double> getActualControlModeBits() {
        return this.channel(ChannelId.ACTUAL_CONTROL_MODE);
    }

    default Channel<String> getActualControlMode() {
        return this.channel(ChannelId.ACTUAL_CONTROL_MODE_ENUM);
    }

    default Channel<Double> getAlarmCodePump() {
        return this.channel(ChannelId.ALARM_CODE_PUMP);
    }

    default Channel<Double> getWarnCode() {
        return this.channel(ChannelId.WARN_CODE);
    }

    default Channel<Double> getAlarmCode() {
        return this.channel(ChannelId.ALARM_CODE);
    }

    default Channel<Double> getWarnBits_1() {
        return this.channel(ChannelId.WARN_BITS_1);
    }

    default Channel<Double> getWarnBits_2() {
        return this.channel(ChannelId.WARN_BITS_2);
    }

    default Channel<Double> getWarnBits_3() {
        return this.channel(ChannelId.WARN_BITS_3);
    }

    default Channel<Double> getWarnBits_4() {
        return this.channel(ChannelId.WARN_BITS_4);
    }

    default Channel<String> getWarnMessage() {
        return this.channel(ChannelId.WARN_MESSAGE);
    }


    default Channel<Double> getAlarmLog1() {
        return this.channel(ChannelId.ALARM_LOG_1);
    }

    default Channel<Double> getAlarmLog2() {
        return this.channel(ChannelId.ALARM_LOG_2);
    }

    default Channel<Double> getAlarmLog3() {
        return this.channel(ChannelId.ALARM_LOG_3);
    }

    default Channel<Double> getAlarmLog4() {
        return this.channel(ChannelId.ALARM_LOG_4);
    }

    default Channel<Double> getAlarmLog5() { return this.channel(ChannelId.ALARM_LOG_5); }


    default Channel<Double> getRmin() {
        return this.channel(ChannelId.R_MIN);
    }

    default Channel<Double> getRmax() {
        return this.channel(ChannelId.R_MAX);
    }

    //Write Tasks
    default WriteChannel<Double> setPumpMaxFlow() {
        return this.channel(ChannelId.SET_PUMP_MAX_FLOW);
    }

    default WriteChannel<Double> setPressureDelta() {
        return this.channel(ChannelId.SET_PRESSURE_DELTA);
    }

    default WriteChannel<Double> setMaxPressure() {
        return this.channel(ChannelId.SET_MAX_PRESSURE);
    }

    default WriteChannel<Double> setConstRefMinH() {
        return this.channel(ChannelId.H_CONST_REF_MIN);
    }

    default WriteChannel<Double> setConstRefMaxH() {
        return this.channel(ChannelId.H_CONST_REF_MAX);
    }

    // Sensor configuration
    default WriteChannel<Double> setSensor1Func() {
        return this.channel(ChannelId.ANA_IN_1_FUNC);
    }

    default WriteChannel<Double> setSensor1Applic() {
        return this.channel(ChannelId.ANA_IN_1_APPLIC);
    }

    default WriteChannel<Double> setSensor1Unit() {
        return this.channel(ChannelId.ANA_IN_1_UNIT);
    }

    default WriteChannel<Double> setSensor1Min() {
        return this.channel(ChannelId.ANA_IN_1_MIN);
    }

    default WriteChannel<Double> setSensor1Max() {
        return this.channel(ChannelId.ANA_IN_1_MAX);
    }

    default Channel<Double> getSensorGsp() {
        return this.channel(ChannelId.GRF_SENSOR_PRESS);
    }

    default WriteChannel<Double> setSensorGspFunc() {
        return this.channel(ChannelId.GRF_SENSOR_PRESS_FUNC);
    }

    default WriteChannel<Double> setHrange() {
        return this.channel(ChannelId.H_RANGE);
    }


    //command Channel
    default WriteChannel<Boolean> setRemote() {
        return this.channel(ChannelId.REMOTE);
    }

    default WriteChannel<Boolean> setStart() {
        return this.channel(ChannelId.START);
    }

    default WriteChannel<Boolean> setStop() {
        return this.channel(ChannelId.STOP);
    }

    default WriteChannel<Boolean> setAutoAdapt() {
        return this.channel(ChannelId.AUTO_ADAPT);
    }

    default WriteChannel<Boolean> setMinMotorCurve() {
        return this.channel(ChannelId.MIN_MOTOR_CURVE);
    }

    default WriteChannel<Boolean> setMaxMotorCurve() {
        return this.channel(ChannelId.MAX_MOTOR_CURVE);
    }

    default WriteChannel<Boolean> setConstFrequency() {
        return this.channel(ChannelId.CONST_FREQUENCY);
    }

    default WriteChannel<Boolean> setConstPressure() {
        return this.channel(ChannelId.CONST_PRESSURE);
    }

    default WriteChannel<Boolean> setWinkOn() {
        return this.channel(ChannelId.WINK_ON);
    }

    default WriteChannel<Boolean> setWinkOff() {
        return this.channel(ChannelId.WINK_OFF);
    }


    //reference Value
    default WriteChannel<Double> setRefRem() {
        return this.channel(ChannelId.REF_REM);
    }


    //frequency
    default WriteChannel<Double> setFupper() {
        return this.channel(ChannelId.FREQUENCY_F_UPPER);
    }

    default WriteChannel<Double> setFnom() {
        return this.channel(ChannelId.FREQUENCY_F_NOM);
    }

    default WriteChannel<Double> setFmin() {
        return this.channel(ChannelId.FREQUENCY_F_MIN);
    }

    default WriteChannel<Double> setFmax() {
        return this.channel(ChannelId.FREQUENCY_F_MAX);
    }

    // Strings
    default Channel<String> getProductNumber() {
        return this.channel(ChannelId.DEVICE_PROD_NO);
    }

    default Channel<String> getSerialNumber() {
        return this.channel(ChannelId.SERIAL_NO);
    }

    // Other
    default Channel<Boolean> isConnectionOk() { return this.channel(ChannelId.CONNECTION_OK); }

    public PumpDevice getPumpDevice();

}



