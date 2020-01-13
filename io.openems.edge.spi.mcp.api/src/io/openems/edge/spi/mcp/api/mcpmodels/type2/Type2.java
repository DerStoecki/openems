package io.openems.edge.spi.mcp.api.mcpmodels.type2;

import io.openems.edge.spi.mcp.api.AbstractAdc;

import java.util.List;

public abstract class Type2 extends AbstractAdc {
    public Type2(List<Long> pins, int inputType, int id, int spiChannel, String circuitBoardId) {
        super(pins, inputType, (byte) 2);
    }
}
