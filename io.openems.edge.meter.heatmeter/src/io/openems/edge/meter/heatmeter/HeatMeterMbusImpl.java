package io.openems.edge.meter.heatmeter;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.heatmeter.task.HeatMeterTask;
import io.openems.edge.bridge.mbus.api.BridgeMbus;
import io.openems.edge.bridge.mbus.api.ChannelRecord;
import io.openems.edge.common.channel.Doc;

import io.openems.edge.bridge.mbus.api.AbstractOpenemsMbusComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.heatmeter.api.HeatMeterMbus;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventConstants;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "HeatMeterMbus",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class HeatMeterMbusImpl extends AbstractOpenemsMbusComponent implements OpenemsComponent, HeatMeterMbus {

    @Reference
    protected ConfigurationAdmin cm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected BridgeMbus mbus;

    HeatMeterType heatMeterType;

    public HeatMeterMbusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                HeatMeterMbus.ChannelId.values(),
                ChannelId.values());
    }

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        MANUFACTURER_ID(Doc.of(OpenemsType.STRING) //
                .unit(Unit.NONE)), //
        DEVICE_ID(Doc.of(OpenemsType.STRING) //
                .unit(Unit.NONE)), //
        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        allocateAddressViaMeterType(config.meterType());
        super.activate(context, config.id(), config.alias(), config.enabled(),
                config.primaryAddress(), cm, "mbus", config.mbusBridgeId());

        this.mbus.addTask(config.id(), new HeatMeterTask(this.mbus, this, this.getAverageHourConsumption(), this.getTotalConsumedEnergy()));

    }

    private void allocateAddressViaMeterType(String meterType) {
        switch (meterType) {
            case "Itron-CF51":
                this.heatMeterType = HeatMeterType.ITRON_CF_51;
                break;

            case "Sharky-775":
                this.heatMeterType = HeatMeterType.SHARKY_775;

                break;

            case "Zelsius CF-CMF":
                this.heatMeterType = HeatMeterType.ZELSIUS_C5_CMF;
                break;


        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    protected void addChannelDataRecords() {
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(HeatMeterMbus.ChannelId.TOTAL_CONSUMED_ENERGY), this.heatMeterType.totalConsumptionEnergyAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(HeatMeterMbus.ChannelId.FLOW_TEMP), this.heatMeterType.flowTempAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(HeatMeterMbus.ChannelId.RETURN_TEMP), this.heatMeterType.returnTempAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(HeatMeterMbus.ChannelId.POWER), this.heatMeterType.powerAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(HeatMeterMbus.ChannelId.PERCOLATION), this.heatMeterType.percolationAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.MANUFACTURER_ID), ChannelRecord.DataType.Manufacturer));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.DEVICE_ID), ChannelRecord.DataType.DeviceId));
    }

    @Override
    public String debugLog() {
        return super.id() + " total consumed energy: " + this.getTotalConsumedEnergy().value() + " average consumption: " + this.getAverageHourConsumption().value();
    }

}
