package io.openems.edge.relays.module.api;


import io.openems.edge.i2c.mcp.api.Mcp;


public interface RelaisBoard {

    String getId();

    Mcp getMcp();
}
