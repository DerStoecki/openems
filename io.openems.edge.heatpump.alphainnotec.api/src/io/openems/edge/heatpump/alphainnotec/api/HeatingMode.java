package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.types.OptionsEnum;

enum HeatingMode implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	AUTOMATIK(0, "Automatik"), //
	ZUSAETZLICHER_WAERMEERZEUGER(1, "Zusätzlicher Wärmeerzeuger"), //
	PARTY(2, "Party"), //
	FERIEN(3, "Ferien"), //
	OFF(4, "Off"); //

	private int value;
	private String name;

	private HeatingMode(int value, String name) {
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