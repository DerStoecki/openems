package io.openems.edge.controller.passing.controlcenter;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.passing.controlcenter.api.PassingControlCenterChannel;
import io.openems.edge.controller.passing.heatingcurveregulator.api.HeatingCurveRegulatorChannel;
import io.openems.edge.controller.pid.passing.api.PidForPassingNature;
import io.openems.edge.controller.warmup.passing.api.ControllerWarmupChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate(ocd = Config.class, factory = true)
@Component(name = "PassingControlCenter", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PassingControlCenterImpl extends AbstractOpenemsComponent implements OpenemsComponent, PassingControlCenterChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(PassingControlCenterImpl.class);

	@Reference
	protected ComponentManager cpm;

	private PidForPassingNature pidControllerChannel;
	private ControllerWarmupChannel warmupControllerChannel;
	private HeatingCurveRegulatorChannel heatingCurveRegulatorChannel;
	private int temperatureDezidegree;

	public PassingControlCenterImpl() {
		super(OpenemsComponent.ChannelId.values(),
				PassingControlCenterChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		//allocate components
		try {
			if (cpm.getComponent(config.allocated_Pid_Controller()) instanceof PidForPassingNature) {
				pidControllerChannel = cpm.getComponent(config.allocated_Pid_Controller());
			} else {
				throw new ConfigurationException(config.allocated_Pid_Controller(),
						"Allocated Passing Controller not a Pid for Passing Controller; Check if Name is correct and try again.");
			}
			if (cpm.getComponent(config.allocated_Warmup_Controller()) instanceof ControllerWarmupChannel) {
				this.warmupControllerChannel = cpm.getComponent(config.allocated_Warmup_Controller());
			} else {
				throw new ConfigurationException(config.allocated_Warmup_Controller(),
						"Allocated Warmup Controller not a WarmupPassing Controller; Check if Name is correct and try again.");
			}
			if (cpm.getComponent(config.allocated_Heating_Curve_Regulator()) instanceof HeatingCurveRegulatorChannel) {
				this.heatingCurveRegulatorChannel = cpm.getComponent(config.allocated_Heating_Curve_Regulator());
			} else {
				throw new ConfigurationException(config.allocated_Warmup_Controller(),
						"Allocated Heating Controller not a Heating Curve Regulator; Check if Name is correct and try again.");
			}
		} catch (ConfigurationException | OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
			throw e;
		}

		this.activateHeater().setNextValue(false);
		if (config.run_warmup_program()) {
			warmupControllerChannel.playPauseWarmupController().setNextWriteValue(true);
		}
	}

	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Check all channels if they have values in them.
		boolean overrideChannelHasValues = this.activateTemperatureOverride().getNextWriteValue().isPresent()
				&& this.setOverrideTemperature().getNextWriteValue().isPresent();

		boolean warmupControllerChannelHasValues = warmupControllerChannel.playPauseWarmupController().getNextWriteValue().isPresent()
				&& warmupControllerChannel.getWarmupTemperature().getNextValue().isDefined()
				&& warmupControllerChannel.noError().getNextValue().isDefined();

		boolean heatingCurveRegulatorChannelHasValues = heatingCurveRegulatorChannel.isActive().getNextValue().isDefined()
				&& heatingCurveRegulatorChannel.getHeatingTemperature().getNextValue().isDefined()
				&& heatingCurveRegulatorChannel.noError().getNextValue().isDefined();

		// Execute controllers by priority. From high to low: override, warmup, heatingCurve
		if (overrideChannelHasValues && this.activateTemperatureOverride().getNextWriteValue().get()) {
			temperatureDezidegree = this.setOverrideTemperature().getNextWriteValue().get();
			this.activateHeater().setNextValue(true);
		} else if (warmupControllerChannelHasValues && warmupControllerChannel.playPauseWarmupController().getNextWriteValue().get()
				&& warmupControllerChannel.noError().getNextValue().get()) {
			temperatureDezidegree = warmupControllerChannel.getWarmupTemperature().getNextValue().get();
			this.activateHeater().setNextValue(true);
		} else if (heatingCurveRegulatorChannelHasValues && heatingCurveRegulatorChannel.isActive().getNextValue().get()
				&& heatingCurveRegulatorChannel.noError().getNextValue().get()) {
			temperatureDezidegree = heatingCurveRegulatorChannel.getHeatingTemperature().getNextValue().get();
			this.activateHeater().setNextValue(true);
		} else {
			this.activateHeater().setNextValue(false);
		}

		// Send controller output to pid.
		if (this.activateHeater().getNextValue().get()) {
			pidControllerChannel.turnOn().setNextWriteValue(true);
			pidControllerChannel.setMinTemperature().setNextWriteValue(temperatureDezidegree);
		} else {
			pidControllerChannel.turnOn().setNextWriteValue(false);
		}


	}

}
