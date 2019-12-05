package io.openems.edge.simulator.temperature;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.simulator.datasource.api.SimulatorDatasource;
import io.openems.edge.thermometer.api.Thermometer;
import io.openems.edge.thermometer.api.test.DummyThermometer;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Simulator.Temperature.Passing", //
        immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
                "type=Thermometer" //
        })
public class TemperatureSimulatorPassing extends AbstractOpenemsComponent implements Thermometer, OpenemsComponent, EventHandler {

    private DummyThermometer pF;
    private DummyThermometer pR;
    private DummyThermometer sF;
    private DummyThermometer sR;
    private DummyThermometer goal;

    public TemperatureSimulatorPassing() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Reference
    ConfigurationAdmin cm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected SimulatorDatasource datasource;


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        try {
            pF = new DummyThermometer(config.pf_Sensor());
            pR = new DummyThermometer(config.pr_Sensor());
            sF = new DummyThermometer(config.sf_Sensor());
            sR = new DummyThermometer(config.sr_Sensor());
            goal = new DummyThermometer(config.goal_Sensor());

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "primaryForward");
        pF.getTemperature().setNextValue(temperature);
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "primaryRewind");
        pR.getTemperature().setNextValue(temperature);
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "secundaryForward");
        sF.getTemperature().setNextValue(temperature);
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "secundaryRewind");
        sR.getTemperature().setNextValue(temperature);
        temperature = this.datasource.getValue(OpenemsType.INTEGER, "goal");
        goal.getTemperature().setNextValue(temperature);
    }


}
