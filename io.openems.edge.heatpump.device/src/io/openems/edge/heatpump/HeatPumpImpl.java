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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "io.openems.edge.heatpump")
public class HeatPumpImpl extends AbstractOpenemsComponent implements OpenemsComponent, HeatPump {

    @Reference
    ComponentManager cpm;

    public HeatPumpImpl() {
        super(OpenemsComponent.ChannelId.values(), HeatPump.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        try {
            if (cpm.getComponent(config.genibusId()) instanceof Genibus) {
                Genibus genibus = cpm.getComponent(config.genibusId());
                genibus.addTask(super.id(), new HeatPumpTask());
            } else {
                throw new ConfigurationException("Genibus with id" + config.genibusId() + "not a Genibus", "GenibusId was wrong");
            }
        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
    }

}
