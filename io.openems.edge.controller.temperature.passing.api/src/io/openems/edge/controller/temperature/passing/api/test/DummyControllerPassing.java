package io.openems.edge.controller.temperature.passing.api.test;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.temperature.passing.api.ControllerPassingChannel;

public class DummyControllerPassing extends AbstractOpenemsComponent implements OpenemsComponent, ControllerPassingChannel {

    public DummyControllerPassing(String id) {
        super(
                OpenemsComponent.ChannelId.values(),
                ControllerPassingChannel.ChannelId.values()

        );
        for (Channel<?> channel : this.channels()) {
            channel.nextProcessImage();
        }
        super.activate(null, id, "", true);
    }
}
