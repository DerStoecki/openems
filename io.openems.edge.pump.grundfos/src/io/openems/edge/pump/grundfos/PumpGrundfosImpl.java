package io.openems.edge.pump.grundfos;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.pump.grundfos.api.PumpGrundfosChannels;
import io.openems.edge.pump.grundfos.api.PumpType;
import io.openems.edge.pump.grundfos.task.PumpCommandsTask;
import io.openems.edge.pump.grundfos.task.PumpReadTask;
import io.openems.edge.pump.grundfos.task.PumpWarnBitsTask;
import io.openems.edge.pump.grundfos.task.PumpWriteTask;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "PumpGrundfos",
        immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        property = { //
                EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
        })
public class PumpGrundfosImpl extends AbstractOpenemsComponent implements OpenemsComponent, PumpGrundfosChannels, EventHandler {


    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    Genibus genibus;

    @Reference
    ComponentManager cpm;

    private PumpType pumpType;


    public PumpGrundfosImpl() {
        super(OpenemsComponent.ChannelId.values(), PumpGrundfosChannels.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        genibus.addDevice(super.id(), config.pumpAddress());
        try {
            //default commands, can be changed via REST
            this.setRemote().setNextWriteValue(false);
            this.setStart().setNextWriteValue(false);
            this.setStop().setNextWriteValue(false);
            this.setMinMotorCurve().setNextWriteValue(false);
            this.setMaxMotorCurve().setNextWriteValue(false);
            this.setConstFrequency().setNextWriteValue(false);
            this.setConstPressure().setNextWriteValue(false);
            this.setAutoAdapt().setNextWriteValue(false);
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }


        allocatePumpType(config.pumpType());


    }

    private void allocatePumpType(String pumpType) {
        switch (pumpType) {
            case "Magna3":
                this.pumpType = PumpType.MAGNA_3;
                createMagna3Tasks();
                break;
        }
    }

    /**
     * Creates all Tasks needed for the Magna 3. They'll be added to the GeniBusBridge Tasks.
     * <p>
     * Commands: can either be set or get Information (not recommended).
     * Commands can be set via boolean.
     * Read Tasks == Measured Data.
     * Measured Data: Get Data or Information. Further Information to Channels in Class: PumpGrundfosChannels.
     * Write Task:
     * Config Params and Reference Values: Get Data, Set, Information.
     *
     *
     * </p>
     */

    private void createMagna3Tasks() {
        // foreach Channel create Task
        // ------------
        // With the current Genibus implementation, the telegram length is limited. Only a limited amount of commands
        // can be sent. That is why a lot of tasks are disabled.

        ///////////////COMMANDS/////////////////

        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getRemote(),
                this.pumpType.getRemoteHeadClass(), setRemote()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getStart(),
                this.pumpType.getStartHeadClass(), this.setStart()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getStop(),
                this.pumpType.getStopHeadClass(), this.setStop()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getMinMotorCurve(),
                this.pumpType.getMinMotorCurveHeadClass(), setMinMotorCurve()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getMaxMotorCurve(),
                this.pumpType.getMaxMotorCurveHeadClass(), setMaxMotorCurve()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getConstFrequency(),
                this.pumpType.getConstFrequencyHeadClass(), setConstFrequency()));

        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getConstPressure(),
                this.pumpType.getConstPressureHeadClass(), setConstPressure()));
        this.genibus.addTask(super.id(), new PumpCommandsTask(this.pumpType.getAutoAdapt(),
                this.pumpType.getAutoAdaptHeadClass(), setAutoAdapt()));

        ///////////////READ TASK/////////////////
        this.genibus.addTask(super.id(), new PumpReadTask(48,
                2, getRefAct(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(90,
                2, getControlSourceBits(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.gethDiff(),
        //        this.pumpType.gethDiffHeadClass(), getDiffPressureHead(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.gettE(),
        //        this.pumpType.gettEheadClass(), getElectronicsTemperature(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getiMo(),
        //        this.pumpType.getImoHeadClass(), getCurrentMotor(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getPlo(),
                this.pumpType.getPloHeadClass(), getPowerConsumption(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getH(),
                this.pumpType.gethHeadClass(), getCurrentPressure(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getQ(),
                this.pumpType.getqHeadClass(), getCurrentPumpFlow(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.gettW(),
                this.pumpType.gettWHeadClass(), getPumpedWaterMediumTemperature(), "Standard"));

        // Frequency might have wrong unit. Magna3 output is Hz, but unit type = 30 = 2*Hz.
        // Temporary fix is change unit type 30 from 2*Hz -> Hz.
        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getfAct(),
                this.pumpType.getfActHeadClass(), getMotorFrequency(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getControlMode(),
                this.pumpType.getControlModeHeadClass(), getActualControlModeBits(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getAlarmCodePump(),
        //        this.pumpType.getAlarmCodePumpHeadClass(), getAlarmCodePump(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getWarnCode(),
                this.pumpType.getWarnCodeHeadClass(), getWarnCode(), "Standard"));

        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getAlarmCode(),
                this.pumpType.getAlarmCodeHeadClass(), getAlarmCode(), "Standard"));
        ///////////////WARN BITS/////////////////
        this.genibus.addTask(super.id(), new PumpWarnBitsTask(this.pumpType.getWarnBits1(),
                this.pumpType.getWarnBits1HeadClass(), getWarnBits_1(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpWarnBitsTask(this.pumpType.getWarnBits2(),
                this.pumpType.getWarnBits2HeadClass(), getWarnBits_2(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpWarnBitsTask(this.pumpType.getWarnBits3(),
                this.pumpType.getWarnBits3HeadClass(), getWarnBits_3(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpWarnBitsTask(this.pumpType.getWarnBits4(),
                this.pumpType.getWarnBits4HeadClass(), getWarnBits_4(), "Magna3"));

        /*
        this.genibus.addTask(super.id(), new PumpReadTask(163,
                2, getAlarmLog1(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpReadTask(164,
                2, getAlarmLog2(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpReadTask(165,
                2, getAlarmLog3(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpReadTask(166,
                2, getAlarmLog4(), "Magna3"));

        this.genibus.addTask(super.id(), new PumpReadTask(167,
                2, getAlarmLog5(), "Magna3"));
        */
        ///////////////REFERENCE VALUES/////////////////
        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getrMin(),
                this.pumpType.getrMinHeadClass(), getRmin(), "Standard"));
        this.genibus.addTask(super.id(), new PumpReadTask(this.pumpType.getrMax(),
                this.pumpType.getrMaxHeadClass(), getRmax(), "Standard"));

        ///////////////WRITE TASK/////////////////
        ///////////////CONFIG PARAMS/////////////////
        //this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.getqMaxHi(),
        //        this.pumpType.getqMaxHiHeadClass(), setPumpFlowHi(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.getqMaxLo(),
        //        this.pumpType.getqMaxLowClass(), setPumpFlowLo(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.getDeltaH(),
        //        this.pumpType.getDeltaHheadClass(), setPressureDelta(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.gethMaxHi(),
        //        this.pumpType.gethMaxHiHeadClass(), setMaxPressureHi(), "Standard"));

        //this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.gethMaxLo(),
        //        this.pumpType.gethMaxLoHeadClass(), setMaxPressureLo(), "Standard"));

        this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.gethConstRefMax(),
                this.pumpType.gethConstRefMaxHeadClass(), setConstRefMaxH(), "Standard"));
        this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.gethConstRefMin(),
                this.pumpType.gethConstRefMinHeadClass(), setConstRefMinH(), "Standard"));

        ///////////////REFERENCE VALUES/////////////////

        this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.getRefRem(),
                this.pumpType.getRefRemHeadClass(), setRefRem(), "Standard"));

        ///////////////FREQUENCY VALUES/////////////////
        //this.genibus.addTask(super.id(), new PumpWriteTask(30,
        //        4, setFupper(), "Standard"));


    }

    @Deactivate
    public void deactivate() {
        genibus.removeDevice(super.id());
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

    // Fill channels with data that are not directly read from the Genibus.
    private void updateChannels() {

        // Parse ActualControlMode value to an enum.
        if (getActualControlModeBits().value().isDefined()) {
            int controlModeValue = (int)Math.round(getActualControlModeBits().value().get());
            getActualControlMode().setNextValue(controlModeValue);
        }

        // Parse ControlSource value to a string.
        if (getControlSourceBits().value().isDefined()) {
            int controlSourceBits = (int)Math.round(getControlSourceBits().value().get());
            int priorityBits = controlSourceBits & 0b1111;
            int activeSourceBits = controlSourceBits >> 4;
            String source;
            switch (activeSourceBits) {
                case 1:
                    source = "Panel";
                    break;
                case 2:
                    source = "Network (e.g. GENIbus)";
                    break;
                case 3:
                    source = "Handheld device (e.g. GENIlink/GENIair)";
                    break;
                case 4:
                    source = "External input (DI, Limit exceeded, AI Stop)";
                    break;
                case 5:
                    source = "Stop button";
                    break;
                default:
                    source = "unknown";
            }
            getControlSource().setNextValue("Command source: " + source + ", priority: " + priorityBits);
        }

        // Put warn messages all in one channel.
        StringBuilder allErrors = new StringBuilder();
        if (getWarnBits_1().value().isDefined()) {
            allErrors.append(getWarnBits_1().value().get());
        }
        if (getWarnBits_2().value().isDefined()) {
            allErrors.append(getWarnBits_2().value().get());
        }
        if (getWarnBits_3().value().isDefined()) {
            allErrors.append(getWarnBits_3().value().get());
        }
        if (getWarnBits_4().value().isDefined()) {
            allErrors.append(getWarnBits_4().value().get());
        }
        getWarnMessage().setNextValue(allErrors);

    }
}
