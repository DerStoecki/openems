package io.openems.edge.heatpump.smartgrid.generalized.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;


/**
 * A generalized interface for smart grid operation of a heat pump.
 * Contains the most important functions shared by all heat pumps, allowing a vendor agnostic implementation.
 * Vendor specific interfaces should extend this interface.
 *
 */

public interface HeatpumpSmartGridGeneralizedChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {


        /**
         * Smart Grid state of the heat pump. Use "setNextWriteValue()" to set the value, "value().get()" will return
         * the state of the heat pump.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0 ... 4
         *      <li> State 0: Off
         *      <li> State 1: Smart Grid Low
         *      <li> State 2: Standard
         *      <li> State 3: Smart Grid High
         * </ul>
         */

        SMART_GRID_STATE(Doc.of(SmartGridState.values()).accessMode(AccessMode.READ_WRITE)),


        /**
         * Is running.
         * <ul>
         *      <li> True if the heat pump is currently running.
         *      <li> Type: Boolean
         * </ul>
         */

        RUNNING(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY)),


        /**
         * Is ready.
         * <ul>
         *      <li> True if the heat pump is ready to run or already running.
         *      <li> Type: Boolean
         * </ul>
         */

        READY(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY)),


        /**
         * Error channel.
         * <ul>
         *      <li> True when there is no Error.
         *      <li> Type: Boolean
         * </ul>
         */

        NO_ERROR(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_ONLY));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    /**
     * Smart Grid state of the heat pump. Use "setNextWriteValue()" to set the state, "value().get()" will return
     * the state of the heat pump.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0 ... 4
     *      <li> State 0: Off
     *      <li> State 1: Smart Grid Low
     *      <li> State 2: Standard
     *      <li> State 3: Smart Grid High
     * </ul>
     *
     * @return the Channel
     */
    default WriteChannel<Integer> getsetSmartGridState() {
        return this.channel(ChannelId.SMART_GRID_STATE);
    }

    /**
     * Channel to track if the heat pump is running.
     *
     * @return the Channel
     */
    default Channel<Boolean> isRunning() { return this.channel(ChannelId.RUNNING); }

    /**
     *
     * Channel to track if the heat pump is ready to run. Already running counts as ready.
     *
     * @return the Channel
     */
    default Channel<Boolean> isReady() { return this.channel(ChannelId.READY); }

    /**
     * Is true when no error has occurred.
     *
     * @return the Channel
     */
    default Channel<Boolean> noError() { return this.channel(ChannelId.NO_ERROR); }

}
