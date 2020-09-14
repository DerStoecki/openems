package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.types.OptionsEnum;

enum CoolingMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	Off(0, "Off"), //
	AUTOMATIK(1, "Automatik"); //

	private int value;
	private String name;

	private CoolingMode(int value, String name) {
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