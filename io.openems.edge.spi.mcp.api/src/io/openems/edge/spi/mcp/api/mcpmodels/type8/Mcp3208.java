package io.openems.edge.spi.mcp.api.mcpmodels.type8;

import io.openems.edge.spi.mcp.api.pins.PinList;


public class Mcp3208 extends Type8 {
    public Mcp3208() {
        super(PinList.Mcp_3208.getPinList(), PinList.Mcp_3208.getInputType());
    }

}
