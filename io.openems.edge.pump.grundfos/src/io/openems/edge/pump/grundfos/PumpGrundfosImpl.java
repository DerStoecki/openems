package io.openems.edge.pump.grundfos;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.bridge.genibus.api.PumpDevice;
import io.openems.edge.bridge.genibus.api.task.PumpReadTask8bit;
import io.openems.edge.bridge.genibus.api.task.PumpReadTaskASCII;
import io.openems.edge.bridge.genibus.api.task.PumpWriteTask16bitOrMore;
import io.openems.edge.bridge.genibus.api.task.PumpWriteTask8bit;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.pump.grundfos.api.PumpGrundfosChannels;
import io.openems.edge.pump.grundfos.api.PumpType;
import io.openems.edge.pump.grundfos.api.WarnBits;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import java.util.List;


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
    private WarnBits warnBits;


    public PumpGrundfosImpl() {
        super(OpenemsComponent.ChannelId.values(), PumpGrundfosChannels.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());

        allocatePumpType(config.pumpType());
        createTaskList(super.id(), config.pumpAddress());
        //genibus.addDevice(super.id(), config.pumpAddress());
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

    }

    private void allocatePumpType(String pumpType) {
        switch (pumpType) {
            case "Magna3":
                this.pumpType = PumpType.MAGNA_3;
                //createMagna3Tasks();
                break;
        }
    }

    /** Creates a PumpDevice object containing all the tasks the GENIbus should send to this device. The PumpDevice is
     * then added to the GENIbus bridge.
     *
     * @param deviceId
     * @param pumpAddress
     */
    private void createTaskList(String deviceId, int pumpAddress) {

        // The variable "lowPrioTasksPerCycle" lets you tune how fast the low priority tasks are executed. A higher
        // number means faster execution, up to the same execution speed as high priority tasks.
        // The controller will execute all high and low tasks once per cycle if there is enough time. A reduced execution
        // speed of low priority tasks happens only when there is not enough time.
        // There is also priority once, which as the name implies will be executed just once.
        //
        // What does "lowPrioTasksPerCycle" actually do?
        // Commands are sent to the pump device from a task queue. Each cycle, all the high tasks are added to the queue,
        // plus the amount "lowPrioTasksPerCycle" of low tasks. If the queue is empty before the cycle is finished, as
        // many low tasks as can still fit in this cycle will be executed as well. When all tasks have been executed
        // once this cycle, the controller will idle.
        // If there is not enough time, the execution rate of low tasks compared to high tasks depends on the total
        // amount of low tasks. The fastest execution rate (same as priority high) is reached when "lowPrioTasksPerCycle"
        // equals the total number of low tasks (value is capped at that number).
        // So if there are 10 low tasks and lowPrioTasksPerCycle=10, the low tasks behave like high tasks.
        // If in the same situation lowPrioTasksPerCycle=5, a priority low task is executed at half the rate of a
        // priority high task.
        PumpDevice pumpDevice = new PumpDevice(deviceId, pumpAddress, 20,


                // Commands. If true is sent to to conflicting channels at the same time (e.g. start and stop), the pump
                // device will act on the command that was sent first. The command list is executed from top to bottom
                // in the order they are listed here.
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getRemote(),
                        this.pumpType.getRemoteHeadClass(), setRemote()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getStart(),
                        this.pumpType.getStartHeadClass(), this.setStart()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getStop(),
                        this.pumpType.getStopHeadClass(), this.setStop()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getMinMotorCurve(),
                        this.pumpType.getMinMotorCurveHeadClass(), setMinMotorCurve()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getMaxMotorCurve(),
                        this.pumpType.getMaxMotorCurveHeadClass(), setMaxMotorCurve()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getConstFrequency(),
                        this.pumpType.getConstFrequencyHeadClass(), setConstFrequency()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getConstPressure(),
                        this.pumpType.getConstPressureHeadClass(), setConstPressure()),
                new io.openems.edge.bridge.genibus.api.task.PumpCommandsTask(this.pumpType.getAutoAdapt(),
                        this.pumpType.getAutoAdaptHeadClass(), setAutoAdapt()),

                // Read tasks priority once
                new PumpReadTask8bit(2, 0, getBufferLength(), "Standard", Priority.ONCE),

                // Read tasks priority high
                new PumpReadTask8bit(48, 2, getRefAct(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(49, 2, getRefNorm(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(90, 2, getControlSourceBits(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getPlo(), this.pumpType.getPloHeadClass(), getPowerConsumption(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getH(), this.pumpType.gethHeadClass(), getCurrentPressure(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getQ(), this.pumpType.getqHeadClass(), getCurrentPumpFlow(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.gettW(), this.pumpType.gettWHeadClass(), getPumpedWaterMediumTemperature(), "Standard", Priority.HIGH),
                // PumpReadTask has an optional channel multiplier. That is a double that is multiplied with the readout
                // value just before it is put in the channel. Here is an example of how to use this feature:
                // Apparently the unit returned by INFO is wrong. Unit type = 30 = 2*Hz, but the value is returned in Hz.
                // Could also be that the error is in the documentation and unit 30 is Hz and not Hz*2.
                new PumpReadTask8bit(this.pumpType.getfAct(), this.pumpType.getfActHeadClass(), getMotorFrequency(), "Standard", Priority.HIGH, 0.5),
                new PumpReadTask8bit(this.pumpType.getrMin(), this.pumpType.getrMinHeadClass(), getRmin(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getrMax(), this.pumpType.getrMaxHeadClass(), getRmax(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getControlMode(), this.pumpType.getControlModeHeadClass(), getActualControlModeBits(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getWarnCode(), this.pumpType.getWarnCodeHeadClass(), getWarnCode(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getAlarmCode(), this.pumpType.getAlarmCodeHeadClass(), getAlarmCode(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getWarnBits1(), this.pumpType.getWarnBits1HeadClass(), getWarnBits_1(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getWarnBits2(), this.pumpType.getWarnBits2HeadClass(), getWarnBits_2(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getWarnBits3(), this.pumpType.getWarnBits3HeadClass(), getWarnBits_3(), "Standard", Priority.HIGH),
                new PumpReadTask8bit(this.pumpType.getWarnBits4(), this.pumpType.getWarnBits4HeadClass(), getWarnBits_4(), "Standard", Priority.HIGH),

                // Read tasks priority low
                new PumpReadTask8bit(this.pumpType.gethDiff(), this.pumpType.gethDiffHeadClass(), getDiffPressureHead(), "Standard", Priority.LOW),
                new PumpReadTask8bit(this.pumpType.gettE(), this.pumpType.gettEheadClass(), getElectronicsTemperature(), "Standard", Priority.LOW),
                new PumpReadTask8bit(this.pumpType.getiMo(), this.pumpType.getImoHeadClass(), getCurrentMotor(), "Standard", Priority.LOW),

                new PumpReadTask8bit(this.pumpType.getAlarmCodePump(), this.pumpType.getAlarmCodePumpHeadClass(), getAlarmCodePump(), "Standard", Priority.LOW),
                new PumpReadTask8bit(163, 2, getAlarmLog1(), "Standard", Priority.LOW),
                new PumpReadTask8bit(164, 2, getAlarmLog2(), "Standard", Priority.LOW),
                new PumpReadTask8bit(165, 2, getAlarmLog3(), "Standard", Priority.LOW),
                new PumpReadTask8bit(166, 2, getAlarmLog4(), "Standard", Priority.LOW),
                new PumpReadTask8bit(167, 2, getAlarmLog5(), "Standard", Priority.LOW),

                // Config parameters tasks
                new PumpWriteTask8bit(this.pumpType.gethConstRefMax(), this.pumpType.gethConstRefMaxHeadClass(),
                        setConstRefMaxH(), "Standard", Priority.HIGH),
                new PumpWriteTask8bit(this.pumpType.gethConstRefMin(), this.pumpType.gethConstRefMinHeadClass(),
                        setConstRefMinH(), "Standard", Priority.HIGH),
                new PumpWriteTask16bitOrMore(2, this.pumpType.gethMaxHi(), this.pumpType.gethMaxHiHeadClass(),
                        setMaxPressure(), "Standard", Priority.HIGH),
                new PumpWriteTask16bitOrMore(2, this.pumpType.getqMaxHi(), this.pumpType.getqMaxHiHeadClass(),
                        setPumpMaxFlow(), "Standard", Priority.HIGH),

                new PumpWriteTask8bit(30, 4, setFupper(), "Standard", Priority.HIGH, 0.5),
                new PumpWriteTask8bit(31, 4, setFnom(), "Standard", Priority.HIGH, 0.5),
                new PumpWriteTask8bit(34, 4, setFmin(), "Standard", Priority.HIGH),
                new PumpWriteTask8bit(35, 4, setFmax(), "Standard", Priority.HIGH),

                // Sensor configuration
                new PumpWriteTask8bit(229, 4, setSensor1Func(), "Standard", Priority.LOW),
                new PumpWriteTask8bit(226, 4, setSensor1Applic(), "Standard", Priority.LOW),
                new PumpWriteTask8bit(208, 4, setSensor1Unit(), "Standard", Priority.LOW),
                new PumpWriteTask16bitOrMore(2, 209, 4, setSensor1Min(), "Standard", Priority.LOW),
                new PumpWriteTask16bitOrMore(2, 211, 4, setSensor1Max(), "Standard", Priority.LOW),

                new PumpReadTask8bit(127, 2, getSensorGsp(), "Standard", Priority.LOW),
                new PumpWriteTask8bit(238, 4, setSensorGspFunc(), "Standard", Priority.LOW),

                // Reference values tasks
                new PumpWriteTask8bit(this.pumpType.getRefRem(), this.pumpType.getRefRemHeadClass(),
                        setRefRem(), "Standard", Priority.HIGH),

                // Strings
                new PumpReadTaskASCII(8, 7, getProductNumber(), "Standard", Priority.ONCE),
                new PumpReadTaskASCII(9, 7, getSerialNumber(), "Standard", Priority.ONCE)
        );
        genibus.addDevice(pumpDevice);
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

    /*
    private void createMagna3Tasks() {
        // foreach Channel create Task
        // ------------


        ///////////////READ TASK/////////////////


        ///////////////WRITE TASK/////////////////
        ///////////////CONFIG PARAMS/////////////////



        this.genibus.addTask(super.id(), new PumpWriteTask(this.pumpType.getDeltaH(),
                this.pumpType.getDeltaHheadClass(), setPressureDelta(), "Standard"));


        ///////////////REFERENCE VALUES/////////////////


        ///////////////FREQUENCY VALUES/////////////////
        //this.genibus.addTask(super.id(), new PumpWriteTask(30,
        //        4, setFupper(), "Standard"));


    }
    */

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

        // Parse warn messages and put them all in one channel.
        StringBuilder allErrors = new StringBuilder();
        List<String> errorValue;
        if (getWarnBits_1().value().isDefined()) {
            int data = (int)Math.round(getWarnBits_1().value().get());
            errorValue = this.warnBits.getErrorBits1();
            for (int x = 0; x < 8; x++) {
                if ((data & (1 << x)) == (1 << x)) {
                    allErrors.append(errorValue.get(x));
                }
            }
        }
        if (getWarnBits_2().value().isDefined()) {
            int data = (int)Math.round(getWarnBits_2().value().get());
            errorValue = this.warnBits.getErrorBits2();
            for (int x = 0; x < 8; x++) {
                if ((data & (1 << x)) == (1 << x)) {
                    allErrors.append(errorValue.get(x));
                }
            }
        }
        if (getWarnBits_3().value().isDefined()) {
            int data = (int)Math.round(getWarnBits_3().value().get());
            errorValue = this.warnBits.getErrorBits3();
            for (int x = 0; x < 8; x++) {
                if ((data & (1 << x)) == (1 << x)) {
                    allErrors.append(errorValue.get(x));
                }
            }
        }
        if (getWarnBits_4().value().isDefined()) {
            int data = (int)Math.round(getWarnBits_4().value().get());
            errorValue = this.warnBits.getErrorBits4();
            for (int x = 0; x < 8; x++) {
                if ((data & (1 << x)) == (1 << x)) {
                    allErrors.append(errorValue.get(x));
                }
            }
        }
        getWarnMessage().setNextValue(allErrors);

    }
}
