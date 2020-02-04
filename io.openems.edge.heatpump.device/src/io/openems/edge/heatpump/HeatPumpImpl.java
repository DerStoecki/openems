package io.openems.edge.heatpump;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.heatpump.device.api.HeatPump;
import io.openems.edge.heatpump.task.HeatPumpTask;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "io.openems.edge.heatpump")
public class HeatPumpImpl extends AbstractOpenemsComponent implements OpenemsComponent, HeatPump {


    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    Genibus genibus;

    @Reference
    ComponentManager cpm;

    private HeatPumpType heatPumpType;


    public HeatPumpImpl() {
        super(OpenemsComponent.ChannelId.values(), HeatPump.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        allocateHeatPumpType(config.pumpType());
        genibus.addDevice(super.id(), config.heatPumpAddress());
        try {
            this.getPressure().setNextWriteValue(config.pumpStartPressure());
            this.getMaxPressure().setNextWriteValue(config.maxPressure());
            this.getMinPressure().setNextWriteValue(config.minPressure());
            //to read from rest client etc
            //this.getPressure().setNextValue(config.pumpStartPressure());
            //this.getMaxPressure().setNextValue(config.maxPressure());
            //this.getMinPressure().setNextValue(config.minPressure());
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        createTasks();


    }

    private void allocateHeatPumpType(String pumpType) {
        switch (pumpType) {
            case "MAGNA3":
                this.heatPumpType = HeatPumpType.MAGNA_3;
                break;
        }
    }

    private void createTasks() {
        //for unique id of stuff
        int idCounter = 0;

        //TODO For each Channel create task and add to Bridge.

        //TODO Address from PumpType ---> e.g. MAGNA_3 ;
        //TODO Header number from pumpType --> MAGNA_3;
        this.genibus.addTask(super.id(), idCounter, new HeatPumpTask(0x20, 2, getPressure(), true));
        //idCounter++;


    }

    @Deactivate
    public void deactivate() {
        genibus.removeTask(super.id());
        super.deactivate();
    }

}
