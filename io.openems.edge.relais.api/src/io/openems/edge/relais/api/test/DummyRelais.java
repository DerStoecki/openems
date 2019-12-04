package io.openems.edge.relais.api.test;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.relais.api.ActuatorRelaisChannel;

public class DummyRelais extends AbstractOpenemsComponent implements ActuatorRelaisChannel, OpenemsComponent {

    public DummyRelais(String id){

        super(OpenemsComponent.ChannelId.values(),
                ActuatorRelaisChannel.ChannelId.values()
                );
        super.activate(null, id, "", true);

    }
}
