package io.openems.edge.heatmeter;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;


@Designate( ocd=ProviderImpl.Config.class, factory=true)
@Component(name="io.openems.edge.heatmeter")
public class ProviderImpl /* implements SomeApi */ {

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
