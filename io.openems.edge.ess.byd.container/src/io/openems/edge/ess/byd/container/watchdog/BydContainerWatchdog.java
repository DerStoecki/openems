package io.openems.edge.ess.byd.container.watchdog;

import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.modbusslave.ModbusType;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.byd.container.EssFeneconBydContainer;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.ess.power.api.Relationship;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Ess.Fenecon.BydContainer.WatchdogController", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BydContainerWatchdog extends AbstractOpenemsComponent
		implements Controller, OpenemsComponent, ModbusSlave {

	private final Logger log = LoggerFactory.getLogger(BydContainerWatchdog.class);

	@Reference
	public ComponentManager componentManager;

	@Reference
	protected ConfigurationAdmin cm;

	public Config config;

	public BydContainerWatchdog() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		WATCHDOG(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.WRITE_ONLY)),
		IS_TIMEOUT(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.WRITE_ONLY));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws IllegalArgumentException, OpenemsNamedException {
		// Get ESS
		EssFeneconBydContainer ess = this.componentManager.getComponent(this.config.ess_id());

		boolean isReadonly = (boolean) ess.getComponentContext().getProperties().get("readonly"); // ess.config.readonly();//

		// Check if Watchdog has been triggered in time. Timeout is configured in
		// Modbus-TCP-Api Controller.
		IntegerWriteChannel channel = this.channel(ChannelId.WATCHDOG);
		Optional<Integer> value = channel.getNextWriteValueAndReset();

		this.logInfo(this.log, "Value [" + (value) + "].");
		if (value.isPresent()) {
			// No Timeout

			if (isReadonly) {
				// if readonly is already set to true --> do nothing
				//
			} else {
				// Set to read-only mode
				setConfig(true, ess.servicePid());
			}
		} else {
			if (isReadonly) {
				// Timeout happened, Set readonly flag to false once and set the active power to
				// zero
				setConfig(false, ess.servicePid());

			} else {
				// We have control
				// setting the active and reactive power to zero
				ess.addPowerConstraintAndValidate("BydContainerWatchdog", Phase.ALL, Pwr.ACTIVE, Relationship.EQUALS, 0);
				ess.addPowerConstraintAndValidate("BydContainerWatchdog", Phase.ALL, Pwr.REACTIVE, Relationship.EQUALS, 0);
			}
		}
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				ModbusSlaveNatureTable.of(BydContainerWatchdog.class, accessMode, 100) //
						.channel(0, ChannelId.WATCHDOG, ModbusType.UINT16) //
						.build());
	}

	/**
	 * Helper function to set the configuration based on the watchdog value.
	 *
	 * @param value true to set readonly flag on, false to set the readonly flag off;
	 * @param pid pid of the Ess             
	 * @throws OpenemsNamedException on error
	 * 
	 */
	private void setConfig(Boolean value, String pid) throws OpenemsNamedException {
		OpenemsComponent.updateConfigurationProperty(this.cm, pid, "readonly", value);
	}	
}