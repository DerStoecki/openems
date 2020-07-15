package io.openems.edge.apartmentmodule.api;

import io.openems.common.types.OptionsEnum;

public enum Ready implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	READY(0, "Ready"), //
	STILL_PROCESSING(1, "Still processing last command"); //

	private int value;
	private String name;

	private Ready(int value, String name) {
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