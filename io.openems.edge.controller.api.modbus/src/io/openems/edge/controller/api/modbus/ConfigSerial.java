package io.openems.edge.controller.api.modbus;

import io.openems.common.channel.AccessMode;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Controller Api Modbus-Slave/Serial", //
		description = "This controller provides a Modbus-Slave/Serial api.")
@interface ConfigSerial {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlApiModbusSerial0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Port-Name", description = "The name of the serial port - e.g. '/dev/ttyUSB0' or 'COM3'")
	String portName() default "/dev/ttyUSB0";

	@AttributeDefinition(name = "Baudrate", description = "The baudrate - e.g. 9600, 19200, 38400, 57600 or 115200")
	int baudRate() default 9600;

	@AttributeDefinition(name = "Flow control in", description = "Flow control in")
	FlowControlIn flowControlIn() default FlowControlIn.DISABLED;

	@AttributeDefinition(name = "Flow control out", description = "Flow control out")
	FlowControlOut flowControlOut() default FlowControlOut.DISABLED;

	@AttributeDefinition(name = "Databits", description = "The number of databits - e.g. 8")
	int databits() default 8;

	@AttributeDefinition(name = "Stopbits", description = "The number of stopbits - '1', '1.5' or '2'")
	Stopbit stopbits() default Stopbit.ONE;

	@AttributeDefinition(name = "Parity", description = "The parity - 'none', 'even', 'odd', 'mark' or 'space'")
	Parity parity() default Parity.NONE;

	@AttributeDefinition(name = "Echo", description = "Echo")
	boolean echo() default false;

	@AttributeDefinition(name = "Access-Mode", description = "Only allow access to Read-Only/Read-Write/Write-Only channels.")
	AccessMode accessMode() default AccessMode.READ_WRITE;

	@AttributeDefinition(name = "Component-IDs", description = "Components that should be made available via Modbus.")
	String[] component_ids() default { "_sum" };

	@AttributeDefinition(name = "Api-Timeout", description = "Sets the timeout in seconds for updates on Channels set by this Api.")
	int apiTimeout() default 60;

	@AttributeDefinition(name = "Components target filter", description = "This is auto-generated by 'Component-IDs'.")
	String Component_target() default "";

	String webconsole_configurationFactory_nameHint() default "Controller Api Modbus-Slave/Serial [{id}]";
}