package io.openems.edge.ess.mr.gridcon;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.ess.mr.gridcon.enums.GridConChannelId;

public class DcdcControl {

	private float dcVoltageSetpoint = 0f;
	private final float weightStringA = 0f; // is set in applyPower()
	private final float weightStringB = 0f; // is set in applyPower()
	private final float weightStringC = 0f; // is set in applyPower()
	private float iRefStringA = 0f;
	private float iRefStringB = 0f;
	private float iRefStringC = 0f;
	private float stringControlMode = 0f;

	public DcdcControl dcVoltageSetpoint(float value) {
		this.dcVoltageSetpoint = value;
		return this;
	}

	public DcdcControl iRefStringA(float value) {
		this.iRefStringA = value;
		return this;
	}

	public DcdcControl iRefStringB(float value) {
		this.iRefStringB = value;
		return this;
	}

	public DcdcControl iRefStringC(float value) {
		this.iRefStringC = value;
		return this;
	}

	public DcdcControl stringControlMode(ComponentManager componentManager, Config config)
			throws OpenemsNamedException {
		int weightingMode = 0;

		// Depends on number of battery strings!!!
		// battA = 1 (2^0)
		// battB = 8 (2^3)
		// battC = 64 (2^6)

		if (config.batteryStringA_id() != null && config.batteryStringA_id().length() > 0) {
			Battery batteryStringA = componentManager.getComponent(config.batteryStringA_id());
			if (batteryStringA != null) {
				weightingMode = weightingMode + 1;
			}
		}
		if (config.batteryStringB_id() != null && config.batteryStringB_id().length() > 0) {
			Battery batteryStringB = componentManager.getComponent(config.batteryStringB_id());
			if (batteryStringB != null) {
				weightingMode = weightingMode + 8;
			}
		}
		if (config.batteryStringC_id() != null && config.batteryStringC_id().length() > 0) {
			Battery batteryStringC = componentManager.getComponent(config.batteryStringC_id());
			if (batteryStringC != null) {
				weightingMode = weightingMode + 64;
			}
		}

		this.stringControlMode = weightingMode;
		return this;
	}

	public void writeToChannels(GridconPCS parent) throws IllegalArgumentException, OpenemsNamedException {
		// weighting is never allowed to be '0'
		if (this.stringControlMode == 0) {
			throw new OpenemsException("Calculated weight of '0' -> not allowed!");
		}

		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_DC_VOLTAGE_SETPOINT, this.dcVoltageSetpoint);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_WEIGHT_STRING_A, this.weightStringA);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_WEIGHT_STRING_B, this.weightStringB);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_WEIGHT_STRING_C, this.weightStringC);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_I_REF_STRING_A, this.iRefStringA);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_I_REF_STRING_B, this.iRefStringB);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_I_REF_STRING_C, this.iRefStringC);
		this.writeValueToChannel(parent, GridConChannelId.DCDC_CONTROL_STRING_CONTROL_MODE, this.stringControlMode);
	}

	private <T> void writeValueToChannel(GridconPCS parent, GridConChannelId channelId, T value)
			throws IllegalArgumentException, OpenemsNamedException {
		((WriteChannel<?>) parent.channel(channelId)).setNextWriteValueFromObject(value);
	}
}
