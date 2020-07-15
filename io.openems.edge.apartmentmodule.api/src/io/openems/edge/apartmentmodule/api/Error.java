package io.openems.edge.apartmentmodule.api;

import io.openems.common.types.OptionsEnum;

public enum Error implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	NO_ERROR(0, "No Error"), //
	ERROR(1, "Error"); //

	private int value;
	private String name;

	private Error(int value, String name) {
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