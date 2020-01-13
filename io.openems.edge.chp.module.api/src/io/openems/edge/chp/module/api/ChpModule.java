package io.openems.edge.chp.module.api;


import io.openems.edge.i2c.mcp.api.Mcp;

public interface ChpModule {

    String getId();

    Mcp getMcp();


}
