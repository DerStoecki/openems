package io.openems.edge.consolinno.leaflet.mainmodule.pcaGpioExpansion;

import com.pi4j.io.i2c.I2CFactory;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.i2c.api.I2cBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.mainmodule.api.AbstractPcaMainModuleProvider;
import io.openems.edge.consolinno.leaflet.mainmodule.api.Pca9536MainModuleProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import java.io.IOException;

@Designate(ocd = Config.class, factory = true)
@Component(name = "MainModule.Pca",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class PcaGpioExpansion extends AbstractOpenemsComponent implements OpenemsComponent {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    I2cBridge refI2cBridge;

    private AbstractPcaMainModuleProvider pca;

    public PcaGpioExpansion() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException, OpenemsException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocatePcaProvider(config);

    }

    private void allocatePcaProvider(Config config) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException, OpenemsException {
        switch (config.version()) {
            case "0.55":
            default:
                this.pca = new Pca9536MainModuleProvider(config.bus_address(), config.pca_address(),
                        config.version(), super.id());
                this.refI2cBridge.addMainModulePca(this.pca);
        }
    }

    @Deactivate
    public void deactivate() {
        refI2cBridge.removeMainModulePca(super.id());
        super.deactivate();
    }

    public AbstractPcaMainModuleProvider getPca() {
        return pca;
    }
}
