package io.openems.edge.relays.device.task;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.i2c.mcp.api.task.McpTask;


public class RelaysActuatorTask extends McpTask {
    private int position;
    private WriteChannel<Boolean> writeOnOrOff;


    public RelaysActuatorTask(int position, WriteChannel<Boolean> writeOnOrOff, String relaysBoard) {
        super(relaysBoard);
        this.position = position;
        this.writeOnOrOff = writeOnOrOff;
    }

    @Override
    public WriteChannel<Boolean> getWriteChannel() {
        return this.writeOnOrOff;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    //No Usage here, just for the ChpModule
    @Override
    public WriteChannel<Integer> getPowerLevel() {
        return null;
    }

    //Same here
    @Override
    public int getDigitValue() {
        return -666;
    }
}
