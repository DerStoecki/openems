package io.openems.edge.pwm.device.api;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;


@Designate( ocd= DummyPwm.Config.class, factory=true)
@Component(name="io.openems.edge.pwm.device.api")
public class DummyPwm /* implements SomeApi */ {

	@ObjectClassDefinition
	@interface Config {
		String name() default "World";
	}

	private String name;

	@Activate
	void activate(Config config) {
		this.name = config.name();
	}

	@Deactivate
	void deactivate() {
	}

}
