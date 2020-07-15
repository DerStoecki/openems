package io.openems.edge.apartmentmodule.api;

import io.openems.common.types.OptionsEnum;

public enum ExternalRequest implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	NO_REQUEST(0, "No request has occurred"), //
	EXTERNAL_REQUEST(1, "External request has occurred"); //

	private int value;
	private String name;

	private ExternalRequest(int value, String name) {
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