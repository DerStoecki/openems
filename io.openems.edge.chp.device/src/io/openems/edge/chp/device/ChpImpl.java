package io.openems.edge.chp.device;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.chp.device.api.PowerLevel;
import io.openems.edge.chp.device.task.ChpTask;
import io.openems.edge.chp.module.api.ChpModule;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.i2c.mcp.api.Mcp;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "Chp",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class ChpImpl extends AbstractOpenemsComponent implements OpenemsComponent, PowerLevel {
    private Mcp mcp;
    //bhkwType only for purposes coming in future
    private ChpType chpType;
    @Reference
    protected ComponentManager cpm;

    public ChpImpl() {
        super(OpenemsComponent.ChannelId.values(), PowerLevel.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException {
        super.activate(context, config.id(), config.alias(), config.enabled());

        switch (config.chpType()) {
            case "EM_6_15":
                this.chpType = ChpType.Vito_EM_6_15;
                break;
            case "EM_9_20":
                this.chpType = ChpType.Vito_EM_9_20;
                break;
            case "EM_20_39":
                this.chpType = ChpType.Vito_EM_20_39;
                break;
            case "EM_20_39_70":
                this.chpType = ChpType.Vito_EM_20_39_RL_70;
                break;
            case "EM_50_81":
                this.chpType = ChpType.Vito_EM_50_81;
                break;
            case "EM_70_115":
                this.chpType = ChpType.Vito_EM_70_115;
                break;
            case "EM_100_167":
                this.chpType = ChpType.Vito_EM_100_167;
                break;
            case "EM_140_207":
                this.chpType = ChpType.Vito_EM_140_207;
                break;
            case "EM_199_263":
                this.chpType = ChpType.Vito_EM_199_263;
                break;
            case "EM_199_293":
                this.chpType = ChpType.Vito_EM_199_293;
                break;
            case "EM_238_363":
                this.chpType = ChpType.Vito_EM_238_363;
                break;
            case "EM_363_498":
                this.chpType = ChpType.Vito_EM_363_498;
                break;
            case "EM_401_549":
                this.chpType = ChpType.Vito_EM_401_549;
                break;
            case "EM_530_660":
                this.chpType = ChpType.Vito_EM_530_660;
                break;
            case "BM_36_66":
                this.chpType = ChpType.Vito_BM_36_66;
                break;
            case "BM_55_88":
                this.chpType = ChpType.Vito_BM_55_88;
                break;
            case "BM_190_238":
                this.chpType = ChpType.Vito_BM_190_238;
                break;
            case "BM_366_437":
                this.chpType = ChpType.Vito_BM_366_437;
                break;

            default:
                break;

        }

        if (cpm.getComponent(config.chpModuleId()) instanceof ChpModule) {
            ChpModule chpModule = cpm.getComponent(config.chpModuleId());
            mcp = chpModule.getMcp();
            mcp.addTask(super.id(), new ChpTask(super.id(),
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
            if (chpType != null) {
                return "Chp: " + this.chpType.getName() + "is at " + this.getPowerLevelChannel().getNextValue().get();
            } else {
                return "Chp is at " + this.getPowerLevelChannel().getNextValue().get();
            }
        }
        return "Percentage Level at 0";
    }

}
