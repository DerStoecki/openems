package io.openems.impl.controller.symmetric.avoidtotalcharge;

/**
 * Created by maxo2 on 29.08.2017.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.api.channel.ConfigChannel;
import io.openems.api.channel.thingstate.ThingStateChannels;
import io.openems.api.controller.Controller;
import io.openems.api.doc.ChannelInfo;
import io.openems.api.doc.ThingInfo;
import io.openems.api.exception.InvalidValueException;
import io.openems.common.session.Role;

@ThingInfo(title = "Avoid total charge of battery. (Symmetric)", description = "Provides control over the battery's maximum state of charge at a specific time of day. For symmetric Ess.")
public class AvoidTotalChargeController extends Controller {

	private final Logger log = LoggerFactory.getLogger(AvoidTotalChargeController.class);

	private ThingStateChannels thingState = new ThingStateChannels(this);
	/*
	 * Config
	 */
	@ChannelInfo(title = "Ess", description = "Sets the Ess devices.", type = io.openems.impl.controller.symmetric.avoidtotalcharge.Ess.class, isArray = true)
	public final ConfigChannel<Set<io.openems.impl.controller.symmetric.avoidtotalcharge.Ess>> esss = new ConfigChannel<Set<io.openems.impl.controller.symmetric.avoidtotalcharge.Ess>>("esss", this);

	@ChannelInfo(title = "Grid Meter", description = "Sets the grid meter.", type = io.openems.impl.controller.symmetric.avoidtotalcharge.Meter.class, isOptional = false, isArray = false)
	public final ConfigChannel<io.openems.impl.controller.symmetric.avoidtotalcharge.Meter> gridMeter = new ConfigChannel<>("gridMeter", this);

	@ChannelInfo(title = "Maximum Production Power", description = "Theoretical peak value of all the photovoltaic panels", type = Long.class, isOptional = false, isArray = false, writeRoles = { Role.OWNER, Role.INSTALLER })
	public final ConfigChannel<Long> maximumProductionPower = new ConfigChannel<>("maximumProductionPower", this);

	@ChannelInfo(title = "Graph 1", description = "Sets the socMaxVals.", defaultValue = "[100,100,100,100,100,60,60,60,60,60,60,60,70,80,90,100,100,100,100,100,100,100,100,100]", type = Long[].class, isArray = true, writeRoles = { Role.OWNER, Role.INSTALLER }, isOptional = true)
	public final ConfigChannel<Long[]> graph1 = new ConfigChannel<>("graph1", this);
	//TODO: implement fixed length and min/max values (accessible by OWNER !)

	@ChannelInfo(title = "Graph 2", description = "Sets the socMaxVals.", defaultValue = "[100,100,100,100,100,60,60,60,60,60,60,60,60,60,70,80,90,100,100,100,100,100,100,100]", type = Long[].class, isArray = true, writeRoles = { Role.OWNER, Role.INSTALLER }, isOptional = true)
	public final ConfigChannel<Long[]> graph2 = new ConfigChannel<>("graph2", this);
	//TODO: implement fixed length and min/max values (accessible by OWNER !)

	@ChannelInfo(title = "Critical Percentage", description = "If the productionMeter's power raises above this percentage of its peak value, the graph-value may be neglected.", type = Long.class, writeRoles = { Role.OWNER, Role.INSTALLER }, defaultValue = "100", isArray = false)
	public final ConfigChannel<Long> criticalPercentage = new ConfigChannel<Long>("criticalPercentage", this);
	//TODO: implement min/max values (accessible by OWNER !)

	@ChannelInfo(title = "Graph 1 active", description = "Activate Graph 1 (If no graph is activated, all values are set to 100)", defaultValue = "true" ,type = Boolean.class, writeRoles = { Role.OWNER, Role.INSTALLER }, isArray = false, isOptional = true)
	public final ConfigChannel<Boolean> graph1active = new ConfigChannel<>("graph1active", this);

	@ChannelInfo(title = "Graph 2 active", description = "Activate Graph 2 (If no graph is activated, all values are set to 100)", defaultValue = "false" ,type = Boolean.class, writeRoles = { Role.OWNER, Role.INSTALLER }, isArray = false, isOptional = true)
	public final ConfigChannel<Boolean> graph2active = new ConfigChannel<>("graph2active", this);




	/*
	 * Constructors
	 */
	public AvoidTotalChargeController() {
		super();
	}

	public AvoidTotalChargeController(String thingId) {
		super(thingId);
	}

	/*
	 * Methods
	 */
	@Override
	public void run() {

		try {
			/**
			 * calculate the average available charging power
			 */
			Long avgAllowedCharge = 0L;

			for (Ess ess : esss.value()) {
				avgAllowedCharge += ess.allowedCharge.value();
			}
			avgAllowedCharge = avgAllowedCharge / esss.value().size();

			/**
			 * generate ChargingGraph and get maxWantedSoc value
			 */
			int graphMode = 0;
			Optional<Boolean> g1aOptional = graph1active.valueOptional();
			Optional<Boolean> g2aOptional = graph2active.valueOptional();

			if (g1aOptional.isPresent() && g1aOptional.get()){
				graphMode = 1;
			} else if (g2aOptional.isPresent() && g2aOptional.get()){
				graphMode = 2;
			}

			Map<Integer, Double> m = new HashMap<Integer, Double>(0);
			for (int i = 0; i < 24; i++) {
				if (graphMode == 1){
					m.put(i, (double) graph1.value()[i] / 100.0);
				}else if (graphMode == 2){
					m.put(i, (double) graph2.value()[i] / 100.0);
				}else {
					m.put(i, 1.0);
				}
			}
			ManualGraph mg = new ManualGraph(m);
			Long maxWantedSoc = (long) (100 * mg.getCurrentVal());

			/**
			 * get the power feeded to the grid relatively to the producer's peak value
			 */
			Long maxAbsoluteProducablePower = maximumProductionPower.value();
			Long relativeFeededPower = 0L;

			relativeFeededPower = -100 * gridMeter.value().activePower.value() / maxAbsoluteProducablePower;

			for (Ess ess : esss.value()) {


				/**
				 * check if state of charge is above the value specified by the user and deny charging in
				 * case. However, in case the critical percentage was reached by the
				 * relativeFeededPower and the spareProducedPower is negative (-> grid is taking the
				 * maxFeedablePower) allow charging nevertheless.
				 */
				if (ess.soc.value() >= maxWantedSoc) {
					if(relativeFeededPower >= criticalPercentage.value()) {
						long spareProducedPower = (long) (((relativeFeededPower - criticalPercentage.value()) / 100.0) * (-1 * maxAbsoluteProducablePower));

						if (spareProducedPower < 0L){
							try {
								Long totalActivePower = (long) (((double) ess.allowedCharge.value() / (double) avgAllowedCharge) * ((double) spareProducedPower / (double) esss.value().size()));
								ess.activePowerLimit.setP(totalActivePower);
								ess.power.applyLimitation(ess.activePowerLimit);
							} catch (Exception e) {
								log.error(e.getMessage(),e);
							}
						} else {
							try {
								ess.minActivePowerLimit.setP(0L);
								ess.power.applyLimitation(ess.minActivePowerLimit);
							} catch (Exception e) {
								log.error(e.getMessage(),e);
							}
						}
					} else {
						try {
							ess.minActivePowerLimit.setP(0L);
							ess.power.applyLimitation(ess.minActivePowerLimit);
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
				}
			}

		} catch (InvalidValueException | IndexOutOfBoundsException e){
			log.error(e.getMessage(),e);
		}
	}

	@Override
	public ThingStateChannels getStateChannel() {
		return this.thingState;
	}
}