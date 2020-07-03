package io.openems.edge.consolinno.leaflet.mainmodule.sc16;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16IS752;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16IS752Impl;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "MainModule.Sc16",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class Sc16Is752GpioExpansion extends AbstractOpenemsComponent implements OpenemsComponent {
    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;

    private String versionId;
    private Sc16IS752 sc16;

    public Sc16Is752GpioExpansion() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.sc16 = new Sc16IS752Impl();

        this.sc16.initialize(config.spiChannel(), config.frequency(), super.id(), config.version(),
                config.interruptActivate(), config.interruptSetting(), config.interruptData());
        this.bridgeSpi.addDoubleUart(this.sc16);
    }

    @Deactivate
    public void deactivate() {
        bridgeSpi.removeDoubleUart(this.sc16);
        super.deactivate();
    }


}
