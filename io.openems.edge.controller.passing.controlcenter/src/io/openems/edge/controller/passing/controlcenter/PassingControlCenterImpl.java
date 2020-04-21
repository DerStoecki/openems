package io.openems.edge.controller.passing.controlcenter;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.pid.passing.api.PidForPassingNature;
import io.openems.edge.controller.warmup.passing.api.ControllerWarmupChannel;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Designate( ocd=Config.class, factory=true)
@Component(name="PassingControlCenter", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PassingControlCenterImpl extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

	private final Logger log = LoggerFactory.getLogger(PassingControlCenterImpl.class);

	@Reference
	protected ComponentManager cpm;

	private PidForPassingNature pidControllerChannel;
	private ControllerWarmupChannel warmupControllerChannel;
//	private OtherController otherControllerChannel;
	private int temperatureDezidegree;

	public PassingControlCenterImpl() {

		super(OpenemsComponent.ChannelId.values(),
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
			/*
			if (cpm.getComponent(config.allocated_Other_Controller()) instanceof ---othercontrollerchannel---) {
				this.otherControllerChannel = cpm.getComponent(config.allocated_Other_Controller());
			} else {
				throw new ConfigurationException(config.allocated_Warmup_Controller(),
						"Allocated Other Controller not a Other Controller; Check if Name is correct and try again.");
			}
			*/
		} catch (ConfigurationException | OpenemsError.OpenemsNamedException e) {
			e.printStackTrace();
			throw e;
		}

		temperatureDezidegree = 0;
		if (config.run_warmup_program()){
			warmupControllerChannel.playPauseWarmupController().setNextWriteValue(true);
		}
	}

	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {
		if (warmupControllerChannel.noError().getNextValue().isDefined() && warmupControllerChannel.getWarmupTemperature().getNextValue().isDefined()){
			if (!warmupControllerChannel.noError().getNextValue().get()){
				temperatureDezidegree = warmupControllerChannel.getWarmupTemperature().getNextValue().get();
			}
		}

		if(!warmupControllerChannel.playPauseWarmupController().getNextWriteValue().isPresent() || !warmupControllerChannel.playPauseWarmupController().getNextWriteValue().get()){	//No value or paused
			//Code from other controller goes here
			//temperatureDezidegree = set by other controller
		}

		pidControllerChannel.setMinTemperature().setNextWriteValue(temperatureDezidegree);

	}

}
