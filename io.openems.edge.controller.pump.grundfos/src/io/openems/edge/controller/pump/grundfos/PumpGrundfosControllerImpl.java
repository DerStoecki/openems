package io.openems.edge.controller.pump.grundfos;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.GenibusChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.pump.grundfos.api.PumpGrundfosControllerChannels;
import io.openems.edge.pump.grundfos.api.PumpGrundfosChannels;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.PumpGrundfos")
public class PumpGrundfosControllerImpl extends AbstractOpenemsComponent implements Controller, OpenemsComponent, PumpGrundfosControllerChannels {

    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    PumpGrundfosChannels needsThisToBeActive;

    GenibusChannel genibus;


    @Reference
    ComponentManager cpm;

    private final Logger log = LoggerFactory.getLogger(PumpGrundfosControllerImpl.class);

    private double hRefMin;
    private double hRefMax;

    private double rRem;
    private double range = 254;
    private double maxPressure;

    private PumpGrundfosChannels pumpChannels;

    private boolean start;
    private boolean stop;
    private boolean remote;
    private boolean minMotorCurve;
    private boolean maxMotorCurve;
    private boolean constFrequency;
    private boolean constPressure;
    private boolean autoAdapt;

    public PumpGrundfosControllerImpl() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values(),
                PumpGrundfosControllerChannels.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.pumpId()) instanceof PumpGrundfosChannels) {
                this.pumpChannels = cpm.getComponent(config.pumpId());
            } else {
                throw new ConfigurationException("Pump not correct instance, check Id!", "Incorrect Id in Config");
            }
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }
        getMaxPressure().setNextValue(config.maxPressure());
        try {
            //was in documentation of Grundfos...Reference Values to set min and max
            setHrefMin().setNextWriteValue(config.hRefMin());
            setHrefMax().setNextWriteValue(config.hRefMax());
            setRrem().setNextWriteValue(config.rRem());
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
        String[] commands = config.commands();
        Arrays.stream(commands).forEach(string -> {
            switch (string) {
                case "Remote":
                    this.remote = true;
                    break;
                case "Start":
                    this.start = true;
                    break;
                case "Stop":
                    this.stop = true;
                    break;
                case "MinMotorCurve":
                    this.minMotorCurve = true;
                    break;
                case "MaxMotorCurve":
                    this.maxMotorCurve = true;
                    break;
                case "ConstFrequency":
                    this.constFrequency = true;
                    break;
                case "ConstPressure":
                    this.constPressure = true;
                    break;
                case "AutoAdapt":
                    this.autoAdapt = true;
                    break;
            }
        });
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    /**
     * Gets the Commands usually from config; or REST/JSON Request and writes ReferenceValues in channels.
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        if (this.remote) {
            this.autoAdapt = false;
        }
        this.pumpChannels.setAutoAdapt().setNextWriteValue(this.autoAdapt);
        this.pumpChannels.setStart().setNextWriteValue(this.start);
        this.pumpChannels.setStop().setNextWriteValue(this.stop);
        this.pumpChannels.setMaxMotorCurve().setNextWriteValue(this.maxMotorCurve);
        this.pumpChannels.setMinMotorCurve().setNextWriteValue(this.minMotorCurve);
        this.pumpChannels.setConstFrequency().setNextWriteValue(this.constFrequency);
        this.pumpChannels.setConstPressure().setNextWriteValue(this.constPressure);
        this.pumpChannels.setRemote().setNextWriteValue(this.remote);

        if (setHrefMin().getNextWriteValue().isPresent() && setHrefMax().getNextWriteValue().isPresent()
                && setRrem().getNextWriteValue().isPresent()) {
            if (setHrefMax().getNextWriteValue().get() < setHrefMin().getNextWriteValue().get()) {
                System.out.println("Attention RefMax < HRef Min! Cannot Execute Controller main logic");

            } else {
                double byteValueHmin = Math.round(calculateByteValue(setHrefMin().getNextWriteValue().get()));
                double byteValueHmax = Math.round(calculateByteValue(setHrefMax().getNextWriteValue().get()));
                double refRemValue = Math.round(calculateRefRem(setRrem().getNextWriteValue().get()));
                this.pumpChannels.setConstRefMinH().setNextWriteValue(byteValueHmin > 254 ? 254 : byteValueHmin);
                this.pumpChannels.setConstRefMaxH().setNextWriteValue(byteValueHmax > 254 ? 254 : byteValueHmax);
                this.pumpChannels.setRefRem().setNextWriteValue(refRemValue > 254 ? 254 : refRemValue);
            }
        }
        // if (this.genibus.getApduConfigurationParameters().getNextValue().get() != 2) {
        //     this.genibus.getApduConfigurationParameters().setNextValue(2);
        // } else {
        //     //TEST if config param is correct-->get value
        //     this.genibus.getApduConfigurationParameters().setNextValue(0);
        // }
        // if (this.genibus.getApduReferenceValues().getNextValue().get() != 2) {
        //     this.genibus.getApduReferenceValues().setNextValue(2);
        // }

        channelOutput();

    }

    private Double calculateRefRem(Double refValue) {
        if (setHrefMax().getNextWriteValue().isPresent()) {
            return ((refValue) * range) / setHrefMax().getNextWriteValue().get();
        } else {
            return 0.d;
        }
    }

    private double calculateByteValue(Double refValue) {
        return ((refValue * range) / this.getMaxPressure().getNextValue().get());
    }

    private void channelOutput() {
        this.logInfo(this.log, "--Magna3 Channels--");
        this.logInfo(this.log, "Control mode: " + pumpChannels.getActualControlMode().value().get());
        this.logInfo(this.log, "Power consumption: " + pumpChannels.getPowerConsumption().value().get());
        this.logInfo(this.log, "Pump flow: " + pumpChannels.getCurrentPumpFlow().value().get());
        this.logInfo(this.log, "Pump pressure: " + pumpChannels.getCurrentPressure().value().get());
        this.logInfo(this.log, "Pump diff. pressure head: " + pumpChannels.getDiffPressureHead().value().get());
        this.logInfo(this.log, "R min: " + pumpChannels.getRmin().value().get());
        this.logInfo(this.log, "R max: " + pumpChannels.getRmax().value().get());
        this.logInfo(this.log, "");
        this.logInfo(this.log, "Ref sent, raw: " + pumpChannels.setRefRem().getNextWriteValue().get());
        this.logInfo(this.log, "");

    }
}
