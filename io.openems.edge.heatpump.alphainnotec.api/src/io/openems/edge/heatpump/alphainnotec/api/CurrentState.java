package io.openems.edge.heatpump.alphainnotec.api;

import io.openems.common.types.OptionsEnum;

public enum CurrentState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	HEIZBETRIEB(0, "Heizbetrieb"), //
	TRINKWARMWASSER(1, "Trinkwarmwasser"), //
	SCHWIMMBAD(2, "Schwimmbad"), //
	EVU_SPERRE(3, "EVU-Sperre"), //
	ABTAUEN(4, "Abtauen"), //
	OFF(5, "Off"), //
	EXTERNE_ENERGIEQUELLE(6, "Externe Energiequelle"), //
	KUEHLUNG(7, "Kühlung"); //

	private int value;
	private String name;

	private CurrentState(int value, String name) {
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