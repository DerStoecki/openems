package io.openems.edge.heatpump.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.heatpump.device.api.HeatPump;
import io.openems.edge.heatpump.device.task.HeatPumpReadTask;
import io.openems.edge.heatpump.device.task.HeatPumpWriteTask;
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

        genibus.addDevice(super.id(), config.heatPumpAddress());
        try {
            this.setPressureDelta().setNextWriteValue(config.pumpStartPressure());
            this.setMaxPressure().setNextWriteValue(config.maxPressure());
            this.setMinPressure().setNextWriteValue(config.minPressure());
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        allocateHeatPumpType(config.pumpType());



    }

    private void allocateHeatPumpType(String pumpType) {
        switch (pumpType) {
            case "MAGNA3":
                this.heatPumpType = HeatPumpType.MAGNA_3;
                createMagna3Tasks();
                break;
        }
    }

    private void createMagna3Tasks() {
        //foreach Channel create Task
        //read Task
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gethDiff(),
                this.heatPumpType.gethDiffHeadClass(), getDiffPressureHead()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gettE(),
                this.heatPumpType.gettEheadClass(), getElectronicsTemperature()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getiMo(),
                this.heatPumpType.getImoHeadClass(), getCurrentMotor()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getPlo(),
                this.heatPumpType.getPloHeadClass(), getPowerConsumption()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getH(),
                this.heatPumpType.gethHeadClass(), getCurrentPressure()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getQ(),
                this.heatPumpType.getqHeadClass(), getCurrentPumpFlow()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gettW(),
                this.heatPumpType.gettWHeadClass(), getPumpedWaterMediumTemperature()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getControlMode(),
                this.heatPumpType.getControlModeHeadClass(), getActualControlMode()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getAlarmCodePump(),
                this.heatPumpType.getAlarmCodePumpHeadClass(), getAlarmCodePump()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnCode(),
                this.heatPumpType.getWarnCodeHeadClass(), getWarnCode()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getAlarmCode(),
                this.heatPumpType.getAlarmCodeHeadClass(), getAlarmCode()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnBits1(),
                this.heatPumpType.getWarnBits1HeadClass(), getWarnBits_1()));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnBits2(),
                this.heatPumpType.getWarnBits2HeadClass(), getWarnBits_2()));
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnBits3(),
                this.heatPumpType.getWarnBits3HeadClass(), getWarnBits_3()));
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnBits4(),
                this.heatPumpType.getWarnBits4HeadClass(), getWarnBits_4()));

        //write Task
        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getqMaxHi(),
                this.heatPumpType.getqMaxHiHeadClass(), setPumpFlowHi()));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getqMaxLo(),
                this.heatPumpType.getqMaxLowClass(), setPumpFlowLo()));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getDeltaH(),
                this.heatPumpType.getDeltaHheadClass(), setPressureDelta()));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethMaxHi(),
                this.heatPumpType.gethMaxHiHeadClass(), setMaxPressure()));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethMaxLo(),
                this.heatPumpType.gethMaxLoHeadClass(), setMinPressure()));
    }

    @Deactivate
    public void deactivate() {
        genibus.removeDevice(super.id());
        super.deactivate();
    }

}
