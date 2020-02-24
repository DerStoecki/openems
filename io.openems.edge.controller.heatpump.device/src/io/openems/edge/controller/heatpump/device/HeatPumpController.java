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
@Component(name = "HeatPumpController")
public class HeatPumpController extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

    @Reference
    ComponentManager cpm;


    private double hRefMin;
    private double hRefMax;

    private double rRem;
    private double range = 254;

    private WriteChannel<Double> hRefMinChannel;
    private WriteChannel<Double> hRefMaxChannel;
    private WriteChannel<Double> refRemChannel;

    public HeatPumpController() {
        super(Controller.ChannelId.values(), OpenemsComponent.ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.heatPumpId()) instanceof HeatPump) {
                HeatPump heatPump = cpm.getComponent(config.heatPumpId());
                this.hRefMinChannel = heatPump.setConstRefMinH();
                this.hRefMaxChannel = heatPump.setConstRefMaxH();
                this.refRemChannel = heatPump.setRefRem();
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
            this.hRefMinChannel.setNextWriteValue(this.hRefMin);
            this.hRefMaxChannel.setNextWriteValue(this.hRefMax);
            double result =  (range * rRem / 100);
            this.refRemChannel.setNextWriteValue(result);
        }



    }
}
