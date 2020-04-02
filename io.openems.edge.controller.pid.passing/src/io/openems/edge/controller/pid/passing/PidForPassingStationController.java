package io.openems.edge.controller.pid.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.filter.PidFilter;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.temperature.passing.api.ControllerPassingChannel;
import io.openems.edge.temperature.passing.api.PassingChannel;
import io.openems.edge.temperature.passing.api.PassingForPid;
import io.openems.edge.temperature.passing.pump.api.Pump;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;


@Designate(ocd = Config.class, factory = true)
@Component(name = "PidPassingStationController")
public class PidForPassingStationController extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

    @Reference
    ComponentManager cpm;

    private PassingForPid passingForPid;
    private Thermometer thermometer;
    private ControllerPassingChannel passing;
    private boolean isPump;
    private PidFilter pidFilter;
    private int setPointTemperature;
    private double intervalTime = 2000;
    private double timestamp = 0;

    public PidForPassingStationController() {
        super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponent(config.temperatureSensorId());
        allocateComponent(config.allocatedPassingDevice());
        allocateComponent(config.passingControllerId());
        this.pidFilter = new PidFilter(config.proportionalGain(), config.integralGain(), config.derivativeGain());
        pidFilter.setLimits(-200, 200);
        this.setPointTemperature = config.setPoint_Temperature();
        this.intervalTime = config.intervalTime() > 0 ? config.intervalTime() * 1000 : 2000;
    }

    private void allocateComponent(String Device) {
        try {
            if (cpm.getComponent(Device) instanceof PassingForPid) {
                if (cpm.getComponent(Device) instanceof Pump) {
                    this.isPump = true;
                }
                this.passingForPid = cpm.getComponent(Device);
            } else if (cpm.getComponent(Device) instanceof Thermometer) {
                this.thermometer = cpm.getComponent(Device);
            } else if (cpm.getComponent(Device) instanceof ControllerPassingChannel) {
                this.passing = cpm.getComponent(Device);
            } else {
                throw new ConfigurationException("The configured Component is neither Valve, Pump, PassingController nor TemperatureSensor! Please Check "
                        + Device, "Configured Component is incorrect!");
            }

        } catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    public void run() throws OpenemsError.OpenemsNamedException {

        if (this.passing.getOnOff_PassingController().getNextWriteValue().isPresent() && this.passing.getOnOff_PassingController().getNextWriteValue().get()) {
            if (this.thermometer.getTemperature().getNextValue().isDefined() && readyToCalc() && this.passingForPid.getPowerLevel().getNextValue().isDefined()) {
                this.timestamp = System.currentTimeMillis();
                double output = pidFilter.applyPidFilter(this.thermometer.getTemperature().getNextValue().get(), this.setPointTemperature);
                // is percentage value fix if so substract from current powerlevel?
                output -= this.passingForPid.getPowerLevel().getNextValue().get();

                if (this.isPump) {
                    output *= -1;
                }
                if (this.passingForPid.readyToChange()) {
                    this.passingForPid.changeByPercentage(output / 10);
                }

            }
        }
    }

    private boolean readyToCalc() {
        return System.currentTimeMillis() - this.timestamp >= intervalTime;
    }
}
