package io.openems.edge.consolinno.leaflet.maindevice.doubleuart;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.doubleuart.DoubleUartDevice;
import io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task.DoubleUartReadTaskImpl;
import io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task.DoubleUartWriteTaskImpl;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

public class DoubleUartDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, DoubleUartDevice {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;
    private boolean isWrite;

    public DoubleUartDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                DoubleUartDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());
        byte pinAddress = setCorrectGpioAddress(config.pinAddress());

        if (isWrite) {
            bridgeSpi.addDoubleUartTask(super.id(), new DoubleUartWriteTaskImpl(super.id(), config.spiChannel(),
                    pinAddress, getOnOff()));
        } else {
            bridgeSpi.addDoubleUartTask(super.id(), new DoubleUartReadTaskImpl(super.id(), config.spiChannel(),
                    pinAddress, getOnOff()));
        }
    }

    @Deactivate
    public void deactivate() {
        bridgeSpi.removeDoubleUartTask(super.id());
        super.deactivate();

    }

    private byte setCorrectGpioAddress(int pinAddress) throws ConfigurationException {
        switch (pinAddress) {
            //GPIO 0-7 will be translated --> Bit 7 == R/W (0/1); Bit 6:3 == GPIO; 2:1 == Channel A/B; 0 == not used
            // == Register Address
            // E.g. GPIO 0 == DSRB --> Write + 0001 + 01 + 0 --> 00001010
            case 0:
                this.isWrite = true;
                return 10;
            case 1:
                this.isWrite = true;
                return 18;
            case 2:
                this.isWrite = true;
                return 34;
            case 6:
                this.isWrite = true;
                return 32;
            case 4:
                return 8;
            case 5:
                return 16;
            case 7:
                return 64;
        }
        throw new ConfigurationException("setCorrectGpioAddress", "The Pin " + pinAddress + " is not supported");
    }


    @Override
    public String debugLog() {
        String onOff = "Off";
        if (this.getOnOff().getNextValue().isDefined()) {
            if (this.getOnOff().getNextValue().get()) {
                onOff = "On";
            }
        }
        return super.id() + " Status: " + onOff;
    }
}
