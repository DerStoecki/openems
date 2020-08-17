package io.openems.edge.heatpump.tecalor;

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
import io.openems.edge.heatpump.tecalor.api.HeatpumpTecalorChannel;
import io.openems.edge.heatpump.smartgrid.generalized.api.HeatpumpSmartGridGeneralizedChannel;
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
@Component(name = "HeatPumpTecalor",
		immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE)

/**
 * This module reads the most important variables available via Modbus from a Tecalor heat pump and maps them to OpenEMS
 * channels. WriteChannels can be used to send commands to the heat pump via "setNextWriteValue" method.
 */
public class HeatPumpTecalorImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, EventHandler, HeatpumpTecalorChannel {

	private final Logger log = LoggerFactory.getLogger(HeatPumpTecalorImpl.class);
	private int testcounter = 0;
	private boolean debug;

	@Reference
	protected ConfigurationAdmin cm;

	// This is essential for Modbus to work, but the compiler does not warn you when it is missing!
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public HeatPumpTecalorImpl() {
		super(OpenemsComponent.ChannelId.values(),
				HeatpumpTecalorChannel.ChannelId.values(),
				HeatpumpSmartGridGeneralizedChannel.ChannelId.values());
	}


	@Activate
	public void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbusBridgeId());
		debug = config.debug();
	}


	@Deactivate
	public void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {

		return new ModbusProtocol(this,
				new FC4ReadInputRegistersTask(506, Priority.LOW,
						// Use SignedWordElement when the number can be negative. Signed 16bit maps every number >32767
						// to negative. That means if the value you read is positive and <32767, there is no difference
						// between signed and unsigned.
						m(HeatpumpTecalorChannel.ChannelId.IR507_AUSSENTEMP, new SignedWordElement(506),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR508_ISTTEMPHK1, new SignedWordElement(507),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR509_SOLLTEMPHK1, new SignedWordElement(508),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR510_SOLLTEMPHK1, new SignedWordElement(509),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR511_ISTTEMPHK2, new SignedWordElement(510),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR512_SOLLTEMPHK2, new SignedWordElement(511),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR513_VORLAUFISTTEMPWP, new SignedWordElement(512),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR514_VORLAUFISTTEMPNHZ, new SignedWordElement(513),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR515_VORLAUFISTTEMP, new SignedWordElement(514),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR516_RUECKLAUFISTTEMP, new SignedWordElement(515),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR517_FESTWERTSOLLTEMP, new SignedWordElement(516),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR518_PUFFERISTTEMP, new SignedWordElement(517),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR519_PUFFERSOLLTEMP, new SignedWordElement(518),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR520_HEIZUNGSDRUCK, new SignedWordElement(519),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR521_VOLUMENSTROM, new SignedWordElement(520),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR522_WWISTTEMP, new SignedWordElement(521),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR523_WWSOLLTEMP, new SignedWordElement(522),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR524_GEBLAESEISTTEMP, new SignedWordElement(523),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR525_GEBLAESESOLLTEMP, new SignedWordElement(524),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR526_FLAECHEISTTEMP, new SignedWordElement(525),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR527_FLAECHESOLLTEMP, new SignedWordElement(526),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC4ReadInputRegistersTask(532, Priority.LOW,
						m(HeatpumpTecalorChannel.ChannelId.IR533_EINSATZGRENZEHZG, new SignedWordElement(532),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR534_EINSATZGRENZEWW, new SignedWordElement(533),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC4ReadInputRegistersTask(2500, Priority.HIGH,
						m(HeatpumpTecalorChannel.ChannelId.IR2501_STATUSBITS, new UnsignedWordElement(2500),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR2502_EVUFREIGABE, new UnsignedWordElement(2501),
								ElementToChannelConverter.DIRECT_1_TO_1),
						new DummyRegisterElement(2502),
						m(HeatpumpTecalorChannel.ChannelId.IR2504_ERRORSTATUS, new UnsignedWordElement(2503),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR2505_BUSSTATUS, new UnsignedWordElement(2504),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR2506_DEFROST, new UnsignedWordElement(2505),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR2507_ERRORNUMBER, new UnsignedWordElement(2506),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC4ReadInputRegistersTask(3500, Priority.LOW,
						m(HeatpumpTecalorChannel.ChannelId.IR3501_HEATPRODUCED_VDHEIZENTAG, new UnsignedWordElement(3500),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3502_HEATPRODUCED_VDHEIZENSUMKWH, new UnsignedWordElement(3501),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3503_HEATPRODUCED_VDHEIZENSUMMWH, new UnsignedWordElement(3502),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3504_HEATPRODUCED_VDWWTAG, new UnsignedWordElement(3503),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3505_HEATPRODUCED_VDWWSUMKWH, new UnsignedWordElement(3504),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3506_HEATPRODUCED_VDWWSUMMWH, new UnsignedWordElement(3505),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3507_HEATPRODUCED_NZHHEIZENSUMKWH, new UnsignedWordElement(3506),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3508_HEATPRODUCED_NZHHEIZENSUMMWH, new UnsignedWordElement(3507),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3509_HEATPRODUCED_NZHWWSUMKWH, new UnsignedWordElement(3508),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3510_HEATPRODUCED_NZHWWSUMMWH, new UnsignedWordElement(3509),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3511_CONSUMEDPOWER_VDHEIZENTAG, new UnsignedWordElement(3510),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3512_CONSUMEDPOWER_VDHEIZENSUMKWH, new UnsignedWordElement(3511),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3513_CONSUMEDPOWER_VDHEIZENSUMMWH, new UnsignedWordElement(3512),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3514_CONSUMEDPOWER_VDWWTAG, new UnsignedWordElement(3513),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3515_CONSUMEDPOWER_VDWWSUMKWH, new UnsignedWordElement(3514),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR3516_CONSUMEDPOWER_VDWWSUMMWH, new UnsignedWordElement(3515),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC4ReadInputRegistersTask(5000, Priority.HIGH,
						m(HeatpumpTecalorChannel.ChannelId.IR5001_SGREADY_OPERATINGMODE, new UnsignedWordElement(5000),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.IR5002_REGLERKENNUNG, new UnsignedWordElement(5001),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(1500, Priority.HIGH,
						m(HeatpumpTecalorChannel.ChannelId.HR1501_BERTIEBSART, new UnsignedWordElement(1500),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1502_KOMFORTTEMPHK1, new SignedWordElement(1501),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1503_ECOTEMPHK1, new SignedWordElement(1502),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1504_SLOPEHK1, new SignedWordElement(1503),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1505_KOMFORTTEMPHK2, new SignedWordElement(1504),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1506_ECOTEMPHK2, new SignedWordElement(1505),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1507_SLOPEHK2, new SignedWordElement(1506),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1508_FESTWERTBETRIEB, new SignedWordElement(1507),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1509_BIVALENZTEMPERATURHZG, new SignedWordElement(1508),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1510_KOMFORTTEMPWW, new SignedWordElement(1509),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1511_ECOTEMPWW, new SignedWordElement(1510),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1512_WARMWASSERSTUFEN, new SignedWordElement(1511),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1513_BIVALENZTEMPERATURWW, new SignedWordElement(1512),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1514_VORLAUFSOLLTEMPFLAECHENKUEHLUNG, new SignedWordElement(1513),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1515_HYSTERESEVORLAUFTEMPFLAECHENKUEHLUNG, new SignedWordElement(1514),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1516_RAUMSOLLTEMPFLAECHENKUEHLUNG, new SignedWordElement(1515),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1517_VORLAUFSOLLTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1516),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1518_HYSTERESEVORLAUFTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1517),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1519_RAUMSOLLTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1518),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1520_RESET, new SignedWordElement(1519),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1521_RESTART_ISG, new SignedWordElement(1520),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC3ReadRegistersTask(4000, Priority.HIGH,
						m(HeatpumpTecalorChannel.ChannelId.HR4001_SGREADY_ONOFF, new UnsignedWordElement(4000),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR4002_SGREADY_INPUT1, new UnsignedWordElement(4001),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR4003_SGREADY_INPUT2, new UnsignedWordElement(4002),
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
				new FC16WriteRegistersTask(1500,
						m(HeatpumpTecalorChannel.ChannelId.HR1501_BERTIEBSART, new UnsignedWordElement(1500),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1502_KOMFORTTEMPHK1, new SignedWordElement(1501),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1503_ECOTEMPHK1, new SignedWordElement(1502),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1504_SLOPEHK1, new SignedWordElement(1503),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1505_KOMFORTTEMPHK2, new SignedWordElement(1504),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1506_ECOTEMPHK2, new SignedWordElement(1505),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1507_SLOPEHK2, new SignedWordElement(1506),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1508_FESTWERTBETRIEB, new SignedWordElement(1507),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1509_BIVALENZTEMPERATURHZG, new SignedWordElement(1508),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1510_KOMFORTTEMPWW, new SignedWordElement(1509),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1511_ECOTEMPWW, new SignedWordElement(1510),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1512_WARMWASSERSTUFEN, new UnsignedWordElement(1511),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1513_BIVALENZTEMPERATURWW, new SignedWordElement(1512),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1514_VORLAUFSOLLTEMPFLAECHENKUEHLUNG, new SignedWordElement(1513),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1515_HYSTERESEVORLAUFTEMPFLAECHENKUEHLUNG, new SignedWordElement(1514),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1516_RAUMSOLLTEMPFLAECHENKUEHLUNG, new SignedWordElement(1515),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1517_VORLAUFSOLLTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1516),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1518_HYSTERESEVORLAUFTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1517),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1519_RAUMSOLLTEMPGEBLAESEKUEHLUNG, new SignedWordElement(1518),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1520_RESET, new UnsignedWordElement(1519),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR1521_RESTART_ISG, new UnsignedWordElement(1520),
								ElementToChannelConverter.DIRECT_1_TO_1)
				),
				new FC16WriteRegistersTask(4000,
						m(HeatpumpTecalorChannel.ChannelId.HR4001_SGREADY_ONOFF, new UnsignedWordElement(4000),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR4002_SGREADY_INPUT1, new UnsignedWordElement(4001),
								ElementToChannelConverter.DIRECT_1_TO_1),
						m(HeatpumpTecalorChannel.ChannelId.HR4003_SGREADY_INPUT2, new UnsignedWordElement(4002),
								ElementToChannelConverter.DIRECT_1_TO_1)
				)

		);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
				//channeltest();	// Just for testing
				channelmapping();
				break;
		}
	}

	// Put values in channels that are not directly Modbus read values but derivatives.
	protected void channelmapping() {
		int statusBits = 0;

		// Map SG generalized "isRunning"
		if (this.getStatusBits().isDefined()) {
			statusBits = this.getStatusBits().get();
			boolean aufheizprogramm = (statusBits & 4) == 4;
			boolean nhzAktiv = (statusBits & 8) == 8;
			boolean heizbetrieb = (statusBits & 16) == 16;
			boolean warmwasserbetrieb = (statusBits & 32) == 32;
			boolean verdichterAktiv = (statusBits & 64) == 64;
			boolean kuehlbetrieb = (statusBits & 256) == 256;
			boolean isRunning = aufheizprogramm || nhzAktiv || heizbetrieb || warmwasserbetrieb || verdichterAktiv || kuehlbetrieb;
			this.channel(HeatpumpSmartGridGeneralizedChannel.ChannelId.RUNNING).setNextValue(isRunning);
		}

		// Map SG generalized "isReady"
		if (this.getErrorStatus().isDefined() && this.getEvuClearance().isDefined()) {
			// Define "isReady" as no error and EVU clearance = true.
			boolean isReady = !this.getErrorStatus().get() && this.getEvuClearance().get();
			this.channel(HeatpumpSmartGridGeneralizedChannel.ChannelId.READY).setNextValue(isReady);
		}

		// Map SG generalized "noError"
		if (this.getErrorStatus().isDefined()) {
			boolean noError = !this.getErrorStatus().get();
			this.channel(HeatpumpSmartGridGeneralizedChannel.ChannelId.NO_ERROR).setNextValue(noError);
		}

		// Map SG generalized "getsetSmartGridState" read values.
		if (this.getSgReadyOperatingMode().isDefined()) {
			int generalizedSgState = this.getSgReadyOperatingMode().get() - 1;
			this.channel(HeatpumpSmartGridGeneralizedChannel.ChannelId.SMART_GRID_STATE).setNextValue(generalizedSgState);
		}

		// Map SG generalized "getsetSmartGridState" write values.
		if (this.getsetSmartGridState().getNextWriteValue().isPresent()) {
			int generalizedSgStateWrite = this.getsetSmartGridState().getNextWriteValue().get();
			boolean sgInput1;
			boolean sgInput2;
			switch (generalizedSgStateWrite) {
				case 0:
					// Off
					sgInput1 = false;
					sgInput2 = true;
					break;
				case 2:
					// Force on, increased temperature levels.
					sgInput1 = true;
					sgInput2 = false;
					break;
				case 3:
					// Force on, max temperature levels.
					sgInput1 = true;
					sgInput2 = true;
					break;
				case 1:
				default:
					// Standard
					sgInput1 = false;
					sgInput2 = false;
					break;
			}
			try {
				this.setSgReadyOnOffChannel().setNextWriteValue(true);
				this.setSgReadyInput1Channel().setNextWriteValue(sgInput1);
				this.setSgReadyInput2Channel().setNextWriteValue(sgInput2);
			} catch (OpenemsError.OpenemsNamedException e) {
				e.printStackTrace();
			}
		}

		// Map "getSetpointTempHk1" according to WPM version.
		if (getReglerkennung().isDefined()) {
			int reglerId = getReglerkennung().get();
			switch (reglerId) {
				case 390:	// WPM 3
				case 449:	// WPMsystem
					if (this.channel(HeatpumpTecalorChannel.ChannelId.IR510_SOLLTEMPHK1).value().isDefined()) {
						int setpointHk1 = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR510_SOLLTEMPHK1).value().get();
						this.getSetpointTempHk1Channel().setNextValue(setpointHk1);
					}
					break;
				case 391:	// WPM 3i
					if (this.channel(HeatpumpTecalorChannel.ChannelId.IR509_SOLLTEMPHK1).value().isDefined()) {
						int setpointHk1 = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR509_SOLLTEMPHK1).value().get();
						this.getSetpointTempHk1Channel().setNextValue(setpointHk1);
					}
					break;
			}
		}

		// Map energy channels that are transmitted as two modbus values.
		boolean channelsHaveValues1 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3502_HEATPRODUCED_VDHEIZENSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3503_HEATPRODUCED_VDHEIZENSUMMWH).value().isDefined();
		if (channelsHaveValues1) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3502_HEATPRODUCED_VDHEIZENSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3503_HEATPRODUCED_VDHEIZENSUMMWH).value().get() * 1000);
			this.getProducedHeatForHkSumChannel().setNextValue(sum);
		}
		boolean channelsHaveValues2 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3505_HEATPRODUCED_VDWWSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3506_HEATPRODUCED_VDWWSUMMWH).value().isDefined();
		if (channelsHaveValues2) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3505_HEATPRODUCED_VDWWSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3506_HEATPRODUCED_VDWWSUMMWH).value().get() * 1000);
			this.getProducedHeatForWwSumChannel().setNextValue(sum);
		}
		boolean channelsHaveValues3 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3507_HEATPRODUCED_NZHHEIZENSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3508_HEATPRODUCED_NZHHEIZENSUMMWH).value().isDefined();
		if (channelsHaveValues3) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3507_HEATPRODUCED_NZHHEIZENSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3508_HEATPRODUCED_NZHHEIZENSUMMWH).value().get() * 1000);
			this.getProducedHeatNhzForHkSumChannel().setNextValue(sum);
		}
		boolean channelsHaveValues4 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3509_HEATPRODUCED_NZHWWSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3510_HEATPRODUCED_NZHWWSUMMWH).value().isDefined();
		if (channelsHaveValues4) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3509_HEATPRODUCED_NZHWWSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3510_HEATPRODUCED_NZHWWSUMMWH).value().get() * 1000);
			this.getProducedHeatNhzForWwSumChannel().setNextValue(sum);
		}
		boolean channelsHaveValues5 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3512_CONSUMEDPOWER_VDHEIZENSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3513_CONSUMEDPOWER_VDHEIZENSUMMWH).value().isDefined();
		if (channelsHaveValues5) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3512_CONSUMEDPOWER_VDHEIZENSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3513_CONSUMEDPOWER_VDHEIZENSUMMWH).value().get() * 1000);
			this.getConsumedPowerForHkSumChannel().setNextValue(sum);
		}
		boolean channelsHaveValues6 = this.channel(HeatpumpTecalorChannel.ChannelId.IR3515_CONSUMEDPOWER_VDWWSUMKWH).value().isDefined()
				&& this.channel(HeatpumpTecalorChannel.ChannelId.IR3516_CONSUMEDPOWER_VDWWSUMMWH).value().isDefined();
		if (channelsHaveValues6) {
			int sum = (Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3515_CONSUMEDPOWER_VDWWSUMKWH).value().get()
					+ ((Integer) this.channel(HeatpumpTecalorChannel.ChannelId.IR3516_CONSUMEDPOWER_VDWWSUMMWH).value().get() * 1000);
			this.getConsumedPowerForWwSumChannel().setNextValue(sum);
		}

		if (debug) {
			this.logInfo(this.log, "--Status Bits 2501--");
			this.logInfo(this.log, "0 - HK1 Pumpe = " + (((statusBits & 1) == 1) ? 1 : 0));
			this.logInfo(this.log, "1 - HK2 Pumpe = " + (((statusBits & 2) == 2) ? 1 : 0));
			this.logInfo(this.log, "2 - Aufheizprogramm = " + (((statusBits & 4) == 4) ? 1 : 0));
			this.logInfo(this.log, "3 - NHZ Stufen in Betrieb = " + (((statusBits & 8) == 8) ? 1 : 0));
			this.logInfo(this.log, "4 - WP im Heizbetrieb = " + (((statusBits & 16) == 16) ? 1 : 0));
			this.logInfo(this.log, "5 - WP im Warmwasserbetrieb = " + (((statusBits & 32) == 32) ? 1 : 0));
			this.logInfo(this.log, "6 - Verdichter in Betrieb = " + (((statusBits & 64) == 64) ? 1 : 0));
			this.logInfo(this.log, "7 - Sommerbetrieb aktiv = " + (((statusBits & 128) == 128) ? 1 : 0));
			this.logInfo(this.log, "8 - Kühlbetrieb aktiv = " + (((statusBits & 256) == 256) ? 1 : 0));
			this.logInfo(this.log, "9 - Min. eine IWS im Abtaubetrieb = " + (((statusBits & 512) == 512) ? 1 : 0));
			this.logInfo(this.log, "10 - Silentmode1 aktiv = " + (((statusBits & 1024) == 1024) ? 1 : 0));
			this.logInfo(this.log, "11 - Silentmode2 aktiv = " + (((statusBits & 2048) == 2048) ? 1 : 0));
			this.logInfo(this.log, "");
			this.logInfo(this.log, "SmartGrid-Modus (Tecalor, 1-4): " + getSgReadyOperatingMode().get());
			this.logInfo(this.log, "Reglerkennung: " + getReglerkennung().get());
			this.logInfo(this.log, "EVU-Freigabe: " + getEvuClearance().get());
			this.logInfo(this.log, "Fehlerstatus: " + getErrorStatus().get());
			this.logInfo(this.log, "Fehlernummer: " + getErrorNumber().get());
			this.logInfo(this.log, "");
			this.logInfo(this.log, "Aussentemp: " + (getAussentemp().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Pufferspeicher Temp: " + (getBuffetTankTempActual().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Heizkreis1 Temp: " + (getIstTempHk1().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Heizkreis2 Temp: " + (getIstTempHk2().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Vorlauf Temp: " + (getForwardTempActual().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Rücklauf Temp: " + (getRewindTempActual().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "Warmwasser Temp: " + (getWarmWaterTempActual().orElse(0) / 10.0) + "°C");
			this.logInfo(this.log, "");
			this.logInfo(this.log, "--Schreibbare Parameter--");
			this.logInfo(this.log, "Betriebsart: " + setOperatingModeChannel().value().asEnum().getName());
			this.logInfo(this.log, "SmartGrid-Ready aktiviert: " + setSgReadyOnOffChannel().value().get());
			this.logInfo(this.log, "SmartGrid-Modus (OpenEMS, 0-3): " + getsetSmartGridState().value().asEnum().getName());
			this.logInfo(this.log, "");
		}

	}

	// Just for testing. Also, example code with some explanations.
	protected void channeltest() {

		// Test Modbus write. Example using Enum name field to set value. In effect, this writes an integer.
		if (testcounter == 5) {

		}

		testcounter++;

	}


}
