package io.openems.edge.apartmentmodule.api;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.*;
import io.openems.edge.common.component.OpenemsComponent;

public interface ApartmentModuleChannel extends OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

        /**
         * External request.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: No request has occurred
         *      <li> State 1: external request has occurred
         * </ul>
         */

        HR_1_EXTERNAL_REQUEST(Doc.of(ExternalRequest.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Error.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: No error
         *      <li> State 1: Error
         * </ul>
         */

        HR_2_ERROR(Doc.of(Error.values()).accessMode(AccessMode.READ_ONLY)),

        /**
         * Communication.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: Slave is awaiting communication
         *      <li> State 1: Slave has not processed the last command
         * </ul>
         */

        HR_3_COMMUNICATION(Doc.of(Ready.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Temperature.
         * <li>
         * <li>Type: Integer
         * <li>Unit: dezidegree celsius
         * </ul>
         */

        HR_10_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEZIDEGREE_CELSIUS).accessMode(AccessMode.READ_ONLY)),

        /**
         * State of relay 1.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: Off
         *      <li> State 1: On
         * </ul>
         */

        HR_21_STATE_RELAY1(Doc.of(OnOff.values()).accessMode(AccessMode.READ_ONLY)),

        /**
         * State of relay 2.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: Off
         *      <li> State 1: On
         * </ul>
         */

        HR_22_STATE_RELAY2(Doc.of(OnOff.values()).accessMode(AccessMode.READ_ONLY)),

        /**
         * Command for relay 1.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: Off
         *      <li> State 1: On
         * </ul>
         */

        HR_31_COMMAND_RELAY1(Doc.of(OnOff.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Command for relay 2.
         * <ul>
         *      <li> Type: Integer
         *      <li> Possible values: 0, 1
         *      <li> State 0: Off
         *      <li> State 1: On
         * </ul>
         */

        HR_32_COMMAND_RELAY2(Doc.of(OnOff.values()).accessMode(AccessMode.READ_WRITE)),

        /**
         * Timing for relay 1.
         * <li>
         * <li>Type: Integer
         * <li>Unit: centi seconds (= seconds * 10E-2)
         * </ul>
         */

        HR_41_TIMING_RELAY1(Doc.of(OpenemsType.INTEGER).unit(Unit.CENTISECONDS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Timing for relay 2.
         * <li>
         * <li>Type: Integer
         * <li>Unit: centi seconds (= seconds * 10E-2)
         * </ul>
         */

        HR_42_TIMING_RELAY2(Doc.of(OpenemsType.INTEGER).unit(Unit.CENTISECONDS).accessMode(AccessMode.READ_WRITE)),

        /**
         * Time remaining for relay 1.
         * <li>
         * <li>Type: Integer
         * <li>Unit: centi seconds (= seconds * 10E-2)
         * </ul>
         */

        HR_51_TIME_REMAINING_RELAY1(Doc.of(OpenemsType.INTEGER).unit(Unit.CENTISECONDS).accessMode(AccessMode.READ_ONLY)),

        /**
         * Time remaining for relay 2.
         * <li>
         * <li>Type: Integer
         * <li>Unit: centi seconds (= seconds * 10E-2)
         * </ul>
         */

        HR_52_TIME_REMAINING_RELAY2(Doc.of(OpenemsType.INTEGER).unit(Unit.CENTISECONDS).accessMode(AccessMode.READ_ONLY));


        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }


    /**
     * External request.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: No request has occurred
     *      <li> State 1: external request has occurred
     * </ul>
     */

    default WriteChannel<Integer> getSetExternalRequest() { return this.channel(ChannelId.HR_1_EXTERNAL_REQUEST); }

    /**
     * Error.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: No error
     *      <li> State 1: Error
     * </ul>
     */

    default Channel<Integer> getError() { return this.channel(ChannelId.HR_2_ERROR); }

    /**
     * Communication.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: Slave is awaiting communication
     *      <li> State 1: Slave has not processed the last command
     * </ul>
     */

    default WriteChannel<Integer> getSetCommunication() { return this.channel(ChannelId.HR_3_COMMUNICATION); }

    /**
     * Temperature.
     * <li>
     * <li>Type: Integer
     * <li>Unit: dezidegree celsius
     * </ul>
     */

    default Channel<Integer> getTemperature() { return this.channel(ChannelId.HR_10_TEMPERATURE); }

    /**
     * State of relay 1.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: Off
     *      <li> State 1: On
     * </ul>
     */

    default Channel<Integer> getStateRelay1() { return this.channel(ChannelId.HR_21_STATE_RELAY1); }

    /**
     * State of relay 2.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: Off
     *      <li> State 1: On
     * </ul>
     */

    default Channel<Integer> getStateRelay2() { return this.channel(ChannelId.HR_22_STATE_RELAY2); }

    /**
     * Command for relay 1.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: Off
     *      <li> State 1: On
     * </ul>
     */

    default WriteChannel<Integer> getSetCommandRelay1() { return this.channel(ChannelId.HR_31_COMMAND_RELAY1); }

    /**
     * Command for relay 2.
     * <ul>
     *      <li> Type: Integer
     *      <li> Possible values: 0, 1
     *      <li> State 0: Off
     *      <li> State 1: On
     * </ul>
     */

    default WriteChannel<Integer> getSetCommandRelay2() { return this.channel(ChannelId.HR_32_COMMAND_RELAY2); }

    /**
     * Timing for relay 1.
     * <li>
     * <li>Type: Integer
     * <li>Unit: centi seconds (= seconds * 10E-2)
     * </ul>
     */

    default WriteChannel<Integer> getSetTimingRelay1() { return this.channel(ChannelId.HR_41_TIMING_RELAY1); }

    /**
     * Timing for relay 2.
     * <li>
     * <li>Type: Integer
     * <li>Unit: centi seconds (= seconds * 10E-2)
     * </ul>
     */

    default WriteChannel<Integer> getSetTimingRelay2() { return this.channel(ChannelId.HR_42_TIMING_RELAY2); }

    /**
     * Time remaining for relay 1.
     * <li>
     * <li>Type: Integer
     * <li>Unit: centi seconds (= seconds * 10E-2)
     * </ul>
     */

    default Channel<Integer> getCountdownRelay1() { return this.channel(ChannelId.HR_51_TIME_REMAINING_RELAY1); }

    /**
     * Time remaining for relay 2.
     * <li>
     * <li>Type: Integer
     * <li>Unit: centi seconds (= seconds * 10E-2)
     * </ul>
     */

    default Channel<Integer> getCountdownRelay2() { return this.channel(ChannelId.HR_52_TIME_REMAINING_RELAY2); }

}
