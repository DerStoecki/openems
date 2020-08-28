package io.openems.edge.pump.grundfos.api;

import io.openems.common.types.OptionsEnum;

public enum ControlMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	CONST_PRESS(0, "Constant pressure"), //
	PROP_PRESS(1, "Proportional pressure"), //
	CONST_FREQ(2, "Constant frequency"), //
	AUTO_ADAPT(5, "AutoAdapt"), //
	CONST_TEMP(6, "Constant temperature"), //
	CL_SENSOR_CONTR(7, "Closed loop sensor control"), //
	CONST_FLOW(8, "Constant flow"), //
	CONST_LEVEL(9, "Constant level"), //
	FLOW_ADAPT(10, "FlowAdapt"), //
	CONST_DIFF_PRESS(11, "Constant differential pressure"), //
	CONST_DIFF_TEMP(12, "Constant differential temperature"); //

	private int value;
	private String name;

	private ControlMode(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}	
}