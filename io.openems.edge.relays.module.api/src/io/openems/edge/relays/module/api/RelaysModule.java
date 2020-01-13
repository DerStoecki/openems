package io.openems.edge.relays.module.api;


import io.openems.edge.i2c.mcp.api.Mcp;


public interface RelaysModule {

    String getId();

    Mcp getMcp();
}
