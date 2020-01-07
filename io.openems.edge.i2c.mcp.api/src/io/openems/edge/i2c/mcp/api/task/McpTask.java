package io.openems.edge.i2c.mcp.api.task;

import io.openems.edge.common.channel.WriteChannel;

public abstract class McpTask {

    private String relaysModule;

    public McpTask(String relaysModule) {
        this.relaysModule = relaysModule;
    }

    public abstract int getPosition();

    public abstract WriteChannel<Boolean> getWriteChannel();

    public abstract WriteChannel<Integer> getPowerLevel();

    public abstract int getDigitValue();


}