package io.openems.edge.lucidcontrol.device;

import io.openems.edge.bridge.lucidcontrol.api.LucidControlBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.lucidcontrol.device.api.LucidControlDevice;
import io.openems.edge.lucidcontrol.device.task.LucidControlReadTask;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.Designate;



@Designate(ocd = Config.class, factory = true)
@Component(name = "LucidControlDevice")
public class LucidControlDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, LucidControlDevice {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    LucidControlBridge lucidControlBridge;


    public LucidControlDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                LucidControlDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {

        super.activate(context, config.id(), config.alias(), config.enabled());
        lucidControlBridge.addLucidControlTask(config.id(),
                new LucidControlReadTask(config.moduleId(), config.id(),
                        lucidControlBridge.getPath(config.moduleId()),
                        lucidControlBridge.getVoltage(config.moduleId()),config.pinPos(),
                        this.getPressure()));
    }

    @Deactivate
    public void deactivate() {
        lucidControlBridge.removeTask(super.id());
        super.deactivate();
    }

    @Override
    public String debugLog() {
        if (getPressure().getNextValue().isDefined()) {
            return "The pressure of " + super.id() + " is: " + getPressure().getNextValue().get();
        } else {
            return "The pressure of " + super.id() + " is not defined yet.";
        }
    }
}
