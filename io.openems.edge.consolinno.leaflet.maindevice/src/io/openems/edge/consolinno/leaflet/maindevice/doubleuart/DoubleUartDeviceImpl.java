package io.openems.edge.consolinno.leaflet.maindevice.doubleuart;

import io.openems.edge.bridge.spi.api.BridgeSpi;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.consolinno.leaflet.maindevice.api.doubleuart.DoubleUartDevice;
import io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task.DoubleUartReadTaskImpl;
import io.openems.edge.consolinno.leaflet.maindevice.doubleuart.task.DoubleUartWriteTaskImpl;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.DoubleUart;
import io.openems.edge.consolinno.leaflet.mainmodule.api.sc16.Sc16IS752;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

public class DoubleUartDeviceImpl extends AbstractOpenemsComponent implements OpenemsComponent, DoubleUartDevice {

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MANDATORY)
    BridgeSpi bridgeSpi;
    private boolean isWrite;
    DoubleUart uart;

    public DoubleUartDeviceImpl() {
        super(OpenemsComponent.ChannelId.values(),
                DoubleUartDevice.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) throws ConfigurationException {

        super.activate(context, config.id(), config.alias(), config.enabled());

        setGpioErrorMessageAndWrite(config.pinPosition());


        uart = bridgeSpi.getUart(config.spiChannel());
        if (uart instanceof Sc16IS752) {
            Sc16IS752 sc16 = (Sc16IS752) uart;

            if (isWrite) {
                sc16.addTask(super.id(), new DoubleUartWriteTaskImpl(super.id(), config.spiChannel(),
                        config.pinPosition(), getOnOff()));
            } else {
                sc16.addTask(super.id(), new DoubleUartReadTaskImpl(super.id(), config.spiChannel(),
                        config.pinPosition(), getOnOff()));
            }
        }
    }

    @Deactivate
    public void deactivate() {
        uart.removeTask(super.id());
        super.deactivate();
    }

    private void setGpioErrorMessageAndWrite(int pinAddress) throws ConfigurationException {
        switch (pinAddress) {

            case 0:
                this.isWrite = true;
                this.getErrorMessage().setNextValue("STATUS-LED-RED");
                break;
            case 1:
                this.isWrite = true;
                this.getErrorMessage().setNextValue("STATUS-LED-YELLOW");
                break;
            case 2:
                this.isWrite = true;
                this.getErrorMessage().setNextValue("STATUS-LED-GREEN");
                break;
            case 6:
                this.isWrite = true;
                this.getErrorMessage().setNextValue("OUTPUT-VOLTAGE-SERVICE-PORTS 5V, 3.3V");
                break;
            case 4:
                this.getErrorMessage().setNextValue("ERROR-FLAG-HBUS 5V");
                break;
            case 5:
                this.getErrorMessage().setNextValue("ERROR-FLAG-HBUS 24V");
                break;
            case 7:
                this.getErrorMessage().setNextValue("OUTPUT-VOLTAGE-SERVICE-PORTS");
                break;
        }
        throw new ConfigurationException("setCorrectGpioAddress", "The Pin " + pinAddress + " is not supported");
    }


    @Override
    public String debugLog() {
        String onOff = "Off";
        if (this.getOnOff().value().isDefined()) {
            if (this.getOnOff().value().get()) {
                onOff = "On";
            }
        }
        return this.getErrorMessage().value() + " " + super.id() + " Status: " + onOff;
    }
}
