package io.openems.edge.consolinno.leaflet.mainmodule.pcaGpioExpansion;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
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

    AbstractPcaMainModuleProvider pca;

    public PcaGpioExpansion() {
        super(OpenemsComponent.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocatePcaProvider(config);

    }

    private void allocatePcaProvider(Config config) throws IOException, I2CFactory.UnsupportedBusNumberException, ConfigurationException {
        switch (config.version()) {
            case "0.55":
            default:
                this.pca = new Pca9536MainModuleProvider(config.bus_address(), Integer.parseInt(config.pca_address()),
                        config.version());
                this.refI2cBridge.addMainModulePca(super.id(), this.pca);
        }
    }



    @Deactivate
    public void deactivate() {
        refI2cBridge.removeMainModulePca(super.id());
        super.deactivate();
    }

}
