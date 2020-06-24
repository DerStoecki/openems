package io.openems.edge.consolinno.leaflet.maindevice.doubleuart;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.doubleuart.DoubleUartDevice;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

public class DoubleUartDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, DoubleUartDevice {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;
    boolean isWrite;
    private int pinAddress;

    public DoubleUartDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                DoubleUartDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        this.pinAddress = setCorrectGpioAddress(config.pinAddress());

    }

    private int setCorrectGpioAddress(int pinAddress) throws ConfigurationException {
        switch (pinAddress) {

            case 0:
                this.isWrite = true;
                return 18;
            case 1:
                this.isWrite = true;
                return 19;
            case 2:
                this.isWrite = true;
                return 20;
            case 6:
                this.isWrite = true;
                return 27;
            case 4:
            case 5:
            case 7:
        }
        throw new ConfigurationException("setCorrectGpioAddress", "The PinAddress " + pinAddress + " is not supported");
    }


}
