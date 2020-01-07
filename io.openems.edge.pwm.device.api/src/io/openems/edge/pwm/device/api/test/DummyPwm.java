package io.openems.edge.pwm.device.api.test;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;


public class DummyPwm extends AbstractOpenemsComponent implements OpenemsComponent, PwmPowerLevelChannel {

	public DummyPwm(String id){
		super(OpenemsComponent.ChannelId.values(),
				PwmPowerLevelChannel.ChannelId.values()
		);
		super.activate(null, id, "", true);

	}

}
