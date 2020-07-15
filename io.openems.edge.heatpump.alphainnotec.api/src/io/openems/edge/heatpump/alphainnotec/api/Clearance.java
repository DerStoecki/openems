package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.types.OptionsEnum;

public enum Clearance implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	SPERRE(0, "Sperre"), //
	FREIGABE_1_VERDICHTER(1, "Freigabe 1 Verdichter"), //
	FREIGABE_2_VERDICHTER(2, "Freigabe 2 Verdichter"); //

	private int value;
	private String name;

	private Clearance(int value, String name) {
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