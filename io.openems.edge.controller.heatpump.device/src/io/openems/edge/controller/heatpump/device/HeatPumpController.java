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

    HeatPump heatpump;

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

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        if (hRefMax < hRefMin) {
            System.out.println("Attention RefMax < HRef Min! Cannot Execute Controller Logic");

        } else {
            this.heatpump.setConstRefMinH().setNextWriteValue(this.hRefMin);
            this.heatpump.setConstRefMaxH().setNextWriteValue(this.hRefMax);
            double result = (range * rRem / 100);
            this.heatpump.setRefRem().setNextWriteValue(Math.floor(result));
            //for REST
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
