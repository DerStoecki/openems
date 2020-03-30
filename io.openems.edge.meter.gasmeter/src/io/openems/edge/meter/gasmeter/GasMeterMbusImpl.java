package io.openems.edge.meter.gasmeter;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.mbus.api.BridgeMbus;
import io.openems.edge.bridge.mbus.api.ChannelRecord;
import io.openems.edge.bridge.mbus.api.task.MbusTask;
import io.openems.edge.common.channel.Doc;

import io.openems.edge.bridge.mbus.api.AbstractOpenemsMbusComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.gasmeter.api.GasMeter;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = Config.class, factory = true)
@Component(name = "GasMeterMbus",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        immediate = true)
public class GasMeterMbusImpl extends AbstractOpenemsMbusComponent implements OpenemsComponent, GasMeter {

    @Reference
    protected ConfigurationAdmin cm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected BridgeMbus mbus;


    private GasMeterType gasMeterType;

    public GasMeterMbusImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GasMeter.ChannelId.values(),
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

        this.mbus.addTask(config.id(), new MbusTask(this.mbus, this));
        allocateAddressViaMeterType(config.meterType());
    }

    private void allocateAddressViaMeterType(String meterType) {
        switch (meterType) {
            case "Placeholder":
                this.gasMeterType = GasMeterType.PLACEHOLDER;
                break;

        }
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


    @Override
    protected void addChannelDataRecords() {
        this.channelDataRecordsList.add(new ChannelRecord(channel(GasMeter.ChannelId.TOTAL_CONSUMED_ENERGY), this.gasMeterType.getTotalConsumptionEnergyAddress()));
        this.channelDataRecordsList.add(new ChannelRecord(channel(GasMeter.ChannelId.FLOW_TEMP), this.gasMeterType.getFlowTempAddress()));
        this.channelDataRecordsList.add(new ChannelRecord(channel(GasMeter.ChannelId.RETURN_TEMP), this.gasMeterType.getReturnTempAddress()));
        this.channelDataRecordsList.add(new ChannelRecord(channel(GasMeter.ChannelId.POWER), this.gasMeterType.getPowerAddress()));
        this.channelDataRecordsList.add(new ChannelRecord(channel(GasMeter.ChannelId.PERCOLATION), this.gasMeterType.getPercolationAddress()));
        this.channelDataRecordsList.add(new ChannelRecord(channel(ChannelId.MANUFACTURER_ID), ChannelRecord.DataType.Manufacturer));
        this.channelDataRecordsList.add(new ChannelRecord(channel(ChannelId.DEVICE_ID), ChannelRecord.DataType.DeviceId));
    }
}