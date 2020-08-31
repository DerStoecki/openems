package io.openems.edge.lucidcontrol.device;


import io.openems.edge.bridge.lucidcontrol.api.LucidControlBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.lucidcontrol.device.api.LucidControlDeviceOutput;

import io.openems.edge.lucidcontrol.device.task.LucidControlOutputTask;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = OutputConfig.class, factory = true)
@Component(name = "Device.LucidControl.Input")
public class LucidControlOutputDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, LucidControlDeviceOutput {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MANDATORY)
    LucidControlBridge lucidControlBridge;

    public LucidControlOutputDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                LucidControlDeviceOutput.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, InputConfig config) {

        super.activate(context, config.id(), config.alias(), config.enabled());
        lucidControlBridge.addLucidControlTask(config.id(),
                new LucidControlOutputTask(config.moduleId(), config.id(), lucidControlBridge.getPath(config.moduleId()),
                        lucidControlBridge.getVoltage(config.moduleId()), config.pinPos(),
                        this.getPercentageChannel()));
    }

    @Deactivate
    public void deactivate() {
        lucidControlBridge.removeTask(super.id());
        super.deactivate();
    }

    @Override
    public String debugLog() {
        if (this.getPercentageValue().isDefined()) {
            return "The pressure of " + super.id() + " is set to: " + this.getPercentageValue().get();
        } else {
            return "The pressure of " + super.id() + " was not set yet";
        }
    }

}
