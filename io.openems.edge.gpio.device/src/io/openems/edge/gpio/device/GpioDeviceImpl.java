package io.openems.edge.gpio.device;

import io.openems.edge.bridge.gpio.api.GpioBridge;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.gpio.device.api.GpioDevice;
import io.openems.edge.gpio.device.task.GpioDeviceReadTaskImpl;
import io.openems.edge.gpio.device.task.GpioDeviceWriteTaskImpl;
import org.osgi.service.cm.ConfigurationException;
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
    boolean isWrite = false;

    public GpioDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GpioDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        int pinPosition = setCorrectGpioPosition(config.pinPosition());
        if (isWrite) {
            gpioBridge.addGpioWriteTask(super.id(), new GpioDeviceWriteTaskImpl(super.id(), pinPosition, getWriteError()));
        }
        gpioBridge.addGpioReadTask(super.id(), new GpioDeviceReadTaskImpl(super.id(), pinPosition, getReadError()));
        this.informationType = config.informationType();
    }

    /**
     * Sets The correctPinPosition.
     *
     * @param pinPosition usually from config. Sets the Pin position.
     *
     *                    <p>The Pin Position on the Leaflet base module is set. first input remapped the 4thGpio
     *                    2nd input the 17th Gpio
     *                    and 3rd input to 27th gpio</p>
     * @return the gpio position as an int.
     */

    private int setCorrectGpioPosition(String pinPosition) {
        switch (pinPosition) {
            // case "1":
            //     return 4;
            // case "2":
            //     return 17;
            // case "3":
            //     return 27;
            case "1.1":
                return 0;
            case "1.2":
                return 22;
            case "1.3":
                return 1;
            case "1.4":
                return 27;
            case "2.1":
                return 13;
            case "2.2":
                return 12;


            case "0.1":
                isWrite = true;
                return 4; //red
            case "0.2":
                isWrite = true;
                return 17; // yellow
            case "0.3":
                isWrite = true;
                return 18; // green


        }
        return -1;
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
        if (isWrite) {
            gpioBridge.removeGpioWriteTask(super.id());
        } else {
            gpioBridge.removeGpioReadTask(super.id());
        }
    }

    @Override
    public String debugLog() {
        if (getReadError().getNextValue().isDefined()) {
            String debugInfo = "The GpioDevice: " + super.id();
            if (this.informationType.equals("OnOff")) {
                if (getReadError().getNextValue().get()) {
                    debugInfo += " is On";
                } else {
                    debugInfo += " is Offline";
                }
            } else {
                if (getReadError().getNextValue().get()) {
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
