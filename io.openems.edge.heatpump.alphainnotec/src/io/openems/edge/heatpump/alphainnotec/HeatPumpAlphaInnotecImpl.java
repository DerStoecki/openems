package io.openems.edge.heatpump.alphainnotec;

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
import io.openems.edge.heatpump.alphainnotec.api.CurrentState;
import io.openems.edge.heatpump.alphainnotec.api.HeatpumpAlphaInnotecChannel;
import io.openems.edge.heatpump.smartgrid.generalized.api.HeatpumpSmartGridGeneralizedChannel;
import io.openems.edge.heatpump.smartgrid.generalized.api.SmartGridState;
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
@Component(name = "HeatPumpAlphaInnotec",
		immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE)

/**
 * This module reads all variables available via Modbus from an Alpha Innotec heat pump and maps them to OpenEMS
 * channels. WriteChannels can be used to send commands to the heat pump via "setNextWriteValue" method.
 */
public class HeatPumpAlphaInnotecImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, EventHandler, HeatpumpAlphaInnotecChannel {

	private final Logger log = LoggerFactory.getLogger(HeatPumpAlphaInnotecImpl.class);
	private int testcounter = 0;

	@Reference
	protected ConfigurationAdmin cm;

	// This is essential for Modbus to work, but the compiler does not warn you when it is missing!
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public HeatPumpAlphaInnotecImpl() {
		super(OpenemsComponent.ChannelId.values(),
				HeatpumpAlphaInnotecChannel.ChannelId.values(),
				HeatpumpSmartGridGeneralizedChannel.ChannelId.values());	// Even though HeatpumpAlphaInnotecChannel extends this channel, it needs to be added separately.
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
				new FC2ReadInputsTask(0, Priority.LOW,
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_0_EVU, new CoilElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_1_EVU2, new CoilElement(1),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_2_SWT, new CoilElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_3_VD1, new CoilElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_4_VD2, new CoilElement(4),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_5_ZWE1, new CoilElement(5),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_6_ZWE2, new CoilElement(6),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.DI_7_ZWE3, new CoilElement(7),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC4ReadInputRegistersTask(0, Priority.LOW,
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_0_MITTELTEMP, new UnsignedWordElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_1_VORLAUFTEMP, new UnsignedWordElement(1),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_2_RUECKLAUFTEMP, new UnsignedWordElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_3_RUECKEXTERN, new UnsignedWordElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_4_TRINKWWTEMP, new UnsignedWordElement(4),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_5_MK1VORLAUF, new UnsignedWordElement(5),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_6_MK2VORLAUF, new UnsignedWordElement(6),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_7_MK3VORLAUF, new UnsignedWordElement(7),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_8_HEISSGASTEMP, new UnsignedWordElement(8),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_9_WQEINTRITT, new UnsignedWordElement(9),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_10_WQAUSTRITT, new UnsignedWordElement(10),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_11_RAUMFV1, new UnsignedWordElement(11),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_12_RAUMFV2, new UnsignedWordElement(12),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_13_RAUMFV3, new UnsignedWordElement(13),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_14_SOLARKOLLEKTOR, new UnsignedWordElement(14),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_15_SOLARSPEICHER, new UnsignedWordElement(15),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_16_EXTEQ, new UnsignedWordElement(16),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_17_ZULUFTTEMP, new UnsignedWordElement(17),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_18_ABLUFTTEMP, new UnsignedWordElement(18),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_19_ANSAUGTEMPVDICHTER, new UnsignedWordElement(19),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_20_ANSAUGTEMPVDAMPFER, new UnsignedWordElement(20),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_21_TEMPVDHEIZUNG, new UnsignedWordElement(21),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_22_UEBERHITZ, new UnsignedWordElement(22),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_23_UEBERHITZSOLL, new UnsignedWordElement(23),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_24_RBERAUMTEMPIST, new UnsignedWordElement(24),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_25_RBERAUMTEMPSOLL, new UnsignedWordElement(25),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_26_DRUCKHD, new UnsignedWordElement(26),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_27_DRUCKND, new UnsignedWordElement(27),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_28_TVD1, new UnsignedWordElement(28),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_29_TVD2, new UnsignedWordElement(29),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_30_TZWE1, new UnsignedWordElement(30),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_31_TZWE2, new UnsignedWordElement(31),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_32_TZWE3, new UnsignedWordElement(32),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_33_TWAERMEPUMPE, new UnsignedWordElement(33),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_34_THEIZUNG, new UnsignedWordElement(34),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_35_TTRINKWW, new UnsignedWordElement(35),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_36_TSWOPV, new UnsignedWordElement(36),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_37_STATUS, new UnsignedWordElement(37),
								ElementToChannelConverter.DIRECT_1_TO_1),
						// A double word combines two 16 bit registers to a 32 bit value. This reads two registers, so
						// the next element address is +2 instead of +1 for a regular register.
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_38_WHHEIZUNG, new UnsignedDoublewordElement(38),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_40_WHTRINKWW, new UnsignedDoublewordElement(40),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_42_WHPOOL, new UnsignedDoublewordElement(42),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_44_WHTOTAL, new UnsignedDoublewordElement(44),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.IR_46_ERROR, new UnsignedWordElement(46),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC1ReadCoilsTask(0, Priority.LOW,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_0_ERRORRESET, new CoilElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						// A Modbus read commands reads everything from start address to finish address. If there is a
						// gap, you must place a dummy element to fill the gap or end the read command there and start
						// with a new read where you want to continue.
						new DummyCoilElement(1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_2_HUP, new CoilElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_3_VEN, new CoilElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_4_ZUP, new CoilElement(4),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_5_BUP, new CoilElement(5),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_6_BOSUP, new CoilElement(6),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_7_ZIP, new CoilElement(7),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_8_FUP2, new CoilElement(8),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_9_FUP3, new CoilElement(9),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_10_SLP, new CoilElement(10),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_11_SUP, new CoilElement(11),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_12_VSK, new CoilElement(12),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_13_FRH, new CoilElement(13),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				// There is no write multiple coils in OpenEMS, so you need a separate write call for each coil.
				new FC5WriteCoilTask(0,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_0_ERRORRESET, new CoilElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(2,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_2_HUP, new CoilElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(3,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_3_VEN, new CoilElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(4,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_4_ZUP, new CoilElement(4),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(5,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_5_BUP, new CoilElement(5),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(6,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_6_BOSUP, new CoilElement(6),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(7,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_7_ZIP, new CoilElement(7),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(8,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_8_FUP2, new CoilElement(8),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(9,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_9_FUP3, new CoilElement(9),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(10,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_10_SLP, new CoilElement(10),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(11,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_11_SUP, new CoilElement(11),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(12,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_12_VSK, new CoilElement(12),
						ElementToChannelConverter.DIRECT_1_TO_1)),
				new FC5WriteCoilTask(13,
						m(HeatpumpAlphaInnotecChannel.ChannelId.COIL_13_FRH, new CoilElement(13),
						ElementToChannelConverter.DIRECT_1_TO_1)),

				new FC3ReadRegistersTask(0, Priority.LOW,
						// Use SignedWordElement when the number can be negative. Signed 16bit maps every number >32767
						// to negative. That means if the value you read is positive and <32767, there is no difference
						// between signed and unsigned.
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_0_OUTSIDETEMP, new SignedWordElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_1_RUECKTEMPSOLL, new UnsignedWordElement(1),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_2_MK1VORTEMPSOLL, new UnsignedWordElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_3_MK2VORTEMPSOLL, new UnsignedWordElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_4_MK3VORTEMPSOLL, new UnsignedWordElement(4),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_5_TRINKWWTEMPSOLL, new UnsignedWordElement(5),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_6_RUNCLEARANCE, new UnsignedWordElement(6),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_7_HEIZUNGRUNSTATE, new UnsignedWordElement(7),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_8_TRINKWWRUNSTATE, new UnsignedWordElement(8),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_9_MK2RUNSTATE, new UnsignedWordElement(9),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_10_MK3RUNSTATE, new UnsignedWordElement(10),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_11_COOLINGRUNSTATE, new UnsignedWordElement(11),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_12_VENTILATIONRUNSTATE, new UnsignedWordElement(12),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_13_POOLRUNSTATE, new UnsignedWordElement(13),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(getsetSmartGridState().channelId(), new UnsignedWordElement(14),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_15_HKHEIZUNGENDPKT, new UnsignedWordElement(15),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_16_HKHEIZUNGPARAVER, new UnsignedWordElement(16),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_17_HKMK1ENDPKT, new UnsignedWordElement(17),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_18_HKMK1PARAVER, new UnsignedWordElement(18),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_19_HKMK2ENDPKT, new UnsignedWordElement(19),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_20_HKMK2PARAVER, new UnsignedWordElement(20),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_21_HKMK3ENDPKT, new UnsignedWordElement(21),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_22_HKMK3PARAVER, new UnsignedWordElement(22),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_23_TEMPPM, new SignedWordElement(23),
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
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_0_OUTSIDETEMP, new SignedWordElement(0),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_1_RUECKTEMPSOLL, new UnsignedWordElement(1),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_2_MK1VORTEMPSOLL, new UnsignedWordElement(2),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_3_MK2VORTEMPSOLL, new UnsignedWordElement(3),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_4_MK3VORTEMPSOLL, new UnsignedWordElement(4),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_5_TRINKWWTEMPSOLL, new UnsignedWordElement(5),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_6_RUNCLEARANCE, new UnsignedWordElement(6),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_7_HEIZUNGRUNSTATE, new UnsignedWordElement(7),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_8_TRINKWWRUNSTATE, new UnsignedWordElement(8),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_9_MK2RUNSTATE, new UnsignedWordElement(9),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_10_MK3RUNSTATE, new UnsignedWordElement(10),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_11_COOLINGRUNSTATE, new UnsignedWordElement(11),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_12_VENTILATIONRUNSTATE, new UnsignedWordElement(12),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_13_POOLRUNSTATE, new UnsignedWordElement(13),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(getsetSmartGridState().channelId(), new UnsignedWordElement(14),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_15_HKHEIZUNGENDPKT, new UnsignedWordElement(15),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_16_HKHEIZUNGPARAVER, new UnsignedWordElement(16),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_17_HKMK1ENDPKT, new UnsignedWordElement(17),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_18_HKMK1PARAVER, new UnsignedWordElement(18),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_19_HKMK2ENDPKT, new UnsignedWordElement(19),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_20_HKMK2PARAVER, new UnsignedWordElement(20),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_21_HKMK3ENDPKT, new UnsignedWordElement(21),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_22_HKMK3PARAVER, new UnsignedWordElement(22),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpAlphaInnotecChannel.ChannelId.HR_23_TEMPPM, new SignedWordElement(23),
								ElementToChannelConverter.DIRECT_1_TO_1)
				)
		);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
				channeltest();	// Just for testing
				channelmapping();
				break;
		}
	}

	// Put values in channels that are not directly Modbus read values but derivatives.
	protected void channelmapping() {

		CurrentState heatpumpCurrentState = getHeatpumpOperatingMode().value().asEnum();
		switch (heatpumpCurrentState) {
			case OFF:
				isReady().setNextValue(true);
				isRunning().setNextValue(false);
				break;
			case EVU_SPERRE:
			case UNDEFINED:
				isReady().setNextValue(false);
				isRunning().setNextValue(false);
				break;
			case ABTAUEN:
			case KUEHLUNG:
			case SCHWIMMBAD:
			case HEIZBETRIEB:
			case TRINKWARMWASSER:
			case EXTERNE_ENERGIEQUELLE:
				isReady().setNextValue(true);
				isRunning().setNextValue(true);
				break;
		}

		// The value can be null. "orElse" means "get value or use alternative value if value = null".
		if (getErrorCode().value().orElse(0) != 0) {
			noError().setNextValue(false);
		} else {
			noError().setNextValue(true);
		}

	}

	// Just for testing. Also, example code with some explanations.
	protected void channeltest() {
		this.logInfo(this.log, "--Testing Channels--");
		this.logInfo(this.log, "State: " + getHeatpumpOperatingMode().value().asEnum().getName());
		this.logInfo(this.log, "Smart Grid State name: " + getsetSmartGridState().value().asEnum().getName());	// Gets the "name" field of the Enum.
		this.logInfo(this.log, "Smart Grid State number: " + getsetSmartGridState().value().get());	// The variable in the channel is actually the integer of the Enum "value" field.
		this.logInfo(this.log, "Runclearance: " + getsetRunClearance().value().asEnum().getName());
		this.logInfo(this.log, "Heizung State: " + getsetHeizungOperationMode().value().asEnum().getName());
		this.logInfo(this.log, "Kühlung State: " + getsetCoolingOperationMode().value().asEnum().getName());
		this.logInfo(this.log, "Heizkurve MK1 Parallelversch.: " + getsetHeizkurveMK1Parallelverschiebung().value().get());
		this.logInfo(this.log, "Temp +- (signed): " + getsetTempPlusMinus().value().get());
		this.logInfo(this.log, "Mitteltemp: " + getMittelTemp().value().get());
		this.logInfo(this.log, "Vorlauftemp: " + getVorlaufTemp().value().get());
		this.logInfo(this.log, "Rücklauftemp: " + getRuecklaufTemp().value().get());
		this.logInfo(this.log, "Aussentemp (signed): " + getsetOutsideTemp().value().get());	// Test if variables that can be negative (signed) display correctly. Could not test as temperature was not negative.
		this.logInfo(this.log, "Rücklauftemp soll (unsigned): " + getsetRuecklaufTempSoll().value().get());
		this.logInfo(this.log, "Wärmemenge Heizung (double): " + getHeatAmountHeizung().value().get());	// Test if 32 bit integers (doubleword) are translated correctly.
		this.logInfo(this.log, "RBE ist: " + getRbeRaumtempIst().value().get());
		this.logInfo(this.log, "RBE soll: " + getRbeRaumtempSoll().value().get());
		this.logInfo(this.log, "EVU: " + isEVUactive().value().get());	// Not sure what this is doing. When testing, this was "true" when pump state said "cooling mode", even though pump state has a "EVU-Sperre" status.
		this.logInfo(this.log, "EVU2: " + isEVU2active().value().get());	// Not sure what this is doing. I expected setting smart grid status to "off" would trigger this, but it remained "false" when smart grid state was "off". Documentation says EVU2 = "true" when smart grid state = "off".
		this.logInfo(this.log, "Verdichter1: " + isVD1active().value().get());
		this.logInfo(this.log, "Verdichter2: " + isVD1active().value().get());
		this.logInfo(this.log, "ZWE3 (optional): " + isVD1active().value().get());	// Test what readings you get from Modbus variables that are not supported by the heat pump model.
		this.logInfo(this.log, "HUP: " + turnOnHUP().value().get());
		this.logInfo(this.log, "Error Code: " + getErrorCode().value().get());	// Code "0" means no error. "Null" means no reading (yet).
		this.logInfo(this.log, "");


		// Test Modbus write. Example using Enum name field to set value. In effect, this writes an integer.
		if (testcounter == 5) {
			this.logInfo(this.log, "Smart Grid off");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(SmartGridState.OFF.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to OFF.");
			}

		}

		if (testcounter == 10) {
			this.logInfo(this.log, "Smart Grid standard");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(SmartGridState.STANDARD.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to Standard.");
			}
		}

		// Test trying to write unsupported values (by Modbus device). Apparently nothing happens.
		if (testcounter == 15) {
			this.logInfo(this.log, "Smart Grid undefined");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(SmartGridState.UNDEFINED.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to Undefined.");
			}
			this.logInfo(this.log, "Channel setNextWriteValue: " + getsetSmartGridState().getNextWriteValue());
			this.logInfo(this.log, "");
		}

		if (testcounter == 20) {
			this.logInfo(this.log, "Smart Grid standard");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(SmartGridState.STANDARD.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to Standard.");
			}
		}

		// The channel is an Enum channel. But since Enum channels are Integer channels, you can just write an integer
		// in them. Better to use SmartGridState.OFF.getValue(), as this gives the reader more information what the
		// value does.
		if (testcounter == 25) {
			this.logInfo(this.log, "Smart Grid 0");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(0);
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to OFF.");
			}
		}

		if (testcounter == 30) {
			this.logInfo(this.log, "Smart Grid 2");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(2);
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to Standard.");
			}
		}

		// You can write any integer into an Enum channel. The value will be in the channel and will be sent as a
		// Modbus write, but the device will just ignore it if it is outside the valid values.
		// Beware that there is no warning or error message that tells you that the value is not valid. Creating
		// the channel as an Enum channel does not limit the input to valid values.
		if (testcounter == 35) {
			this.logInfo(this.log, "Smart Grid 6");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(6);
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to unreasonable values.");
			}
			this.logInfo(this.log, "Channel setNextWriteValue: " + getsetSmartGridState().getNextWriteValue());
			this.logInfo(this.log, "");
		}

		if (testcounter == 40) {
			this.logInfo(this.log, "Smart Grid standard");
			this.logInfo(this.log, "");
			try {
				getsetSmartGridState().setNextWriteValue(SmartGridState.STANDARD.getValue());
			} catch (OpenemsError.OpenemsNamedException e) {
				this.logError(this.log, "Unable to set SmartGridState to Standard.");
			}
		}


		testcounter++;


	}




}
