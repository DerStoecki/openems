package io.openems.edge.bridge.lmnwired.api;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.common.component.AbstractOpenemsComponent;

public abstract class AbstractOpenEmsLMNWiredComponent extends AbstractOpenemsComponent {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(AbstractOpenEmsLMNWiredComponent.class);

    protected AbstractOpenEmsLMNWiredComponent(io.openems.edge.common.channel.ChannelId[] firstInitialChannelIds,
                                               io.openems.edge.common.channel.ChannelId[]... furtherInitialChannelIds) {
        super(firstInitialChannelIds, furtherInitialChannelIds);
    }

    protected void activate(ComponentContext context, String id, String alias, boolean enabled,
                            ConfigurationAdmin cm, String service_pid) {
        super.activate(context, id, alias, enabled);
    }

}
