package io.openems.edge.consolinno.leaflet.maindevice.api.doubleuart;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.PcaDevice;

public interface DoubleUartDevice extends OpenemsComponent {

    enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        /**
         * OnOff.
         *
         * <ul>
         * <li>Interface: PcaDevice
         * <li>Type: Boolean
         * </ul>
         */
        ON_OFF(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)), //
        IS_INPUT(Doc.of(OpenemsType.BOOLEAN));
        private final Doc doc;


        ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }

    }

    default WriteChannel<Boolean> getOnOff() {
        return this.channel(PcaDevice.ChannelId.ON_OFF);
    }
}
