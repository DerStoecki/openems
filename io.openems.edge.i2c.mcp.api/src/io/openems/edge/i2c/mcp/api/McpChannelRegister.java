package io.openems.edge.i2c.mcp.api;

import io.openems.edge.i2c.mcp.api.task.McpTask;

import java.util.Map;

public interface McpChannelRegister {

    void shift();

    void addTask(String id, McpTask mcpTask);

    void removeTask(String id);

    String getParentCircuitBoard();

    void deactivate();

    void setPosition(int position, boolean activate);

    Map<Integer, Boolean> getValuesPerDefault();

    void addToDefault(int position, boolean activate);

}
