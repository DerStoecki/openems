package io.openems.edge.i2c.mcp.api.task;

import io.openems.edge.common.channel.WriteChannel;

public abstract class RelaysTask extends McpTask {

    public RelaysTask(String moduleId) {
        super(moduleId);
    }
    public abstract WriteChannel<Boolean> getWriteChannel();
}
