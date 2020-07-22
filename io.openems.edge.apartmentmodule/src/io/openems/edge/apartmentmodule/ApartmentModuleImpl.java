package io.openems.edge.apartmentmodule;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.*;
import io.openems.edge.bridge.modbus.api.task.*;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.apartmentmodule.api.ApartmentModuleChannel;
import io.openems.edge.apartmentmodule.api.OnOff;
import io.openems.edge.apartmentmodule.api.Ready;
import io.openems.edge.apartmentmodule.api.ExternalRequest;
import io.openems.edge.apartmentmodule.api.Error;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Designate(ocd = Config.class, factory = true)
@Component(name = "ApartmentModule",
		immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE)

/**
 * This module reads all variables available via Modbus from a Consolinno Apartment Module and maps them to OpenEMS
 * channels. WriteChannels can be used to send commands to the Apartment Module via "setNextWriteValue" method.
 */

public class ApartmentModuleImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, EventHandler, ApartmentModuleChannel {

	private final Logger log = LoggerFactory.getLogger(ApartmentModuleImpl.class);

	private int testcounter = 0;

	@Reference
	protected ConfigurationAdmin cm;

	// This is essential for Modbus to work, but the compiler does not warn you when it is missing!
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public ApartmentModuleImpl() {
		super(OpenemsComponent.ChannelId.values(),
				ApartmentModuleChannel.ChannelId.values());
	}


	@Activate
	public void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbusBridgeId());
	}


	@Deactivate
	public void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {

		return new ModbusProtocol(this,
				new FC3ReadRegistersTask(0, Priority.HIGH,
						m(ApartmentModuleChannel.ChannelId.HR_1_EXTERNAL_REQUEST, new UnsignedWordElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_2_ERROR, new UnsignedWordElement(1),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_3_COMMUNICATION, new UnsignedWordElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(9, Priority.HIGH,
						// Use SignedWordElement when the number can be negative. Signed 16bit maps every number >32767
						// to negative. That means if the value you read is positive and <32767, there is no difference
						// between signed and unsigned.
						m(ApartmentModuleChannel.ChannelId.HR_10_TEMPERATURE, new SignedWordElement(9),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(20, Priority.HIGH,
						m(ApartmentModuleChannel.ChannelId.HR_21_STATE_RELAY1, new UnsignedWordElement(20),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_22_STATE_RELAY2, new UnsignedWordElement(21),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(30, Priority.HIGH,
						m(ApartmentModuleChannel.ChannelId.HR_31_COMMAND_RELAY1, new UnsignedWordElement(30),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_32_COMMAND_RELAY2, new UnsignedWordElement(31),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(40, Priority.HIGH,
						m(ApartmentModuleChannel.ChannelId.HR_41_TIMING_RELAY1, new UnsignedWordElement(40),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_42_TIMING_RELAY2, new UnsignedWordElement(41),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(50, Priority.HIGH,
						m(ApartmentModuleChannel.ChannelId.HR_51_TIME_REMAINING_RELAY1, new UnsignedWordElement(50),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_52_TIME_REMAINING_RELAY2, new UnsignedWordElement(51),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),

				// Modbus write tasks take the "setNextWriteValue" value of a channel and send them to the device.
				// Modbus read tasks put values in the "setNextValue" field, which get automatically transferred to the
				// "value" field of the channel. By default, the "setNextWriteValue" field is NOT copied to the
				// "setNextValue" and "value" field. In essence, this makes "setNextWriteValue" and "setNextValue"/"value"
				// two separate channels.
				// That means: Modbus read tasks will not overwrite any "setNextWriteValue" values. You do not have to
				// watch the order in which you call read and write tasks.
				// Also: if you do not add a Modbus read task for a write channel, any "setNextWriteValue" values will
				// not be transferred to the "value" field of the channel, unless you add code that does that.
				new FC16WriteRegistersTask(0,
						m(ApartmentModuleChannel.ChannelId.HR_1_EXTERNAL_REQUEST, new UnsignedWordElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC16WriteRegistersTask(2,
						m(ApartmentModuleChannel.ChannelId.HR_3_COMMUNICATION, new UnsignedWordElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC16WriteRegistersTask(30,
						m(ApartmentModuleChannel.ChannelId.HR_31_COMMAND_RELAY1, new UnsignedWordElement(30),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_32_COMMAND_RELAY2, new UnsignedWordElement(31),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC16WriteRegistersTask(40,
						m(ApartmentModuleChannel.ChannelId.HR_41_TIMING_RELAY1, new UnsignedWordElement(40),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(ApartmentModuleChannel.ChannelId.HR_42_TIMING_RELAY2, new UnsignedWordElement(41),
								ElementToChannelConverter.DIRECT_1_TO_1)
				)
		);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
				//channeltest();	// Just for testing
				break;
		}
	}

	// Just for testing. Also, example code with some explanations.
	protected void channeltest() {
		this.logInfo(this.log, "--Testing Channels--");
		this.logInfo(this.log, "External Request: " + getSetExternalRequest().value().asEnum().getName()); // Gets the "name" field of the Enum.
		this.logInfo(this.log, "Error: " + getError().value().asEnum().getName());
		this.logInfo(this.log, "Communication: " + getSetCommunication().value().asEnum().getName());
		this.logInfo(this.log, "Temperature: " + getTemperature().value().orElse(0) / 10 + "°C");
		this.logInfo(this.log, "State of Relay1: " + getStateRelay1().value().asEnum().getName());
		this.logInfo(this.log, "State of Relay2: " + getStateRelay2().value().asEnum().getName());
		this.logInfo(this.log, "Command for Relay1: " + getSetCommandRelay1().value().asEnum().getName());
		this.logInfo(this.log, "Command for Relay2: " + getSetCommandRelay2().value().asEnum().getName());
		this.logInfo(this.log, "Timing for Relay1: " + getSetTimingRelay1().value().orElse(0) / 100 + " s");
		this.logInfo(this.log, "Timing for Relay2: " + getSetTimingRelay2().value().orElse(0) / 100 + " s");
		this.logInfo(this.log, "Countdown for Relay1: " + getCountdownRelay1().value().orElse(0) / 100 + " s");
		this.logInfo(this.log, "Countdown for Relay2: " + getCountdownRelay2().value().orElse(0) / 100 + " s");
		this.logInfo(this.log, "");


		// Test Modbus write. Example using Enum name field to set value. In effect, this writes an integer.
		if (testcounter == 5) {
			this.logInfo(this.log, "Turn on relay 1.");
			this.logInfo(this.log, "");
			try {
				getSetCommandRelay1().setNextWriteValue(OnOff.ON.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set relay 1 to on.");
			}
		}

		if (testcounter == 10) {
			this.logInfo(this.log, "Turn off relay 1.");
			this.logInfo(this.log, "");
			try {
				getSetCommandRelay1().setNextWriteValue(OnOff.OFF.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set relay 1 to off.");
			}
		}


		testcounter++;

	}




}
