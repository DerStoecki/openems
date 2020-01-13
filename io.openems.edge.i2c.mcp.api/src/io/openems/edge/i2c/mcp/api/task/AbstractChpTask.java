package io.openems.edge.i2c.mcp.api.task;

import io.openems.edge.common.channel.WriteChannel;

public abstract class AbstractChpTask extends McpTask {
    public AbstractChpTask(String moduleId) {
        super(moduleId);
    }

    public abstract WriteChannel<Integer> getPowerLevel();

    public abstract int getDigitValue();
}
