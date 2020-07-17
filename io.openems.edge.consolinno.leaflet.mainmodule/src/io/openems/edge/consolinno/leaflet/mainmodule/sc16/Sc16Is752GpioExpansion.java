package io.openems.edge.consolinno.leaflet.mainmodule.sc16;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16IS752;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16IS752Impl;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.nature.Sc16Nature;
import io.openems.edge.consolinno.leaflet.mainmodule.sc16.tasks.DoubleUartReadTaskImpl;
import io.openems.edge.consolinno.leaflet.mainmodule.sc16.tasks.DoubleUartWriteTaskImpl;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "MainModule.Sc16",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class Sc16Is752GpioExpansion extends AbstractOpenemsComponent implements OpenemsComponent, Sc16Nature {
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
                false, "0", "0");
        //this.sc16.initialize(config.spiChannel(), config.frequency(), super.id(), config.version(),
        //      config.interruptActivate(), config.interruptSetting(), config.interruptData());
        this.bridgeSpi.addDoubleUart(this.sc16);
        addTasksToSc16();

    }

    private void addTasksToSc16() throws ConfigurationException {
        this.sc16.addTask(new DoubleUartWriteTaskImpl(0, this.ledRedStatus()));
        this.sc16.addTask(new DoubleUartWriteTaskImpl(1, this.ledYellowStatus()));
        this.sc16.addTask(new DoubleUartWriteTaskImpl(2, this.ledGreenStatus()));
        this.sc16.addTask(new DoubleUartWriteTaskImpl(6, this.enableOutput()));
        this.sc16.addTask(new DoubleUartReadTaskImpl(4, this.hBus5V()));
        this.sc16.addTask(new DoubleUartReadTaskImpl(5, this.hBus24V()));
        this.sc16.addTask(new DoubleUartReadTaskImpl(7, this.outputVoltageFlag()));

    }

    @Deactivate
    public void deactivate() {
        bridgeSpi.removeDoubleUart(this.sc16);
        super.deactivate();
    }

    public Sc16IS752 getSc16() {
        return sc16;
    }

    @Override
    public String debugLog() {
        String debug = "";
        debug += this.ledRedStatus().channelId().id() + " Status: " + this.ledRedStatus().value().get() + "\n";
        debug += this.ledYellowStatus().channelId().id() + " Status: " + this.ledYellowStatus().value().get() + "\n";
        debug += this.ledGreenStatus().channelId().id() + " Status: " + this.ledGreenStatus().value().get() + "\n";
        debug += this.enableOutput().channelId().id() + " Status: " + this.enableOutput().value().get() + "\n";

        debug += this.hBus5V().channelId().id() + " Status: " + this.hBus5V().value().get() + "\n";
        debug += this.hBus24V().channelId().id() + " Status: " + this.hBus24V().value().get() + "\n";
        debug += this.outputVoltageFlag().channelId().id() + " Status: " + this.outputVoltageFlag().value().get() + "\n";

        return debug;
    }
}
