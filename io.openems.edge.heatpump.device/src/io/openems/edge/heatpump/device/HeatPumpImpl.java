package io.openems.edge.heatpump.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.heatpump.device.api.HeatPump;
import io.openems.edge.heatpump.device.tempapi.HeatPumpType;
import io.openems.edge.heatpump.device.task.HeatPumpCommandsTask;
import io.openems.edge.heatpump.device.task.HeatPumpReadTask;
import io.openems.edge.heatpump.device.task.HeatPumpWarnBitsTask;
import io.openems.edge.heatpump.device.task.HeatPumpWriteTask;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "HeatPump")
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
            //commands
            this.setRemote().setNextWriteValue(true);
            this.setStart().setNextWriteValue(true);
            this.setStop().setNextWriteValue(false);
            this.setMinMotorCurve().setNextWriteValue(false);
            this.setMaxMotorCurve().setNextWriteValue(false);
            this.setConstFrequency().setNextWriteValue(false);
            this.setConstPressure().setNextWriteValue(true);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }


        allocateHeatPumpType(config.pumpType());


    }

    private void allocateHeatPumpType(String pumpType) {
        switch (pumpType) {
            case "Magna3":
                this.heatPumpType = HeatPumpType.MAGNA_3;
                createMagna3Tasks();
                break;
        }
    }

    private void createMagna3Tasks() {
        //foreach Channel create Task

        //commands
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getRemote(),
                this.heatPumpType.getRemoteHeadClass(), setRemote()));
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getStart(),
                this.heatPumpType.getStartHeadClass(), this.setStart()));
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getStop(),
                this.heatPumpType.getStopHeadClass(), this.setStop()));
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getMinMotorCurve(),
                this.heatPumpType.getMinMotorCurveHeadClass(), setMinMotorCurve()));
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getMaxMotorCurve(),
                this.heatPumpType.getMaxMotorCurveHeadClass(), setMaxMotorCurve()));
        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getConstFrequency(),
                this.heatPumpType.getConstFrequencyHeadClass(), setConstFrequency()));

        this.genibus.addTask(super.id(), new HeatPumpCommandsTask(this.heatPumpType.getConstPressure(),
                this.heatPumpType.getConstPressureHeadClass(), setConstPressure()));

        //read Task
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gethDiff(),
                this.heatPumpType.gethDiffHeadClass(), getDiffPressureHead(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gettE(),
                this.heatPumpType.gettEheadClass(), getElectronicsTemperature(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getiMo(),
                this.heatPumpType.getImoHeadClass(), getCurrentMotor(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getPlo(),
                this.heatPumpType.getPloHeadClass(), getPowerConsumption(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getH(),
                this.heatPumpType.gethHeadClass(), getCurrentPressure(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getQ(),
                this.heatPumpType.getqHeadClass(), getCurrentPumpFlow(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.gettW(),
                this.heatPumpType.gettWHeadClass(), getPumpedWaterMediumTemperature(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getControlMode(),
                this.heatPumpType.getControlModeHeadClass(), getActualControlMode(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getAlarmCodePump(),
                this.heatPumpType.getAlarmCodePumpHeadClass(), getAlarmCodePump(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getWarnCode(),
                this.heatPumpType.getWarnCodeHeadClass(), getWarnCode(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getAlarmCode(),
                this.heatPumpType.getAlarmCodeHeadClass(), getAlarmCode(), "Standard"));
        //warnBits
        this.genibus.addTask(super.id(), new HeatPumpWarnBitsTask(this.heatPumpType.getWarnBits1(),
                this.heatPumpType.getWarnBits1HeadClass(), getWarnBits_1(), "Magna3"));

        this.genibus.addTask(super.id(), new HeatPumpWarnBitsTask(this.heatPumpType.getWarnBits2(),
                this.heatPumpType.getWarnBits2HeadClass(), getWarnBits_2(), "Magna3"));

        this.genibus.addTask(super.id(), new HeatPumpWarnBitsTask(this.heatPumpType.getWarnBits3(),
                this.heatPumpType.getWarnBits3HeadClass(), getWarnBits_3(), "Magna3"));

        this.genibus.addTask(super.id(), new HeatPumpWarnBitsTask(this.heatPumpType.getWarnBits4(),
                this.heatPumpType.getWarnBits4HeadClass(), getWarnBits_4(), "Magna3"));
        //reference Values
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getrMin(),
                this.heatPumpType.getrMinHeadClass(), getRmin(), "Standard"));
        this.genibus.addTask(super.id(), new HeatPumpReadTask(this.heatPumpType.getrMax(),
                this.heatPumpType.getrMaxHeadClass(), getRmax(), "Standard"));

        //write Task --> headClass 4 --> Config Param
        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getqMaxHi(),
                this.heatPumpType.getqMaxHiHeadClass(), setPumpFlowHi(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getqMaxLo(),
                this.heatPumpType.getqMaxLowClass(), setPumpFlowLo(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getDeltaH(),
                this.heatPumpType.getDeltaHheadClass(), setPressureDelta(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethMaxHi(),
                this.heatPumpType.gethMaxHiHeadClass(), setMaxPressure(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethMaxLo(),
                this.heatPumpType.gethMaxLoHeadClass(), setMinPressure(), "Standard"));

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethConstRefMax(),
                this.heatPumpType.gethConstRefMaxHeadClass(), setConstRefMaxH(), "Standard"));
        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.gethConstRefMin(),
                this.heatPumpType.gethConstRefMinHeadClass(), setConstRefMinH(), "Standard"));

        //write Task --> head Class 5 --> Reference Values

        this.genibus.addTask(super.id(), new HeatPumpWriteTask(this.heatPumpType.getRefRem(),
                this.heatPumpType.getRefRemHeadClass(), setRefRem(), "Standard"));

    }

    @Deactivate
    public void deactivate() {
        genibus.removeDevice(super.id());
        super.deactivate();
    }

}
