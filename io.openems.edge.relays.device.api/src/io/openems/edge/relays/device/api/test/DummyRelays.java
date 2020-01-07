package io.openems.edge.relays.device.api.test;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.relays.device.api.ActuatorRelaysChannel;

public class DummyRelays extends AbstractOpenemsComponent implements ActuatorRelaysChannel, OpenemsComponent {

    public DummyRelays(String id) {
        super(OpenemsComponent.ChannelId.values(),
                ActuatorRelaysChannel.ChannelId.values()
        );
        super.activate(null, id, "", true);

    }
}
