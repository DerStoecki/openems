package io.openems.edge.relaisboardmcp;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Mcp23008 extends Mcp implements McpChannelRegister {

	private final String address;
	private String parentCircuitBoard;
	private final int length = 8;
	private final I2CDevice device;
	Map<Integer, Boolean> valuesPerDefault = new ConcurrentHashMap<>();
	private final boolean[] shifters;

	public Mcp23008(String address, I2CBus device, String parentCircuitBoard) throws IOException {
		this.address = address;
		this.parentCircuitBoard = parentCircuitBoard;
		this.shifters = new boolean[length];

		for (int i = 0; i < length; i++) {
			this.shifters[i] = false;
		}
		switch (address) {
			case "0x22":
				this.device = device.getDevice(0x22);
				break;
			case "0x24":
				this.device = device.getDevice(0x24);
				break;
			case "0x26":
				this.device = device.getDevice(0x26);
				break;
			default:
				this.device = device.getDevice(0x20);
				break;
		}

		this.device.write(0x00, (byte) 0x00);

	}


	public void setPosition(int position, boolean activate) {
		if (position < this.length) {
			this.shifters[position] = activate;
		} else {
			throw new IllegalArgumentException("There is no such position." + position + " maximum is " + this.length);
		}
	}

	public void shift() {
		byte data = 0x00;
		for (int i = length - 1; i >= 0; i--) {
			data = (byte) (data << 1);
			if (this.shifters[i]) {
				data += 1;
			}
		}
		try {
			device.write(0x09, (byte) data);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addToDefault(int position, boolean activate) {
		this.valuesPerDefault.put(position, activate);
	}

	public Map<Integer, Boolean> getValuesPerDefault() {
		return valuesPerDefault;
	}

	public String getParentCircuitBoard() {
		return parentCircuitBoard;
	}
}
