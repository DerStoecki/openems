package io.openems.edge.i2c.mcp.api.task;

import io.openems.edge.common.channel.WriteChannel;

public abstract class RelaysTask extends McpTask {

    private boolean isInverse;

    public RelaysTask(String moduleId, boolean isInverse) {
        super(moduleId);
        this.isInverse = isInverse;
    }
    public abstract WriteChannel<Boolean> getWriteChannel();
    public boolean isInverse(){
        return this.isInverse;
    }
}
