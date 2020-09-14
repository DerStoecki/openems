package io.openems.edge.controller.signalhotwater;

import io.openems.common.exceptions.OpenemsError;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.controller.signalhotwater.api.SignalHotWaterChannel;
import io.openems.edge.thermometer.api.Thermometer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


/**
 * This is the Consolinno SignalHotWater Controller. It monitors two temperature sensors on a water tank and listens
 * to a remote signal about the heat network status. The water tank upper sensor monitors the minimum temperature, the
 * water tank lower sensor monitors the maximum temperature. When the temperature falls below the minimum, the
 * controller sets the heatTankRequest channel to "true", which signals to a remote station to activate the heat network.
 * The controller then waits for a response from the remote station that the heat network is ready ("true" is written in
 * the heatNetworkReadySignal channel by the remote station), or proceeds after a timeout. Then the needHotWater channel
 * is set to "true", which signals to the next controller to start transferring heat from the heat network to the water
 * tank.
 * While the water tank temperature is within bounds, changes (!) in the heatNetworkReadySignal channel are
 * passed on to the needHotWater channel. That means, if the heatNetworkReadySignal channel changes from "true" to
 * "false" while the water tank is heating up and is already above min temperature, needHotWater will change to "false"
 * and the heating will stop. If heatNetworkReadySignal changes from "false" to "true" while the tank is below max
 * temperature, needHotWater will change to "true" and heating will start.
 *
 */

// An Paul:
// Anmerkung zum Controller: Wenn das Nahwärme Netzwerk nicht anspringt (warum auch immer) wird bei aktueller Logik
// versucht den Wassertank zu heizen bis er voll ist. Solange ist die Heizung abgeklemmt. Reicht die Wärme im Netzwerk
// nicht um den Wassertank voll zu bekommen, bleibt der Controller im "Wassertank Heizen" Status hängen und die Heizung
// bleibt abgeklemmt. (Keine Ahnung ob das tatsächlich eintreten kann.)
// Also Wasser lauwarm und Heizung ganz aus wenn die Nahwärme nicht reicht.
// Wäre Heizung lauwarm und Wasser kalt da nicht besser? Im Winter vermutlich ja, im Sommer nein.
// Eine Lösung wäre ein Timer, der das befüllen vom Wassertank irgendwann beendet, wenn das Wärmenetz nicht an geht.
// Kann auch mit der Außentemperatur gekoppelt werden, um dieses Errorhandling auf den Winter zu begrenzen.
//
// Andere Lösung: Wenn das remote Signal heatNetworkReady kurz an und wieder aus geht würde das Tankbefüllen auch
// abgebrochen. Sofern der Wassertank über minimum Temperatur ist.


