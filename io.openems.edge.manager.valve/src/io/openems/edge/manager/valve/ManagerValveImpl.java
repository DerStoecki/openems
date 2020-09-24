package io.openems.edge.manager.valve;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.bridge.i2c.api.I2cBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.i2c.mcp.api.McpChannelRegister;
import io.openems.edge.manager.valve.api.ManagerValve;
import io.openems.edge.temperature.passing.valve.api.Valve;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Consolinno.Manager.Valve",
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ManagerValveImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller, ManagerValve {
    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    I2cBridge i2cBridge;


    private Map<String, Valve> valves = new ConcurrentHashMap<>();

    public ManagerValveImpl() {
        super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values());
    }


    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void addValve(String id, Valve valve) {
        this.valves.put(id, valve);

    }

    @Override
    public void removeValve(String id) {
        this.valves.remove(id);
    }

    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        valves.values().forEach(valve -> {

            if (valve.getValveReset().value().isDefined() && valve.getValveReset().value().get()) {
                valve.reset();
            }
            if (!valve.powerLevelReached()) {
                valve.updatePowerLevel();
            }
            valve.readyToChange();
        });
        i2cBridge.getMcpList().forEach(McpChannelRegister::shift);
    }
}
