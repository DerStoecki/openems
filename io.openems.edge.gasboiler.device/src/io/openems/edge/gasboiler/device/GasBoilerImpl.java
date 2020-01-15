package io.openems.edge.gasboiler.device;

import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC2ReadInputsTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.gasboiler.device.api.GasBoiler;
import io.openems.edge.gasboiler.device.api.GasBoilerData;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Designate;


public class GasBoilerImpl extends AbstractOpenemsModbusComponent implements OpenemsComponent, GasBoilerData, GasBoiler {

    @Reference
    protected ConfigurationAdmin cm;

    public GasBoilerImpl() {
        super(OpenemsComponent.ChannelId.values(),
                GasBoilerData.ChannelId.values());
    }


    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbusBridgeId());
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC2ReadInputsTask(1, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_MODULATION_VALUE, new UnsignedWordElement(1),
                                ElementToChannelConverter.DIRECT_1_TO_1),
                        new DummyRegisterElement(2,2),
                        m(GasBoilerData.ChannelId.WARM_WATER_EFFECTIVE_SET_POINT_TEMPERATURE, new UnsignedWordElement(3)),
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(4))
                        ),
                new FC2ReadInputsTask(8, Priority.LOW,
                        m(GasBoilerData.ChannelId.HEAT_BOILER_TEMPERATURE_ACTUAL, new UnsignedWordElement(8))
                        ),
                new FC2ReadInputsTask(11, Priority.LOW,
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_TEMPERATURE_EFFECTIVE, new UnsignedWordElement(11)),
                        m(GasBoilerData.ChannelId.BOILER_SET_POINT_PERFORMANCE_EFFECTIVE, new UnsignedWordElement(12))),
                new FC2ReadInputsTask(17, Priority.LOW,
                        m(GasBoilerData.ChannelId.WARM_WATER_TEMPERATURE_SET_POINT_EFFECTIVE, new UnsignedWordElement(17)))

                );
    }
}
