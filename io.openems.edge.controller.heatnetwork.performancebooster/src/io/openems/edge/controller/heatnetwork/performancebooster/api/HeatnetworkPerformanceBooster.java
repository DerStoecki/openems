package io.openems.edge.controller.heatnetwork.performancebooster.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface HeatnetworkPerformanceBooster extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * SetPoint Temperature When the Controller should Activate.
         *
         * <ul>
         * <li>Interface: HeatnetworkPerformanceBooster
         * <li>Type: Integer
         * <li> Unit: Dezidegree Celsius
         * </ul>
         */
        SET_POINT_TEMPERATURE_ACTIVATE(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.DEZIDEGREE_CELSIUS).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                }
        )),
        /**
         * SetPoint Valve Percent Standard. This Percent is standard configuration if no errors occurred but heat demand present.
         *
         * <ul>
         * <li>Interface: HeatnetworkPerformanceBooster
         * <li>Type: Integer
         * <li> Unit: Percent
         * </ul>
         */
        SET_POINT_VALVE_PERCENT_STANDARD(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        /**
         * SetPoint Valve Percent Addition. This Percent is added to standard configuration if errors occurred and heat demand present.
         *
         * <ul>
         * <li>Interface: HeatnetworkPerformanceBooster
         * <li>Type: Integer
         * <li> Unit: Percent
         * </ul>
         */
        SET_POINT_VALVE_PERCENT_ADDITION(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        /**
         * SetPoint Heater Percent Standard. This Percent is standard configuration if no errors occurred but heat demand present.
         *
         * <ul>
         * <li>Interface: HeatnetworkPerformanceBooster
         * <li>Type: Integer
         * <li> Unit: Percent
         * </ul>
         */
        SET_POINT_HEATER_PERCENT_STANDARD(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                })),
        /**
         * SetPoint Heater Percent Addition. This Percent is added to standard configuration if errors occurred and heat demand present.
         *
         * <ul>
         * <li>Interface: HeatnetworkPerformanceBooster
         * <li>Type: Integer
         * <li> Unit: Percent
         * </ul>
         */
        SET_POINT_HEATER_PERCENT_ADDITION(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).unit(Unit.PERCENT).onInit(
                channel -> {
                    ((IntegerWriteChannel) channel).onSetNextWrite(channel::setNextValue);
                }));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }


    }

    default WriteChannel<Integer> temperatureSetPoint() {
        return this.channel(ChannelId.SET_POINT_TEMPERATURE_ACTIVATE);
    }

    default WriteChannel<Integer> valveSetPointStandard() {
        return this.channel(ChannelId.SET_POINT_VALVE_PERCENT_STANDARD);
    }

    default WriteChannel<Integer> valveSetPointAddition() {
        return this.channel(ChannelId.SET_POINT_VALVE_PERCENT_ADDITION);
    }

    default WriteChannel<Integer> heaterSetPointStandard() {
        return this.channel(ChannelId.SET_POINT_HEATER_PERCENT_STANDARD);
    }

    default WriteChannel<Integer> heaterSetPointAddition() {
        return this.channel(ChannelId.SET_POINT_HEATER_PERCENT_ADDITION);
    }

}
