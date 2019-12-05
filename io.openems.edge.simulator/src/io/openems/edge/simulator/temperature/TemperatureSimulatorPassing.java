package io.openems.edge.simulator.temperature;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.simulator.datasource.api.SimulatorDatasource;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Simulator.Temperature", //
        immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
        })
public class TemperatureSimulatorPassing extends AbstractOpenemsComponent implements Thermometer, OpenemsComponent, EventHandler {

    public TemperatureSimulatorPassing() {
        super(OpenemsComponent.ChannelId.values(), Thermometer.ChannelId.values());
    }

    @Reference
    ConfigurationAdmin cm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected SimulatorDatasource datasource;


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        if (OpenemsComponent.updateReferenceFilter(cm, this.servicePid(), "datasource", config.datasource_id())) {
            return;
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.getTopic()) {
            case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
                this.updateChannels();
                break;
        }
    }

    private void updateChannels() {

        int temperature;
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "Temperature");
        this.getTemperature().setNextValue(temperature);
    }

    @Override
    public String debugLog() {
        return "T:" + this.getTemperature().value().asString();
    }
}
