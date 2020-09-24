package io.openems.edge.controller.pump.grundfos;

public enum ControlMode {
	CONST_PRESSURE(1), //
	CONST_FREQUENCY(2);

	ControlMode(int value) {
		this.value = value;
	}

	private int value;

	public int getValue() {
		return this.value;
	}
}
