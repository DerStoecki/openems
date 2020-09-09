package io.openems.edge.meter.watermeter;

import io.openems.edge.common.channel.Channel;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.mbus.api.AbstractOpenemsMbusComponent;
import io.openems.edge.bridge.mbus.api.BridgeMbus;
import io.openems.edge.bridge.mbus.api.ChannelRecord;
import io.openems.edge.bridge.mbus.api.ChannelRecord.DataType;
import io.openems.edge.bridge.mbus.api.task.MbusTask;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
//import io.openems.edge.meter.api.MeterType;

@Designate(ocd = Config.class, factory = true)
@Component(name = "WaterMeterMbus", //
        immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE)

public class WaterMeterMbusImpl extends AbstractOpenemsMbusComponent
        implements OpenemsComponent {

//    private MeterType meterType = MeterType.CONSUMPTION_METERED;
    int volAddress;
    int timeStampAddress;

    @Reference
    protected ConfigurationAdmin cm;

    @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
    protected BridgeMbus mbus;

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        MANUFACTURER_ID(Doc.of(OpenemsType.STRING) //
                .unit(Unit.NONE)), //
        DEVICE_ID(Doc.of(OpenemsType.STRING) //
                .unit(Unit.NONE)), //
        TOTAL_CONSUMED_WATER(Doc.of(OpenemsType.DOUBLE) //
                .unit(Unit.CUBIC_METER)), //
        // TIMESTAMP is in seconds, however with a granularity of 60 seconds
        TIMESTAMP(Doc.of(OpenemsType.INTEGER) //
                .unit(Unit.SECONDS)), //
        ;

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return this.doc;
        }
    }

    public WaterMeterMbusImpl() {
        super(OpenemsComponent.ChannelId.values(), //
                ChannelId.values());
    }

    @Activate
    void activate(ComponentContext context, Config config) {
//        this.meterType = config.type();
        if ( config.model().equals("none") ) {
            this.volAddress=config.volAddress();
            this.timeStampAddress=config.timeStampAddress();
        } else {
            //Select meter model from enum list. string in config.model has to be in WaterMeterModel list.
            this.volAddress=WaterMeterModel.valueOf(config.model()).getVolAddress();
            this.timeStampAddress=WaterMeterModel.valueOf(config.model()).getTimeStampAddress();
        }
        super.activate(context, config.id(), config.alias(), config.enabled(), config.primaryAddress(), this.cm, "mbus",
                config.mbus_id());
        // register into mbus bridge task list
        this.mbus.addTask(config.id(), new MbusTask(this.mbus, this));
    }

    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected void addChannelDataRecords() {
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.MANUFACTURER_ID), DataType.Manufacturer));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.DEVICE_ID), DataType.DeviceId));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.TOTAL_CONSUMED_WATER), this.volAddress));
        this.channelDataRecordsList.add(new ChannelRecord(this.channel(ChannelId.TIMESTAMP), this.timeStampAddress));
    }

    public Channel<Integer> getTotalConsumedWater() {
        return this.channel(ChannelId.TOTAL_CONSUMED_WATER);
    }


}
