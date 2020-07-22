package io.openems.edge.controller.api.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.google.common.base.CaseFormat;
import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.jsonrpc.base.JsonrpcRequest;
import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;
import io.openems.common.session.User;
import io.openems.common.worker.AbstractWorker;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.jsonapi.JsonApi;
import io.openems.edge.common.meta.Meta;
import io.openems.edge.common.modbusslave.*;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.api.core.ApiWorker;
import io.openems.edge.controller.api.core.WritePojo;
import io.openems.edge.controller.api.modbus.jsonrpc.GetModbusProtocolExportXlsxRequest;
import io.openems.edge.controller.api.modbus.jsonrpc.GetModbusProtocolExportXlsxResponse;
import io.openems.edge.controller.api.modbus.jsonrpc.GetModbusProtocolRequest;
import io.openems.edge.controller.api.modbus.jsonrpc.GetModbusProtocolResponse;
import io.openems.edge.timedata.api.Timedata;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * This is the Modbus Custom module.
 * This Modbus module allows you to map channels from any active OpenEMS module to a Modbus register. The differences to
 * the non-Custom, regular Modbus module are as following.
 *
 * Non-custom Modbus:
 * - To make a component's data available using Modbus, you only need to enter the component-ID in the Modbus UI.
 * - The component needs to implement the ModbusSlave interface for this to work. If a component does not implement that,
 *   you can't use it with Modbus non-custom.
 * - Fixed mapping of channels. What channels are made available and at which register address is not an UI option.
 * - Access mode can be selected, but applies to all channels.
 *
 * Modbus Custom:
 * - Works with any channel of any component. No code required in the component you want to connect to Modbus.
 * - The UI lets you choose which channels to make available. Allows custom configurations with the same code base.
 * - For each channel, you can individually set register address, access mode and data format.
 * - You need to know the name of the channel you want to connect to Modbus. This information is not available in the UI.
 * - Because of UI limitations, the configuration needs to be entered as a string. This is prone to errors.
 *
 * The crucial part - how to correctly enter the channel configuration in the UI:
 * This is about the field "Channels" in the UI. In this field you enter the channel you want to connect to Modbus, what
 * Modbus address you want to map it to and the data format. This is entered into the field in the following format:
 *
 *                            RegisterAddress/ModbusType/Component-ID/Channel-ID/AccessMode
 *
 * That is five parameters, separated by four "/". The "AccessMode" parameter is optional and can be omitted.
 * Using the "Simulator GridMeter Acting" module from the "OpenEMS - getting startet" example
 * (https://openems.github.io/openems.io/openems/latest/gettingstarted.html), a valid input would be:
 *
 *                            0/UINT16/meter0/SIMULATED_ACTIVE_POWER/read_only
 *
 * If the entry is not in the required format, it will be discarded. The code has error correction, so there is some
 * leeway regarding format errors. Three "/" are needed (AccessMode is optional). Less will result in a discard of the
 * entry, more will not. Anything entered after a fifth "/" will simply be ignored.
 * Detailed description of the parameters:
 *
 * - "RegisterAddress" is the Modbus holding register you want to map the channel to. This needs to be an integer
 *   between 0 and 9998 (inclusive). If this parameter is not within bounds, not a number, left blank or already in use,
 *   the code will automatically assign the lowest possible free register address. The assigned address is announced in
 *   the log. The log will also display a summary of assigned addresses.
 *   A channel is always mapped to both input and holding registers, no matter if the channel is a read or write channel.
 *
 * - "ModbusType" is how to convert the channel. Available options: UINT16, FLOAT32, FLOAT64 and STRING16. Lower case
 *   input is ok too.
 *   IMPORTANT: When reading a multiple register type, you need to read all the registers in one call. Reading a single
 *   register or the wrong range will result in an "Illegal Data Address exception response". This applies to the
 *   non-custom Modbus variants as well.
 *   Example - STRING16 mapped to address 0. If you read registers 0 to 15, you get data. If you read register 0 alone,
 *   you get an error.
 *   UINT16: An integer with 16 bit (=short), mapped to one register.
 *   FLOAT32: A 32 bit float, mapped to two 16 bit registers in big endian format.
 *            Example - mapping an integer channel that contains 4 to address 0 with FLOAT32.
 *            Output is then 0x4080 on address 0 and 0x0000 on address 1.
 *   FLOAT64: A 64 bit float, mapped to four 16 bit registers in big endian format.
 *   STRING16: A string coded in ASCII mapped to 16 16 bit registers in big endian format. This allows a string of 32
 *             characters length to be transmitted.
 *             Example - STRING16 mapped to register 0, transmitting "OpenEMS Association e.V." (without the quotation marks).
 *             Output is then (from register 0 to 15): 0x4F70, 0x656E, 0x454D, 0x5320, 0x4173, 0x736F, 0x6369, 0x6174, 0x696F,
 *             0x6E20, 0x652E, 0x562E, 0x0000, 0x0000, 0x0000, 0x0000.
 *   Note that the "ModbusType" parameter affects the register mapping, as types that use multiple registers block
 *   registers following the address assigned to them.
 *   Data conversion: standard OpenEMS channel data conversion is active. That means a boolean channel mapped to UINT16
 *   will output 0 for false and 1 for true.
 *   Format Error: If the "ModbusType" parameter is not valid or left blank, the error handling code will automatically
 *   assign a type based on the channel data type.
 *   boolean or short -> UINT16.
 *   integer or float -> FLOAT32
 *   long or double -> FLOAT64
 *   string -> STRING16
 *
 * - "Component-ID" is the Id of the module that contains the channel. The module must be enabled.
 *
 * - "Channel-ID" is the Id of the channel, entered as UPPER_UNDERSCORE or UpperCamel.
 *
 * - "AccessMode" defines the privilege of the Modbus connection. Available options: READ_ONLY, READ_WRITE and WRITE_ONLY.
 *   Can be entered in upper or lower case. Optional parameter. If left blank (or misspelled), READ_WRITE is used.
 *   A mismatch with the channel access mode (like Modbus WRITE_ONLY for a READ_ONLY channel) will default back to the
 *   channel access mode.
 *
 * Summary of error handling: The input "//meter0/SIMULATED_ACTIVE_POWER" would still work. The error handling code will
 * fill in the blanks with address 0, Modbus type FLOAT32 and AccessMode READ_WRITE.
 *
 *
 */
@Designate(ocd = ConfigSerialCustom.class, factory = true)
@Component(//
		name = "Controller.Api.ModbusSerialCustom", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ModbusSerialApiCustom extends AbstractOpenemsComponent implements Controller, OpenemsComponent, JsonApi, ModbusSlaveApi {

	public static final int UNIT_ID = 1;

	private final Logger log = LoggerFactory.getLogger(ModbusSerialApiCustom.class);

	private final ApiWorker apiWorker = new ApiWorker();
	private final MyProcessImage processImage;

	private SerialParameters serialParameters;

	/**
	 * Holds the link between Modbus address and ModbusRecord.
	 */
	protected final TreeMap<Integer, ModbusRecord> records = new TreeMap<>();

	/**
	 * Holds the link between Modbus start address of a Component and the
	 * Component-ID.
	 */
	protected final TreeMap<Integer, String> components = new TreeMap<>();

	private String[] componentIds;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected Meta metaComponent = null;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	protected void addComponent(OpenemsComponent component) {
		this._components.put(component.id(), component);
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	protected volatile Timedata timedataService = null;

	@Reference
	protected ConfigurationAdmin cm;

	@Reference
	protected ComponentManager cpm;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		UNABLE_TO_START(Doc.of(Level.FAULT) //
				.text("Unable to start Modbus/Serial-Api Server"));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	protected volatile Map<String, OpenemsComponent> _components = new HashMap<>();
	private ConfigSerialCustom config = null;

	public ModbusSerialApiCustom() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);

		this.processImage = new MyProcessImage(this);
	}

	@Activate
	void activate(ComponentContext context, ConfigSerialCustom config) throws ModbusException, OpenemsException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;

		serialParameters = new SerialParameters(config.portName(), config.baudRate(), config.flowControlIn().getValue(),
				config.flowControlOut().getValue(), config.databits(), config.stopbits().getValue(), config.parity().getValue(), config.echo());

		// Verify that the input is of correct format. Abort if it is not.
		if (processChannelInput(config.channel_input())) {
			return;
		}

		// update filter for 'components'
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "Component", this.componentIds)) {
			return;
		}

		this.apiWorker.setTimeoutSeconds(config.apiTimeout());

		if (!this.isEnabled()) {
			// abort if disabled
			return;
		}

		printModbusAddressMapping();

		// Start Modbus-Server
		this.startApiWorker.activate(config.id());
	}

	@Deactivate
	protected void deactivate() {
		this.startApiWorker.deactivate();
		ModbusSlaveFactory.close();
		super.deactivate();
	}

	private final AbstractWorker startApiWorker = new AbstractWorker() {

		private static final int DEFAULT_WAIT_TIME = 5000; // 5 seconds

		private final Logger log = LoggerFactory.getLogger(AbstractWorker.class);

		private com.ghgande.j2mod.modbus.slave.ModbusSlave slave = null;

		@Override
		protected void forever() {
			String port = ModbusSerialApiCustom.this.config.portName();
			if (this.slave == null) {
				try {
					// start new server
					this.slave = ModbusSlaveFactory.createSerialSlave(serialParameters);

					slave.addProcessImage(UNIT_ID, ModbusSerialApiCustom.this.processImage);
					slave.open();

					ModbusSerialApiCustom.this.logInfo(this.log, "Modbus/SerialApi started on port [" + port + "] with UnitId ["
							+ ModbusSerialApiCustom.UNIT_ID + "].");
					ModbusSerialApiCustom.this.channel(ChannelId.UNABLE_TO_START).setNextValue(false);
				} catch (ModbusException e) {
					ModbusSlaveFactory.close();
					ModbusSerialApiCustom.this.logError(this.log,
							"Unable to start Modbus/Serial Api on port [" + port + "]: " + e.getMessage());
					ModbusSerialApiCustom.this.channel(ChannelId.UNABLE_TO_START).setNextValue(true);
				}

			} else {
				// regular check for errors
				String error = slave.getError();
				if (error != null) {
					ModbusSerialApiCustom.this.logError(this.log,
							"Unable to start Modbus/Serial Api on port [" + port + "]: " + error);
					ModbusSerialApiCustom.this.channel(ChannelId.UNABLE_TO_START).setNextValue(true);
					this.slave = null;
					// stop server
					ModbusSlaveFactory.close();
				}
			}
		}

		@Override
		protected int getCycleTime() {
			return DEFAULT_WAIT_TIME;
		}

	};


	/**
	 * This method takes the string array "channelInput" of the config entry "Channels" and processes the contents.
	 *
	 * @param channelInput string array containing input from the user.
	 */
	private boolean processChannelInput(String[] channelInput) {
		componentIds = new String[channelInput.length];
		int counter = 0;

		for (String entry : channelInput) {
			String[] inputArray;
			String registerAddressString;
			String modbusTypeString;
			String componentIdString;
			String channelIdString;
			String accessModeString;

			try {
				inputArray = entry.split("/");

				// Just for debugging.
				int arrayLine = 0;
				this.logDebug(this.log, "Debugging Channels input:");
				for (String entryOfArray : inputArray) {
					switch (arrayLine) {
						case 0:
							this.logDebug(this.log, "Address: " + entryOfArray);
							break;
						case 1:
							this.logDebug(this.log, "ModbusType: " + entryOfArray);
							break;
						case 2:
							this.logDebug(this.log, "component-ID: " + entryOfArray);
							break;
						case 3:
							this.logDebug(this.log, "channel-ID: " + entryOfArray);
							break;
						case 4:
							this.logDebug(this.log, "AccessMode: " + entryOfArray);
							break;
					}
					arrayLine++;
				}

				registerAddressString = inputArray[0].trim();
				modbusTypeString = inputArray[1].trim();
				componentIdString = inputArray[2].trim();
				channelIdString = inputArray[3].trim();
				try {
					accessModeString = inputArray[4].trim();
				} catch (Exception e) {
					accessModeString = "READ_WRITE";
				}
			} catch (Exception e) {
				this.logWarn(this.log, "Wrong format in configuration option \"Channels\", line "
						+ (counter + 1) + ". Discarding entry.");
				continue;
			}

			// Check if Module exists and is active.
			OpenemsComponent componentEntry;
			try {
				boolean componentExistsAndIsEnabled = cpm.getComponent(componentIdString) instanceof OpenemsComponent
						&& cpm.getComponent(componentIdString).isEnabled();
				if (componentExistsAndIsEnabled) {
					componentEntry = cpm.getComponent(componentIdString);
					addComponent(componentEntry);
				} else {
					this.logWarn(this.log, "Bad entry in configuration option \"Channels\", line "
							+ (counter + 1) + ": [" + componentIdString + "] is not a valid component-ID. Discarding entry.");
					continue;
				}
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logWarn(this.log, "Bad entry in configuration option \"Channels\", line "
						+ (counter + 1) + ": [" + componentIdString + "] is not a valid component-ID. Discarding entry.");
				continue;
			}

			// Check if Channel exists.
			Channel<?> channelEntry;
			// The channel Id needs to be in UpperCamel format.
			if (channelIdString.toUpperCase().equals(channelIdString)) {
				channelIdString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, channelIdString);
			}
			try {
				channelEntry = componentEntry.channel(channelIdString);
			} catch (Exception e) {
				this.logWarn(this.log, "Bad entry in configuration option \"Channels\", line "
						+ (counter + 1) + ": [" + channelIdString + "] is not a valid channel of component ["
						+ componentIdString + "]. Discarding entry");
				this.logWarn(this.log, e.toString());
				continue;
			}

			// Verify and parse Modbus Type.
			modbusTypeString = modbusTypeString.toUpperCase();
			ModbusType modbusTypeEntry = ModbusType.UINT16;
			switch (modbusTypeString) {
				case "UINT16":
					modbusTypeEntry = ModbusType.UINT16;
					break;
				case "FLOAT32":
					modbusTypeEntry = ModbusType.FLOAT32;
					break;
				case "FLOAT64":
					modbusTypeEntry = ModbusType.FLOAT64;
					break;
				case "STRING16":
					modbusTypeEntry = ModbusType.STRING16;
					break;
				default:
					this.logWarn(this.log, "Bad entry in configuration option \"Channels\", line "
							+ (counter + 1) + ": [" + modbusTypeString + "] is not a valid Modbus type. "
							+ "Assigning Modbus type based on channel type.");
					switch (channelEntry.getType()) {
						case BOOLEAN:
						case SHORT:
							modbusTypeEntry = ModbusType.UINT16;
							break;
						case INTEGER:
						case FLOAT:
							modbusTypeEntry = ModbusType.FLOAT32;
							break;
						case LONG:
						case DOUBLE:
							modbusTypeEntry = ModbusType.FLOAT64;
							break;
						case STRING:
							modbusTypeEntry = ModbusType.STRING16;
							break;
					}
					this.logWarn(this.log, "Channel " + channelIdString + " is of type " + channelEntry.getType().toString()
							+ ". Assigning Modbus type " + modbusTypeEntry.toString() + ".");
			}

			// Verify and parse address.
			int registerAddress;
			try {
				registerAddress = Integer.parseInt(registerAddressString.trim());
			} catch (NumberFormatException e) {
				this.logWarn(this.log, "Wrong format in configuration option \"Channels\", line "
						+ (counter + 1) + ": [" + registerAddressString + "] is not a number. Assigning next free address.");
				registerAddress = mapToNextFreeAddress(modbusTypeEntry);
				this.logWarn(this.log, "Assigned " + componentIdString + "/" + channelIdString
						+ " to Modbus address " + registerAddress + ".");
			}
			if (registerAddress < 0 || (registerAddress + modbusTypeEntry.getWords() - 1) > 9998) {
				this.logWarn(this.log, "Wrong format in configuration option \"Channels\", line "
						+ (counter + 1) + ": [" + registerAddress + "] is not a possible Modbus holding register address. "
						+ "Assigning next free address.");
				registerAddress = mapToNextFreeAddress(modbusTypeEntry);
				this.logWarn(this.log, "Assigned " + componentIdString + "/" + channelIdString
						+ " to Modbus address " + registerAddress + ".");
			}

			// Verify that the intended address range is free to use.
			if (records.isEmpty() == false) {

				// Check adjacent lower entry.
				if (records.floorKey(registerAddress) != null) {
					int usedRangeLowerEntry = records.floorKey(registerAddress)
							+ records.floorEntry(registerAddress).getValue().getType().getWords() - 1;
					if (usedRangeLowerEntry >= registerAddress) {
						this.logWarn(this.log, "Modbus register address " + registerAddress + " is already used. Each "
								+ "channel must have a unique address! Assigning next free address.");
						registerAddress = mapToNextFreeAddress(modbusTypeEntry);
						this.logWarn(this.log, "Assigned " + componentIdString + "/" + channelIdString
								+ " to Modbus address " + registerAddress + ".");
					}
				}

				// Check adjacent higher entry.
				if (records.ceilingKey(registerAddress) != null) {
					int usedRangeThisEntry = registerAddress + modbusTypeEntry.getWords() - 1;
					if (usedRangeThisEntry >= records.ceilingKey(registerAddress)) {
						this.logWarn(this.log, "Cannot fit channel " + componentIdString + "/"
								+ channelIdString +	" in Modbus register address " + registerAddress + ". Not enough "
								+ "unassigned registers before the next entry. Assigning next free address.");
						registerAddress = mapToNextFreeAddress(modbusTypeEntry);
						this.logWarn(this.log, "Assigned " + componentIdString + "/" + channelIdString
								+ " to Modbus address " + registerAddress + ".");
					}
				}
			}

			// Verify and parse access mode.
			accessModeString = accessModeString.toUpperCase();
			AccessMode accessModeEntry;
			switch (accessModeString) {
				case "READ_WRITE":
					accessModeEntry = AccessMode.READ_WRITE;
					break;
				case "READ_ONLY":
					accessModeEntry = AccessMode.READ_ONLY;
					break;
				case "WRITE_ONLY":
					accessModeEntry = AccessMode.WRITE_ONLY;
					break;
				default:
					this.logWarn(this.log, "Bad entry in configuration option \"Channels\", line "
							+ (counter + 1) + ": [" + accessModeString + "] is not a valid access mode. "
							+ "Assigning default access mode READ_WRITE.");
					accessModeEntry = AccessMode.READ_WRITE;
			}

			// Build entry
			ModbusRecordChannel recordChannelEntry = new ModbusRecordChannel(0, modbusTypeEntry, channelEntry.channelId(), accessModeEntry);
			componentIds[counter] = componentIdString;
			this.components.put(registerAddress, componentEntry.alias());
			addRecordToProcessImage(registerAddress, recordChannelEntry, componentEntry);

			counter++;
		}
		if (this.records.isEmpty()) {
			this.logError(this.log, "No channels defined to map to Modbus. Deactivating.");
			return true;
		}
		return false;
	}


	/**
	 * Finds the lowest unoccupied Modbus register address in "records" that has enough following unoccupied
	 * registers to fit the specified Modbus type.
	 *
	 * @param type the Modbus type (UINT16, FLOAT32, ...), so the method knows how many registers are needed.
	 * @return the register address.
	 */
	private int mapToNextFreeAddress(ModbusType type) {
		int previousKey = 0;
		int previousKeyLength = 0;
		boolean previousKeyExists = false;
		int neededRegisters = type.getWords();

		// Travers the map and see if a gap is big enough.
		for (Map.Entry<Integer, ModbusRecord> entry : records.entrySet()) {
			int gap = entry.getKey() - previousKey - previousKeyLength;
			if (gap >= neededRegisters) {
				return previousKey + previousKeyLength;
			}

			previousKeyExists = true;
			previousKey = entry.getKey();
			previousKeyLength = entry.getValue().getType().getWords();
		}

		// If there is no suitable gap in the map, return next valid address after the last entry.
		if (previousKeyExists) {
			return previousKey + previousKeyLength;
		} else {
			// You land here if the map (records) is empty.
			return 0;
		}
	}


	private void printModbusAddressMapping() {
		this.logInfo(this.log, "");
		this.logInfo(this.log, "--Modbus address mapping--");
		boolean notice = false;
		for (Map.Entry<Integer, ModbusRecord> entry : records.entrySet()) {
			String keyRange = "";
			switch (entry.getValue().getType()) {
				case UINT16:
					keyRange = entry.getKey().toString();
					break;
				case FLOAT32:
					keyRange = entry.getKey() + "-" + (entry.getKey() + 1);
					notice = true;
					break;
				case FLOAT64:
					keyRange = entry.getKey() + "-" + (entry.getKey() + 3);
					notice = true;
					break;
				case STRING16:
					keyRange = entry.getKey() + "-" + (entry.getKey() + 15);
					notice = true;
					break;
			}
			this.logInfo(this.log, keyRange + " - " + entry.getValue().getType().toString() + " - "
					+ entry.getValue().getName() + " - " + entry.getValue().getAccessMode().toString());
		}
		this.logInfo(this.log, "");
		if (notice) {
			this.logInfo(this.log, "Note: Channels mapped to multiple registers can not be read one register at "
					+ "a time.");
			this.logInfo(this.log, "For example, a FLOAT32 is mapped to address 0. Then you need to read the "
					+ "address range 0-1 to get a value. Reading a single register at address 0 or 1 will return nothing.");
			this.logInfo(this.log, "");
		}
	}


	/**
	 * Adds a Record to the process image at the given address.
	 * 
	 * @param address   the address
	 * @param record    the record
	 * @param component the OpenEMS Component
	 * @return the next address after this record
	 */
	private int addRecordToProcessImage(int address, ModbusRecord record, OpenemsComponent component) {
		record.setComponentId(component.id());

		// Handle writes to the Channel; limited to ModbusRecordChannels
		if (record instanceof ModbusRecordChannel) {
			ModbusRecordChannel r = (ModbusRecordChannel) record;
			r.onWriteValue((value) -> {
				Channel<?> readChannel = component.channel(r.getChannelId());
				if (!(readChannel instanceof WriteChannel)) {
					this.logWarn(this.log, "Unable to write to Read-Only-Channel [" + readChannel.address() + "]");
					return;
				}
				WriteChannel<?> channel = (WriteChannel<?>) readChannel;
				this.apiWorker.addValue(channel, new WritePojo(value));
			});
		}

		this.records.put(address, record);
		return address + record.getType().getWords();
	}

	@Override
	public void run() throws OpenemsNamedException {
		this.apiWorker.run();
	}

	@Override
	public void logDebug(Logger log, String message) {
		super.logDebug(log, message);
	}

	@Override
	public void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	public void logWarn(Logger log, String message) {
		super.logWarn(log, message);
	}

	@Override
	public CompletableFuture<JsonrpcResponseSuccess> handleJsonrpcRequest(User user, JsonrpcRequest message)
			throws OpenemsNamedException {
		switch (message.getMethod()) {
		case GetModbusProtocolRequest.METHOD:
			return CompletableFuture.completedFuture(new GetModbusProtocolResponse(message.getId(), this.records));

		case GetModbusProtocolExportXlsxRequest.METHOD:
			return CompletableFuture.completedFuture(
					new GetModbusProtocolExportXlsxResponse(message.getId(), this.components, this.records));

		}
		return null;
	}

	@Override
	public TreeMap<Integer, ModbusRecord> getRecords() {
		return records;
	}

	@Override
	public Map<String, OpenemsComponent> getComponents() {
		return _components;
	}
}
