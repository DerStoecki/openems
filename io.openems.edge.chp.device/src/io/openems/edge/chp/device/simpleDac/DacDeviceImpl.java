package io.openems.edge.chp.device.simpleDac;


import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.ChpType;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.chp.device.task.ChpTaskImpl;
import io.openems.edge.chp.module.api.ChpModule;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.i2c.mcp.api.Mcp;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "DacDevice",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class DacDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, PowerLevel {
    private Mcp mcp;

    @Reference
    protected ComponentManager cpm;

    public DacDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                PowerLevel.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        if (cpm.getComponent(config.chpModuleId()) instanceof ChpModule) {
            ChpModule chpModule = cpm.getComponent(config.chpModuleId());
            mcp = chpModule.getMcp();
            mcp.addTask(super.id(), new ChpTaskImpl(super.id(),
                    config.position(), config.minLimit(), config.maxLimit(),
                    config.percentageRange(), 4096.f, this.getPowerLevelChannel()));
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
            this.mcp.removeTask(super.id());
    }


    @Override
    public String debugLog() {

            if (this.getPowerLevelChannel().getNextValue().get() != null) {
                return "DAC: " + super.id() + "is at " + this.getPowerLevelChannel().getNextValue().get();
            }
            return "Percentage Level at 0";

    }

}