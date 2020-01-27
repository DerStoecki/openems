package io.openems.edge.gpio.device;

import io.openems.edge.bridge.gpio.api.GpioBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.gpio.device.api.GpioDevice;
import io.openems.edge.gpio.device.task.GpioDeviceTaskImpl;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "GpioDevice",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class GpioDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, GpioDevice {

    private String informationType;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MANDATORY)
    GpioBridge gpioBridge;


    public GpioDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GpioDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {

        super.activate(context, config.id(), config.alias(), config.enabled());


        gpioBridge.addGpioTask(super.id(), new GpioDeviceTaskImpl(super.id(), setCorrectGpioPosition(config.pinPosition()), getOnOff()));
        this.informationType = config.informationType();
    }

    private int setCorrectGpioPosition(String pinPosition) {
        switch (pinPosition) {
            case "1":
                return 4;
            case "2":
                return 17;
            case "3":
                return 27;
        }
        return -1;
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        gpioBridge.removeGpioTask(super.id());
    }

    @Override
    public String debugLog() {
        if (getOnOff().getNextValue().isDefined()) {
            String debugInfo = "The GpioDevice: " + super.id();
            if (this.informationType.equals("OnOff")) {
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
