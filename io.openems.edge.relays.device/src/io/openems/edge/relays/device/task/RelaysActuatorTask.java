package io.openems.edge.relays.device.task;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.i2c.mcp.api.task.McpTask;
import io.openems.edge.i2c.mcp.api.task.RelaysTask;


public class RelaysActuatorTask extends RelaysTask {
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

}
