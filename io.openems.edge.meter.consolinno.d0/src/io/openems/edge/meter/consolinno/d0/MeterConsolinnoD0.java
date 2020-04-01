package io.openems.edge.meter.consolinno.d0;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.HashMap;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.openems.edge.bridge.lmnwired.api.AbstractOpenEmsLMNWiredComponent;
import io.openems.edge.bridge.lmnwired.api.BridgeLMNWired;
import io.openems.edge.bridge.lmnwired.api.task.LMNWiredTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.AsymmetricMeter;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.api.SymmetricMeter;

import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.ConfigurationPolicy;

@Designate(ocd = Config.class, factory = true)
@Component(name = "io.openems.edge.meter.consolinno.d0", //
        immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE)

public class MeterConsolinnoD0 extends AbstractOpenEmsLMNWiredComponent
        implements SymmetricMeter, AsymmetricMeter, OpenemsComponent {

    @Reference
    protected ConfigurationAdmin cm;

    private MeterType meterType = MeterType.GRID;

    private final Logger log = LoggerFactory.getLogger(MeterConsolinnoD0.class);

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected BridgeLMNWired bridgeLMNWired;

    private String taskIDObisDataRequest;

    private HashMap<String, SymmetricMeter.ChannelId> obisChannelMapping = new HashMap<>();


    Config config;

    public MeterConsolinnoD0() {
        super(OpenemsComponent.ChannelId.values(), //
                SymmetricMeter.ChannelId.values(),//
                AsymmetricMeter.ChannelId.values());

        obisChannelMapping.put("1-0:1.8.0*255", SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TOTAL);
        obisChannelMapping.put("1-0:1.8.1*255", SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_ONE);
        obisChannelMapping.put("1-0:1.8.2*255", SymmetricMeter.ChannelId.POSITIVE_ACTIVE_ENERGY_TARIF_TWO);
        obisChannelMapping.put("1-0:2.8.0*255", SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TOTAL);
        obisChannelMapping.put("1-0:2.8.1*255", SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_ONE);
        obisChannelMapping.put("1-0:2.8.2*255", SymmetricMeter.ChannelId.NEGATIVE_ACTIVE_ENERGY_TARIF_TWO);
        obisChannelMapping.put("1-0:16.7.0*255", SymmetricMeter.ChannelId.ELECTRICITY_EFFECTIVE_VALUE);
        obisChannelMapping.put("1-0:32.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_ONE);
        obisChannelMapping.put("1-0:52.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_TWO);
        obisChannelMapping.put("1-0:72.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_VOLTAGE_PHASE_THREE);
        obisChannelMapping.put("1-0:31.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_ONE);
        obisChannelMapping.put("1-0:51.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_TWO);
        obisChannelMapping.put("1-0:71.7.0*255", SymmetricMeter.ChannelId.INSTANTANEOUS_AMPERAGE_PHASE_THREE);
        obisChannelMapping.put("1-0:81.7.1*255", SymmetricMeter.ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_ONE);
        obisChannelMapping.put("1-0:81.7.2*255", SymmetricMeter.ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWO);
        obisChannelMapping.put("1-0:81.7.4*255", SymmetricMeter.ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FOUR);
        obisChannelMapping.put("1-0:81.7.15*255", SymmetricMeter.ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_FIFTEEN);
        obisChannelMapping.put("1-0:81.7.26*255", SymmetricMeter.ChannelId.PHASE_ANGLE_MOMENTARY_VALUE_TWENTY_SIX);
        obisChannelMapping.put("1-0:14.7.0*255", SymmetricMeter.ChannelId.FREQUENCY_MOMENTARY_VALUE_TOTAL);
        obisChannelMapping.put("1-0:1.8.0*96", SymmetricMeter.ChannelId.ENERGY_USAGE_LAST_DAY);
        obisChannelMapping.put("1-0:1.8.0*97", SymmetricMeter.ChannelId.ENERGY_USAGE_LAST_WEEK);
        obisChannelMapping.put("1-0:1.8.0*98", SymmetricMeter.ChannelId.ENERGY_USAGE_LAST_MONTH);
        obisChannelMapping.put("1-0:1.8.0*99", SymmetricMeter.ChannelId.ENERGY_USAGE_LAST_YEAR);
        obisChannelMapping.put("1-0:1.8.0*100", SymmetricMeter.ChannelId.ENERGY_USAGE_SINCE_LAST_RESET);
        obisChannelMapping.put("1-0:0.2.0*255", SymmetricMeter.ChannelId.GENERAL_USAGE);
        obisChannelMapping.put("1-0:96.1.0*255", SymmetricMeter.ChannelId.PRODUCTION_NUMBER);
        obisChannelMapping.put("1-0:96.90.2*255", SymmetricMeter.ChannelId.CHECKSUM);
        obisChannelMapping.put("1-0:97.97.0*255", SymmetricMeter.ChannelId.INTERNAL_ERROR);
    }

    @Activate
    void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled(), this.cm, config.service_pid());

        this.config = config;
        this.meterType = config.type();
        // update filter for 'lmnwired'
        if (OpenemsComponent.updateReferenceFilter(cm, config.service_pid(), "lmnwired", config.lmnwired_id())) {
            return;
        }

        log.info("Add Tasks for Meter: " + config.serialNumber());

        // Add one read task per obis
        taskIDObisDataRequest = config.id() + "_readobis";

        this.bridgeLMNWired.addTask(
                taskIDObisDataRequest,
                new LMNWiredTask(this, bridgeLMNWired, config.serialNumber(), config.use_obis_list(), obisChannelMapping)
        );

    }

    @Deactivate
    protected void deactivate() {
        super.deactivate();
        this.bridgeLMNWired.removeTask(taskIDObisDataRequest);
    }

    @Override
    public MeterType getMeterType() {
        return meterType;
    }

}
