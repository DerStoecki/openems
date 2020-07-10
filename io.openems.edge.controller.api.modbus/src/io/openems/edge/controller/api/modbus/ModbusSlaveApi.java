package io.openems.edge.controller.api.modbus;


import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusRecord;
import io.openems.edge.common.modbusslave.ModbusSlave;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;

import java.util.Map;
import java.util.TreeMap;

@ProviderType
public interface ModbusSlaveApi extends OpenemsComponent {

	void logDebug(Logger log, String message);

	void logInfo(Logger log, String message);

	void logWarn(Logger log, String message);

	TreeMap<Integer, ModbusRecord> getRecords();

	Map<String, ModbusSlave> getComponents();

}
