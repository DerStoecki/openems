package io.openems.edge.controller.pid.passing;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.filter.PidFilter;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.pid.passing.api.PidForPassingNature;
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
@Component(name = "Controller.Passing.Pid")
public class PidForPassingStationController extends AbstractOpenemsComponent implements OpenemsComponent, Controller, PidForPassingNature {

    @Reference
    ComponentManager cpm;

    private PassingForPid passingForPid;
    private Thermometer thermometer;
    private ControllerPassingChannel passing;
    private boolean isPump;
    private PidFilter pidFilter;

    private double intervalTime = 2000;
    private double timestamp = 0;

    public PidForPassingStationController() {
        super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values(), PidForPassingNature.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
        super.activate(context, config.id(), config.alias(), config.enabled());
        allocateComponent(config.temperatureSensorId());
        allocateComponent(config.allocatedPassingDevice());
        allocateComponent(config.passingControllerId());
        this.pidFilter = new PidFilter(config.proportionalGain(), config.integralGain(), config.derivativeGain());
        pidFilter.setLimits(-200, 200);
        try {
            this.setMinTemperature().setNextWriteValue(config.setPoint_Temperature());
            //for REST / JSON
            this.setMinTemperature().setNextValue(config.setPoint_Temperature());
        } catch (OpenemsError.OpenemsNamedException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Allocate the Component.</p>
     *
     * @param Device String from Config; needs to be an instance of PassingForPid/Thermometer/ControllerPassingChannel.
     *               <p>
     *               Allocate the Component --> Access to Channels
     *               </p>
     * @throws OpenemsError.OpenemsNamedException when cpm can't access / somethings wrong with cpm.
     * @throws ConfigurationException                                          when cpm tries to access device but it's not correct instance.
     */
    private void allocateComponent(String Device) throws OpenemsError.OpenemsNamedException, ConfigurationException {
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

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    /**
     * Controls a pump or valve via PID.
     *
     * <p>
     * Only activates/runs if PassingController is active!
     * if the Temperature from the temperatureSensor ist defined and the controller is ready to calc,
     * PID value is calculated by setPointTemperature; usually from config. And the measured Temperature.
     * This output is saved and the currentPowerLevel of the passingForPid substracts it's value from the output.
     * if the passingForPid is a pump; the value will be inverted --> slower pump --> water will be heated up faster
     * And vice versa.
     * the output is divided by 10 bc the return value is in %*10 (or at least thats what i think due to testing the
     * return values are sometimes 200 etc).
     *
     * </p>
     */
    @Override
    public void run() throws OpenemsError.OpenemsNamedException {


<<<<<<< HEAD
        if (this.turnOn().value().isDefined() && this.turnOn().value().get()) {
            if (this.passing.getOnOff_PassingController().getNextWriteValue().isPresent() && this.passing.getOnOff_PassingController().getNextWriteValue().get()) {
                if (this.thermometer.getTemperature().getNextValue().isDefined() && readyToCalc()) {
                    if (this.setMinTemperature().getNextWriteValue().isPresent()) {
                        this.setMinTemperature().setNextValue(this.setMinTemperature().getNextWriteValue().get());
                    }
                    this.timestamp = System.currentTimeMillis();
                    double output = pidFilter.applyPidFilter(this.thermometer.getTemperature().getNextValue().get(), this.setMinTemperature().getNextWriteValue().get());
                    // is percentage value fix if so substract from current powerlevel?
                    output -= this.passingForPid.getPowerLevel().getNextValue().get();

                    if (this.isPump) {
                        output *= -1;
                    }
                    if (this.passingForPid.readyToChange()) {
                        this.passingForPid.changeByPercentage(output / 10);
                    }
=======
        if (this.passing.getOnOff_PassingController().getNextWriteValue().isPresent() && this.passing.getOnOff_PassingController().getNextWriteValue().get()) {
            if (this.thermometer.getTemperature().getNextValue().isDefined()) {
                if (this.setMinTemperature().getNextWriteValue().isPresent()) {
                    this.setMinTemperature().setNextValue(this.setMinTemperature().getNextWriteValue().get());
                }
                this.timestamp = System.currentTimeMillis();
                double output = pidFilter.applyPidFilter(this.thermometer.getTemperature().getNextValue().get(), this.setMinTemperature().getNextWriteValue().get());
                // is percentage value fix if so substract from current powerlevel?
                output -= this.passingForPid.getPowerLevel().getNextValue().get();
>>>>>>> hotfix/PassingStation

                }
            }
        }

    }
}
