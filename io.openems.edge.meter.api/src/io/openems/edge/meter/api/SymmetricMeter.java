package io.openems.edge.meter.api;

import io.openems.common.OpenemsConstants;
import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.common.utils.IntUtils;
import io.openems.common.utils.IntUtils.Round;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerDoc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusType;

/**
 * Represents a Symmetric Meter.
 *
 * <p>
 * <ul>
 * <li>Negative ActivePower and ConsumptionActivePower represent Consumption,
 * i.e. power that is 'leaving the system', e.g. feed-to-grid
 * <li>Positive ActivePower and ProductionActivePower represent Production, i.e.
 * power that is 'entering the system', e.g. buy-from-grid
 * </ul>
 */
public interface SymmetricMeter extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * Frequency.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: mHz
         * <li>Range: only positive values
         * </ul>
         */
        FREQUENCY(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.MILLIHERTZ)), //
        /**
         * Minimum Ever Active Power.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: W
         * <li>Range: negative or '0'
         * <li>Implementation Note: value is automatically derived from ACTIVE_POWER
         * </ul>
         */
        MIN_ACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.WATT)), //
        /**
         * Maximum Ever Active Power.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: W
         * <li>Range: positive or '0'
         * <li>Implementation Note: value is automatically derived from ACTIVE_POWER
         * </ul>
         */
        MAX_ACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.WATT)), //
        /**
         * Active Power.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: W
         * <li>Range: negative values for Consumption (power that is 'leaving the
         * system', e.g. feed-to-grid); positive for Production (power that is 'entering
         * the system')
         * </ul>
         */
        ACTIVE_POWER(new IntegerDoc() //
                .unit(Unit.WATT) //
                .text(OpenemsConstants.POWER_DOC_TEXT) //
                .onInit(channel -> {
                    channel.onSetNextValue(value -> {
                        /*
                         * Fill Min/Max Active Power channels
                         */
                        if (value.isDefined()) {
                            int newValue = value.get();
                            {
                                Channel<Integer> minActivePowerChannel = channel.getComponent()
                                        .channel(ChannelId.MIN_ACTIVE_POWER);
                                int minActivePower = minActivePowerChannel.value().orElse(0);
                                int minNextActivePower = minActivePowerChannel.getNextValue().orElse(0);
                                if (newValue < Math.min(minActivePower, minNextActivePower)) {
                                    // avoid getting called too often -> round to 100
                                    newValue = IntUtils.roundToPrecision(newValue, Round.TOWARDS_ZERO, 100);
                                    minActivePowerChannel.setNextValue(newValue);
                                }
                            }
                            {
                                Channel<Integer> maxActivePowerChannel = channel.getComponent()
                                        .channel(ChannelId.MAX_ACTIVE_POWER);
                                int maxActivePower = maxActivePowerChannel.value().orElse(0);
                                int maxNextActivePower = maxActivePowerChannel.getNextValue().orElse(0);
                                if (newValue > Math.max(maxActivePower, maxNextActivePower)) {
                                    // avoid getting called too often -> round to 100
                                    newValue = IntUtils.roundToPrecision(newValue, Round.AWAY_FROM_ZERO, 100);
                                    maxActivePowerChannel.setNextValue(newValue);
                                }
                            }
                        }
                    });
                })), //
        /**
         * Reactive Power.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: var
         * <li>Range: negative values for Consumption (power that is 'leaving the
         * system', e.g. feed-to-grid); positive for Production (power that is 'entering
         * the system')
         * </ul>
         */
        REACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.VOLT_AMPERE_REACTIVE) //
                .text(OpenemsConstants.POWER_DOC_TEXT)), //
        /**
         * Active Production Energy.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: Wh
         * </ul>
         */
        ACTIVE_PRODUCTION_ENERGY(Doc.of(OpenemsType.LONG) //
                .unit(Unit.WATT_HOURS)),
        /**
         * Active Consumption Energy.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: Wh
         * </ul>
         */
        ACTIVE_CONSUMPTION_ENERGY(Doc.of(OpenemsType.LONG) //
                .unit(Unit.WATT_HOURS)),
        /**
         * Voltage.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: mV
         * </ul>
         */
        VOLTAGE(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.MILLIVOLT)), //
        /**
         * Current.
         *
         * <ul>
         * <li>Interface: Meter Symmetric
         * <li>Type: Integer
         * <li>Unit: mA
         * </ul>
         */
        CURRENT(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.MILLIAMPERE)), //

        //Consolinno Channel:

        POSITIVE_ACTIVE_ENERGY_TOTAL(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        POSITIVE_ACTIVE_ENERGY_TARIF_ONE(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        POSITIVE_ACTIVE_ENERGY_TARIF_TWO(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        ELECTRICITY_EFFECTIVE_VALUE(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT)),
        NEGATIVE_ACTIVE_ENERGY_TOTAL(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        NEGATIVE_ACTIVE_ENERGY_TARIF_ONE(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        NEGATIVE_ACTIVE_ENERGY_TARIF_TWO(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        INSTANTANEOUS_VOLTAGE_PHASE_ONE(Doc.of(OpenemsType.FLOAT).unit(Unit.VOLT)),
        INSTANTANEOUS_VOLTAGE_PHASE_TWO(Doc.of(OpenemsType.FLOAT).unit(Unit.VOLT)),
        INSTANTANEOUS_VOLTAGE_PHASE_THREE(Doc.of(OpenemsType.FLOAT).unit(Unit.VOLT)),
        INSTANTANEOUS_AMPERAGE_PHASE_ONE(Doc.of(OpenemsType.FLOAT).unit(Unit.AMPERE)),
        INSTANTANEOUS_AMPERAGE_PHASE_TWO(Doc.of(OpenemsType.FLOAT).unit(Unit.AMPERE)),
        INSTANTANEOUS_AMPERAGE_PHASE_THREE(Doc.of(OpenemsType.FLOAT).unit(Unit.AMPERE)),
        PHASE_ANGLE_MOMENTARY_VALUE_ONE(Doc.of(OpenemsType.FLOAT)),
        PHASE_ANGLE_MOMENTARY_VALUE_TWO(Doc.of(OpenemsType.FLOAT)),
        PHASE_ANGLE_MOMENTARY_VALUE_FOUR(Doc.of(OpenemsType.FLOAT)),
        PHASE_ANGLE_MOMENTARY_VALUE_FIFTEEN(Doc.of(OpenemsType.FLOAT)),
        PHASE_ANGLE_MOMENTARY_VALUE_TWENTY_SIX(Doc.of(OpenemsType.FLOAT)),
        FREQUENCY_MOMENTARY_VALUE_TOTAL(Doc.of(OpenemsType.FLOAT).unit(Unit.HERTZ)),
        ENERGY_USAGE_LAST_DAY(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        ENERGY_USAGE_LAST_WEEK(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        ENERGY_USAGE_LAST_MONTH(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        ENERGY_USAGE_LAST_YEAR(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        ENERGY_USAGE_SINCE_LAST_RESET(Doc.of(OpenemsType.FLOAT).unit(Unit.WATT_SECONDS)),
        GENERAL_USAGE(Doc.of(OpenemsType.LONG)),
        PRODUCTION_NUMBER(Doc.of(OpenemsType.STRING)),
        CHECKSUM(Doc.of(OpenemsType.FLOAT)),
        INTERNAL_ERROR(Doc.of(OpenemsType.STRING));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    /**
     * Gets the type of this Meter.
     *
     * @return the MeterType
     */
    MeterType getMeterType();

    public static ModbusSlaveNatureTable getModbusSlaveNatureTable(AccessMode accessMode) {
        return ModbusSlaveNatureTable.of(SymmetricMeter.class, accessMode, 100) //
                .channel(0, ChannelId.FREQUENCY, ModbusType.FLOAT32) //
                .channel(2, ChannelId.MIN_ACTIVE_POWER, ModbusType.FLOAT32) //
                .channel(4, ChannelId.MAX_ACTIVE_POWER, ModbusType.FLOAT32) //
                .channel(6, ChannelId.ACTIVE_POWER, ModbusType.FLOAT32) //
                .channel(8, ChannelId.REACTIVE_POWER, ModbusType.FLOAT32) //
                .channel(10, ChannelId.ACTIVE_PRODUCTION_ENERGY, ModbusType.FLOAT32) //
                .channel(12, ChannelId.ACTIVE_CONSUMPTION_ENERGY, ModbusType.FLOAT32) //
                .channel(14, ChannelId.VOLTAGE, ModbusType.FLOAT32) //
                .channel(16, ChannelId.CURRENT, ModbusType.FLOAT32) //
                //ConsolinnoChannels
                .channel(18, ChannelId.POSITIVE_ACTIVE_ENERGY_TOTAL, ModbusType.FLOAT32)
                .channel(20, ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_ONE, ModbusType.FLOAT32)
                .channel(22, ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_TWO, ModbusType.FLOAT32)
                .channel(24, ChannelId.NEGATIVE_ACTIVE_ENERGY_TOTAL, ModbusType.FLOAT32)
                .channel(26, ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_ONE, ModbusType.FLOAT32)
                .channel(28, ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_TWO, ModbusType.FLOAT32)
                .channel(30, ChannelId.ELECTRICITY_EFFECTIVE_VALUE, ModbusType.FLOAT32)
                .channel(32, ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_ONE, ModbusType.FLOAT32)//
                .channel(34, ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_TWO, ModbusType.FLOAT32)
                .channel(36, ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_THREE, ModbusType.FLOAT32)
                .channel(38, ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_ONE, ModbusType.FLOAT32)
                .channel(40, ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_TWO, ModbusType.FLOAT32)
                .channel(42, ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_THREE, ModbusType.FLOAT32)
                .channel(44, ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_ONE, ModbusType.FLOAT32)
                .channel(46, ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWO, ModbusType.FLOAT32)
                .channel(48, ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FOUR, ModbusType.FLOAT32)
                .channel(50, ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FIFTEEN, ModbusType.FLOAT32)
                .channel(52, ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWENTY_SIX, ModbusType.FLOAT32)
                .channel(54, ChannelId.FREQUENCY_MOMENTARY_VALUE_TOTAL, ModbusType.FLOAT32)
                .channel(56, ChannelId.ENERGY_USAGE_LAST_DAY, ModbusType.FLOAT32)
                .channel(58, ChannelId.ENERGY_USAGE_LAST_WEEK, ModbusType.FLOAT32)
                .channel(60, ChannelId.ENERGY_USAGE_LAST_MONTH, ModbusType.FLOAT32)
                .channel(62, ChannelId.ENERGY_USAGE_LAST_YEAR, ModbusType.FLOAT32)
                .channel(64, ChannelId.ENERGY_USAGE_SINCE_LAST_RESET, ModbusType.FLOAT32)
                .channel(66, ChannelId.GENERAL_USAGE, ModbusType.FLOAT32)
                .channel(68, ChannelId.PRODUCTION_NUMBER, ModbusType.FLOAT32)
                .channel(70, ChannelId.CHECKSUM, ModbusType.FLOAT32)
                .channel(72, ChannelId.INTERNAL_ERROR, ModbusType.FLOAT32)
                .build();

    }

    /**
     * Gets the Active Power in [W]. Negative values for Consumption; positive for
     * Production
     *
     * @return the Channel
     */
    default Channel<Integer> getActivePower() {
        return this.channel(ChannelId.ACTIVE_POWER);
    }

    /**
     * Gets the Reactive Power in [var]. Negative values for Consumption; positive
     * for Production.
     *
     * @return the Channel
     */
    default Channel<Integer> getReactivePower() {
        return this.channel(ChannelId.REACTIVE_POWER);
    }

    /**
     * Gets the Production Active Energy in [Wh]. This relates to positive
     * ACTIVE_POWER.
     *
     * @return the Channel
     */
    default Channel<Long> getActiveProductionEnergy() {
        return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY);
    }

    /**
     * Gets the Frequency in [mHz]. FREQUENCY
     *
     * @return the Channel
     */
    default Channel<Integer> getFrequency() {
        return this.channel(ChannelId.FREQUENCY);
    }

    /**
     * Gets the Voltage in [mV].
     *
     * @return the Channel
     */

    default Channel<Integer> getVoltage() {
        return this.channel(ChannelId.VOLTAGE);
    }

    /**
     * Gets the Consumption Active Energy in [Wh]. This relates to negative
     * ACTIVE_POWER.
     *
     * @return the Channel
     */
    default Channel<Long> getActiveConsumptionEnergy() {
        return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY);
    }

    /**
     * Gets the Minimum Ever Active Power.
     *
     * @return the Channel
     */
    default Channel<Integer> getMinActivePower() {
        return this.channel(ChannelId.MIN_ACTIVE_POWER);
    }

    /**
     * Gets the Maximum Ever Active Power.
     *
     * @return the Channel
     */
    default Channel<Integer> getMaxActivePower() {
        return this.channel(ChannelId.MAX_ACTIVE_POWER);
    }

    /**
     * Gets the Current in [mA].
     *
     * @return the Channel
     */
    default Channel<Integer> getCurrent() {
        return this.channel(ChannelId.CURRENT);
    }

    /*		Consolinno Channels		*/

    default Channel<Float> getPositiveActiveEnergyTotal() {
        return this.channel(ChannelId.POSITIVE_ACTIVE_ENERGY_TOTAL);
    }

    default Channel<Float> getPositiveActiveEnergyTone() {
        return this.channel(ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_ONE);
    }

    default Channel<Float> getPositiveActiveEnergyTtwo() {
        return this.channel(ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_TWO);
    }

    default Channel<Float> getNegativeActiveEnergyTotal() {
        return this.channel(ChannelId.NEGATIVE_ACTIVE_ENERGY_TOTAL);
    }

    default Channel<Float> getNegativeActiveEnergyTone() {
        return this.channel(ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_ONE);
    }

    default Channel<Float> getNegativeActiveEnergyTtwo() {
        return this.channel(ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_TWO);
    }

    default Channel<Float> getActiveInstantaneousPower() {
        return this.channel(ChannelId.ELECTRICITY_EFFECTIVE_VALUE);
    }

    default Channel<Float> getInstantVoltPhaseOne() {
        return this.channel(ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_ONE);
    }

    default Channel<Float> getInstantVoltPhaseTwo() {
        return this.channel(ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_TWO);
    }

    default Channel<Float> getInstantVoltPhaseThree() {
        return this.channel(ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_THREE);
    }

    default Channel<Float> getInstantAmperePhaseOne() {
        return this.channel(ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_ONE);
    }

    default Channel<Float> getInstantAmperePhaseTwo() {
        return this.channel(ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_TWO);
    }

    default Channel<Float> getInstantAmperePhaseThree() {
        return this.channel(ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_THREE);
    }

    default Channel<Float> getPhaseAngleOne() {
        return this.channel(ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_ONE);
    }

    default Channel<Float> getPhaseAngleTwo() {
        return this.channel(ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWO);
    }

    default Channel<Float> getPhaseAngleFour() {
        return this.channel(ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FOUR);
    }

    default Channel<Float> getPhaseAngleFifteen() {
        return this.channel(ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FIFTEEN);
    }

    default Channel<Float> getPhaseAngleTwentySix() {
        return this.channel(ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWENTY_SIX);
    }

    default Channel<Float> getFrequencyMomentaryValue() {
        return this.channel(ChannelId.FREQUENCY_MOMENTARY_VALUE_TOTAL);
    }

    default Channel<Float> getEnergyUsageLastDay() {
        return this.channel(ChannelId.ENERGY_USAGE_LAST_DAY);
    }

    default Channel<Float> getEnergyUsageLastWeek() {
        return this.channel(ChannelId.ENERGY_USAGE_LAST_WEEK);
    }

    default Channel<Float> getEnergyUsageLastMonth() {
        return this.channel(ChannelId.ENERGY_USAGE_LAST_MONTH);
    }

    default Channel<Float> getEnergyUsageLastYear() {
        return this.channel(ChannelId.ENERGY_USAGE_LAST_YEAR);
    }

    default Channel<Float> getEnergyUsageLastReset() {
        return this.channel(ChannelId.ENERGY_USAGE_SINCE_LAST_RESET);
    }

    default Channel<Float> getGeneralUsage() {
        return this.channel(ChannelId.GENERAL_USAGE);
    }

    default Channel<String> getProductionNumber() {
        return this.channel(ChannelId.PRODUCTION_NUMBER);
    }

    default Channel<Float> getCheckSum() {
        return this.channel(ChannelId.CHECKSUM);
    }

    default Channel<String> getInternalError() {
        return this.channel(ChannelId.INTERNAL_ERROR);
    }

}
