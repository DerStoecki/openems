package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.types.OptionsEnum;

enum VentilationMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	AUTOMATIK(0, "Automatik"), //
	PARTY(1, "Party"), //
	FERIEN(2, "Ferien"), //
	OFF(3, "Off"); //

	private int value;
	private String name;

	private VentilationMode(int value, String name) {
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