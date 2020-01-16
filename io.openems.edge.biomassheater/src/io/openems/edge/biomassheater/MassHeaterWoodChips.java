package io.openems.edge.biomassheater;

import io.openems.edge.biomassheater.api.BioMassHeater;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.task.FC1ReadCoilsTask;
import io.openems.edge.bridge.modbus.api.task.FC2ReadInputsTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


public class MassHeaterWoodChips extends AbstractOpenemsModbusComponent implements OpenemsComponent, BioMassHeater {
    @Reference
    protected ConfigurationAdmin cm;


    public MassHeaterWoodChips() {
        super(OpenemsComponent.ChannelId.values(),
                BioMassHeater.ChannelId.values());
    }

    @Activate
    public void activate(ComponentContext context, Config config) {
        super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), cm, "Modbus", config.modbusBridgeId());

    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
                new FC1ReadCoilsTask(10000, Priority.HIGH,
                        m(BioMassHeater.ChannelId.DISTURBANCE, 10000, ))
    }
}
