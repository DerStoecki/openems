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

    private int powerAddress;
    private int percolationAddress;
    private int totalConsumedEnergyAddress;
    private int flowTempAddress;
    private int returnTempAddress;

    public HeatMeterMbusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                HeatMeterMbus.ChannelId.values(),
                ChannelId.values());
    }

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        TOTAL_CONSUMED_ENERGY(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.KILOWATT_HOURS)), //
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

        super.activate(context, config.id(), config.alias(), config.enabled(),
                config.primaryAddress(), cm, "mbus", config.mbusBridgeId());

        this.mbus.addTask(config.id(), new HeatMeterTask(this.mbus, this, this.getAverageHourConsumption(), this.getTotalConsumedEnergy()));
        allocateAddressViaMeterType(config.meterType());
    }

    private void allocateAddressViaMeterType(String meterType) {
        switch (meterType) {
            case "Itron-CF51":
                this.powerAddress = HeatMeterType.ITRON_CF_51.getPowerAddress();
                this.percolationAddress = HeatMeterType.ITRON_CF_51.getPercolationAddress();
                this.totalConsumedEnergyAddress = HeatMeterType.ITRON_CF_51.getTotalConsumptionEnergyAddress();
                this.flowTempAddress = HeatMeterType.ITRON_CF_51.getFlowTempAddress();
                this.returnTempAddress = HeatMeterType.ITRON_CF_51.getReturnTempAddress();
                break;

            case "Sharky-775":
                this.powerAddress = HeatMeterType.SHARKY_775.getPowerAddress();
                this.percolationAddress = HeatMeterType.SHARKY_775.getPercolationAddress();
                this.totalConsumedEnergyAddress = HeatMeterType.SHARKY_775.getTotalConsumptionEnergyAddress();
                this.flowTempAddress = HeatMeterType.SHARKY_775.getFlowTempAddress();
                this.returnTempAddress = HeatMeterType.SHARKY_775.getReturnTempAddress();

                break;

            case "Zelsius CF-CMF":
                this.powerAddress = HeatMeterType.ZELSIUS_C5_CMF.getPowerAddress();
                this.percolationAddress = HeatMeterType.ZELSIUS_C5_CMF.getPercolationAddress();
                this.totalConsumedEnergyAddress = HeatMeterType.ZELSIUS_C5_CMF.getTotalConsumptionEnergyAddress();
                this.flowTempAddress = HeatMeterType.ZELSIUS_C5_CMF.getFlowTempAddress();
                this.returnTempAddress = HeatMeterType.ZELSIUS_C5_CMF.getReturnTempAddress();
                break;


        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    protected void addChannelDataRecords() {
        this.channelDataRecordsList.add(new ChannelRecord(channel(HeatMeterMbus.ChannelId.TOTAL_CONSUMED_ENERGY), this.totalConsumedEnergyAddress));
        this.channelDataRecordsList.add(new ChannelRecord(channel(HeatMeterMbus.ChannelId.FLOW_TEMP), this.flowTempAddress));
        this.channelDataRecordsList.add(new ChannelRecord(channel(HeatMeterMbus.ChannelId.RETURN_TEMP), this.returnTempAddress));
        this.channelDataRecordsList.add(new ChannelRecord(channel(HeatMeterMbus.ChannelId.POWER), this.powerAddress));
        this.channelDataRecordsList.add(new ChannelRecord(channel(HeatMeterMbus.ChannelId.PERCOLATION), this.percolationAddress));
        this.channelDataRecordsList.add(new ChannelRecord(channel(ChannelId.MANUFACTURER_ID), ChannelRecord.DataType.Manufacturer));
        this.channelDataRecordsList.add(new ChannelRecord(channel(ChannelId.DEVICE_ID), ChannelRecord.DataType.DeviceId));
    }

}
