package io.openems.edge.consolinno.leaflet.maindevice.pca;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.i2c.api.I2cBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.PcaDevice;
import io.openems.edge.consolinno.leaflet.maindevice.pca.task.PcaDeviceReadTask;
import io.openems.edge.consolinno.leaflet.maindevice.pca.task.PcaDeviceWriteTask;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "MainModule.PcaDevice",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class PcaDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, PcaDevice {
    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    I2cBridge refI2cBridge;

    private String descriptor;


    public PcaDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(), PcaDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        this.descriptor = config.descriptor();
        allocateToPca(config);

    }

    @Deactivate
    public void deactivate() {
        refI2cBridge.removeMainModulePcaTask(super.id());
        super.deactivate();
    }

    private void allocateToPca(Config config) throws OpenemsException {

        boolean[] readOrWrite;
        switch (refI2cBridge.getPcaMainProviderVersion(config.moduleId())) {
            case "0.05":
            default:
                readOrWrite = new boolean[]{false, true, false, false};
        }
        if (readOrWrite[config.pinPosition()]) {
            refI2cBridge.addMainModulePcaTask(super.id(), new PcaDeviceWriteTask(config.moduleId(),
                    super.id(), config.pinPosition(), this.getOnOff()));

        } else {
            refI2cBridge.addMainModulePcaTask(super.id(), new PcaDeviceReadTask(config.moduleId(),
                    super.id(), config.pinPosition(), this.getOnOff()));
        }

    }

    @Override
    public String debugLog() {
        if (getOnOff().getNextValue().isDefined()) {
            String debugInfo = "The PcaDevice: " + super.id();
            if (this.descriptor.equals("OnOff")) {
                if (getOnOff().getNextValue().get()) {
                    debugInfo += " is On";
                } else {
                    debugInfo += " is Offline";
                }
            } else {
                if (getOnOff().getNextValue().get()) {
                    debugInfo += " got an error!";
                } else {
                    debugInfo += " no errors";
                }
            }
            return debugInfo;
        } else {
            return null;
        }
    }
}
