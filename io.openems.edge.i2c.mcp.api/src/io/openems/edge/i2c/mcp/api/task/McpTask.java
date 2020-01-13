package io.openems.edge.i2c.mcp.api.task;

import io.openems.edge.common.channel.WriteChannel;

public abstract class McpTask {

    private String moduleId;

    McpTask(String moduleId) {
        this.moduleId = moduleId;
    }

    public abstract int getPosition();

    //could be used at some point e.g. identification of the Mcp etc, not needed yet.

    public String getModuleId() {
        return moduleId;
    }
}