@Designate(ocd = Config.class, factory = true)
@Component(name = "SignalHotWater", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SignalHotWaterImpl extends AbstractOpenemsComponent implements OpenemsComponent, SignalHotWaterChannel, Controller {

	private final Logger log = LoggerFactory.getLogger(SignalHotWaterImpl.class);

	@Reference
	protected ComponentManager cpm;

	private Thermometer watertankTempSensorUpperChannel;
	private Thermometer watertankTempSensorLowerChannel;
	private int minTempUpper;
	private int maxTempLower;
	private int responseTimeout;
	private LocalDateTime timestamp;
	private boolean heatNetworkStateTracker;

	// Variables for channel readout
	private boolean tempSensorsSendData;
	private boolean heatNetworkSignalReveived;
	private boolean heatNetworkReady;
	private int watertankTempUpperSensor;
	private int watertankTempLowerSensor;

	public SignalHotWaterImpl() {
		super(OpenemsComponent.ChannelId.values(),
				SignalHotWaterChannel.ChannelId.values(),
				Controller.ChannelId.values());
	}

	@Activate
	public void activate(ComponentContext context, Config config) throws OpenemsError.OpenemsNamedException, ConfigurationException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		minTempUpper = config.min_temp_upper() * 10;	// Convert to dezidegree.
		maxTempLower = config.max_temp_lower() * 10;
		responseTimeout = config.response_timeout();
		this.heatTankRequest().setNextValue(false);
		this.needHotWater().setNextValue(false);
		heatNetworkStateTracker = false;
		this.noError().setNextValue(true);

		// Allocate components.
		try {
			if (cpm.getComponent(config.temperatureSensorUpperId()) instanceof Thermometer) {
				this.watertankTempSensorUpperChannel = cpm.getComponent(config.temperatureSensorUpperId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorUpperId(), "configured component is incorrect!");
			}
			if (cpm.getComponent(config.temperatureSensorLowerId()) instanceof Thermometer) {
				this.watertankTempSensorLowerChannel = cpm.getComponent(config.temperatureSensorLowerId());
			} else {
				throw new ConfigurationException("The configured component is not a temperature sensor! Please check "
						+ config.temperatureSensorLowerId(), "configured component is incorrect!");
			}
		} catch (OpenemsError.OpenemsNamedException | ConfigurationException e) {
			e.printStackTrace();
		}

	}


	@Deactivate
	public void deactivate() {super.deactivate();}

	@Override
	public void run() throws OpenemsError.OpenemsNamedException {

		// Sensor checks and error handling.
		if (watertankTempSensorUpperChannel.getTemperature().value().isDefined()) {
			if (watertankTempSensorLowerChannel.getTemperature().value().isDefined()) {
				tempSensorsSendData = true;
				watertankTempLowerSensor = watertankTempSensorLowerChannel.getTemperature().value().get();
				watertankTempUpperSensor = watertankTempSensorUpperChannel.getTemperature().value().get();
				if (this.noError().value().get() == false) {
					this.noError().setNextValue(true);
					this.logError(this.log, "Temperature sensors are fine now!");
				}
			} else {
				tempSensorsSendData = false;
				this.noError().setNextValue(false);
				this.logError(this.log, "Not getting any data from the water tank lower temperature sensor " + watertankTempSensorLowerChannel.id() + ".");
			}
		} else {
			tempSensorsSendData = false;
			this.noError().setNextValue(false);
			this.logError(this.log, "Not getting any data from the water tank upper temperature sensor " + watertankTempSensorUpperChannel.id() + ".");
		}

		// Transfer channel data to local variables for better readability of logic code.
		heatNetworkSignalReveived = this.heatNetworkReadySignal().value().isDefined();
		heatNetworkReady = this.heatNetworkReadySignal().value().get();


		// Control logic, version 1.
		if (tempSensorsSendData) {

			// Remote changes from false to true.
			if (heatNetworkSignalReveived && heatNetworkReady && (heatNetworkStateTracker == false)) {
				this.heatTankRequest().setNextValue(true);
				heatNetworkStateTracker = true;

				// Set timer, just in case "heatNetworkReady == false" next cycle.
				timestamp = LocalDateTime.now();
			}

			// Remote changes from true to false.
			if (heatNetworkSignalReveived && (heatNetworkReady == false) && heatNetworkStateTracker) {
				this.heatTankRequest().setNextValue(false);
				heatNetworkStateTracker = false;
			}

			// Check lower temperature limit. Use heatTankRequest().getNextValue() here, to override remote if needed.
			if (this.heatTankRequest().getNextValue().get() == false) {
				if (watertankTempUpperSensor < minTempUpper) {
					timestamp = LocalDateTime.now();
					this.heatTankRequest().setNextValue(true);
				}
			}

			// Stop when hot enough.
			if (watertankTempLowerSensor > maxTempLower) {
				this.heatTankRequest().setNextValue(false);
				this.needHotWater().setNextValue(false);
			} else {

				// Heating has been requested.
				if (this.heatTankRequest().value().get()) {

					// Wait for remote signal or execute after timeout.
					if ((heatNetworkSignalReveived && heatNetworkReady)
							|| ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now()) >= responseTimeout) {

						// Check if we got here by timeout.
						if ((heatNetworkSignalReveived && heatNetworkReady) == false){
							if (this.needHotWater().value().get() == false) {
								this.logWarn(this.log, "Warning: remote response timeout.");
							}
						}

						this.needHotWater().setNextValue(true);
					}
				} else {
					this.needHotWater().setNextValue(false);
				}
			}

			// Pass on temperature of lower sensor. Saves needing to configure the temperature sensor in other controller.
			this.waterTankTemp().setNextValue(watertankTempLowerSensor);
		}


		// An Paul:
		// Weil bei control logic version 1 die Abfrage "this.heatTankRequest().getNextValue().get()" drin ist, habe ich
		// versucht zu überlegen ob ich das umbauen kann um statt "getNextValue()" die Abfrage "value()" zu verwenden.
		// Herausgekommen ist control logic version 2. Ich kann jetzt nicht beurteilen ob die besser oder schlechter ist,
		// vor allem was verständlichkeit angeht. Musst Du sagen.
		// Es gibt tatsächlich Unterschiede im Verhalten, aber nur im unwahrscheinlichen Grenzfall. Ist in Zeile 283
		// beschrieben.


/*
		// Control logic, version 2.
		if (tempSensorsSendData) {

			// Check if water tank is above max temperature.
			if (watertankTempLowerSensor > maxTempLower) {
				// Stop heating
				this.heatTankRequest().setNextValue(false);
				this.needHotWater().setNextValue(false);
			} else {

				// When water tank temperature is below max, check remote for "on" signal.
				// heatNetworkReady changes from false to true -> start heating.
				if (heatNetworkSignalReveived && heatNetworkReady && (heatNetworkStateTracker == false)) {
					this.heatTankRequest().setNextValue(true);
					heatNetworkStateTracker = true;

					// Set timer, just in case "heatNetworkReady == false" next cycle.
					timestamp = LocalDateTime.now();
				}

				// Check if heating has been requested.
				if (this.heatTankRequest().value().get()) {

					// Wait for "heatNetworkReady == true" or execute after timeout.
					if ((heatNetworkSignalReveived && heatNetworkReady)
							|| ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now()) >= responseTimeout) {

						// Check if we got here by timeout.
						if ((heatNetworkSignalReveived && heatNetworkReady) == false){
							if (this.needHotWater().value().get() == false) {
								this.logWarn(this.log, "Warning: heat network response timeout.");
							}
						}

						this.needHotWater().setNextValue(true);
					}
				}
			}

			// Anmerkung an PauL:
			// Soll die "too cold" Abfrage zur besseren Lesbarkeit vor der "too hot" Abfrage stehen muss etwas Code
			// geändert werden. Genauer: this.needHotWater().setNextValue(false) in Zeile 272 muss verschoben werden
			// in ein "else" in Zeile 251.


			// Check if water tank is too cold.
			if (watertankTempUpperSensor < minTempUpper) {
				// Request heat and start timer.
				if (this.heatTankRequest().value().get() == false) {
					timestamp = LocalDateTime.now();
					this.heatTankRequest().setNextValue(true);
				}
			} else {
				// If water tank temperature is not too low, check remote for "off" signal.
				// heatNetworkReady changes from true to false -> turn off heating.
				if (heatNetworkSignalReveived && (heatNetworkReady == false) && heatNetworkStateTracker) {
					this.heatTankRequest().setNextValue(false);
					this.needHotWater().setNextValue(false);
					heatNetworkStateTracker = false;
				}
			}

			// Pass on temperature of lower sensor. Saves needing to configure the temperature sensor in other controller.
			this.waterTankTemp().setNextValue(watertankTempLowerSensor);
		}
*/


		// An Paul:
		// Unterschied Control logic ver. 1 zu ver. 2: Grenzfall niedrige Wassertank Temperatur
		// (Szenario is unwahrscheinlich, aber es existiert)
		// Fall Wassertank zu kalt, Wärmenetz geht an, Tank wird beheizt. Jetzt geht das Netz wieder aus bevor der Tank
		// voll ist.
		// Ist der Tank über minimum Temperatur, wird bei beiden Logik Versionen das heizen abgebrochen.
		// Ist der Tank UNTER minimum Temperatur, gibt es einen Unterschied zwischen ver. 1 und ver. 2.
		// - Ver. 1: Das Netz-aus hat keine Auswirkungen, es wird erst aufgehört zu heizen wenn der Tank voll ist. Oder
		// 			 wenn das Netz nochmal an und dann wieder aus geht, während der Tank über minimum Temp ist.
		// - Ver. 2: Sobald minimum Temp erreicht ist wird Netz-aus umgesetzt und mit dem Heizen aufgehört.
		//
		// Nächster Unterschied: Grenzfall hohe Wassertank Temperatur
		// (Dieses Szenario ist vermutlich ebenfalls selten)
		// Fall Wassertank wurde befüllt und ist über maximum Temperatur. Das Wärmenetz bleibt aber an. Der Wassertank
		// kühlt ab und ist wieder unter Maximaltemperatur.
		// Bleibt das Wärmenetz durchgehend an, passiert nichts. Keine Version fängt zum heizen an.
		// Für Ver. 2 gibt es aber den Spezialfall: Geht das Wärmenetz aus und dann wieder an, während der Tank über
		// maximum Temperatur ist, fängt Ver. 2 zum Heizen an sobald der Tank die Maximaltemperatur unterschreitet.
		//
		// Der Unterschied kommt daher, ob remote überprüft wird mit oder ohne Temperaturabfrage vorher. Man kann sich
		// aussuchen welches Verhalten in welchem Grenzfall man haben will. Also z.B. bei Grenzfall tiefe Temp Verhalten
		// Ver. 2 und bei Grenzfall hohe Temp Verhalten Ver. 1 ist möglich.





	}

}