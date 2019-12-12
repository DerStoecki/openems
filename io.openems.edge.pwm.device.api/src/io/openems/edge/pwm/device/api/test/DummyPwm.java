package io.openems.edge.pwm.device.api.test;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.pwm.device.api.PwmPowerLevelChannel;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;



public class DummyPwm extends AbstractOpenemsComponent implements OpenemsComponent, PwmPowerLevelChannel {

	public DummyPwm(String id){
		super(OpenemsComponent.ChannelId.values(),
				PwmPowerLevelChannel.ChannelId.values()
		);
		super.activate(null, id, "", true);

	}

}
