package io.openems.edge.controller.heatpump.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.genibus.api.Genibus;
import io.openems.edge.bridge.genibus.api.GenibusChannel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.heatpump.device.api.HeatPump;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Arrays;


@Designate(ocd = Config.class, factory = true)
@Component(name = "ControllerHeatPump")
public class HeatPumpController extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

    @Reference(policy = ReferencePolicy.STATIC,
            policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    GenibusChannel genibus;


    @Reference
    ComponentManager cpm;

    private double hRefMin;
    private double hRefMax;

    private double rRem;
    private double range = 254;

    private HeatPump heatpump;

    private boolean start;
    private boolean stop;
    private boolean remote;
    private boolean minMotorCurve;
    private boolean maxMotorCurve;
    private boolean constFrequency;
    private boolean constPressure;
    private boolean autoAdapt;

    public HeatPumpController() {
        super(OpenemsComponent.ChannelId.values(),
                Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.heatPumpId()) instanceof HeatPump) {
                this.heatpump = cpm.getComponent(config.heatPumpId());
            } else {
                throw new ConfigurationException("HeatPump not correct instance, check Id!", "Incorrect Id in Config");
            }
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }
        this.hRefMax = config.hRefMax();
        this.hRefMin = config.hRefMin();
        this.rRem = config.rRem();

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


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {
        if (this.remote) {
            this.autoAdapt = false;
        }
        this.heatpump.setAutoAdapt().setNextWriteValue(this.autoAdapt);
        this.heatpump.setStart().setNextWriteValue(this.start);
        this.heatpump.setStop().setNextWriteValue(this.stop);
        this.heatpump.setMaxMotorCurve().setNextWriteValue(this.maxMotorCurve);
        this.heatpump.setMinMotorCurve().setNextWriteValue(this.minMotorCurve);
        this.heatpump.setConstFrequency().setNextWriteValue(this.constFrequency);
        this.heatpump.setConstPressure().setNextWriteValue(this.constPressure);
        this.heatpump.setRemote().setNextWriteValue(this.remote);


        if (hRefMax < hRefMin) {
            System.out.println("Attention RefMax < HRef Min! Cannot Execute Controller main logic");

        } else {
            //setNextValue is for reading from REST Client
            this.heatpump.setConstRefMinH().setNextWriteValue(this.hRefMin);
            this.heatpump.setConstRefMinH().setNextValue(this.hRefMin);
            this.heatpump.setConstRefMaxH().setNextWriteValue(this.hRefMax);
            this.heatpump.setConstRefMaxH().setNextValue(this.hRefMax);
            double result = (range * rRem / 100);
            this.heatpump.setRefRem().setNextWriteValue(Math.floor(result));
            this.heatpump.setRefRem().setNextValue(Math.floor(result));
        }
        if (this.genibus.getApduConfigurationParameters().getNextValue().get() != 2) {
            this.genibus.getApduConfigurationParameters().setNextValue(2);
        }
        if (this.genibus.getApduReferenceValues().getNextValue().get() != 2) {
            this.genibus.getApduReferenceValues().setNextValue(2);
        }

    }
}
