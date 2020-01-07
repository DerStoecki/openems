package io.openems.edge.spi.mcp.api.mcpmodels.type4;

import io.openems.edge.spi.mcp.api.AbstractAdc;

import java.util.List;

public abstract class Type4 extends AbstractAdc {

    public Type4(List<Long> pins, int inputType, int id, int spiChannel, String circuitBoardId) {
        super(pins, inputType, (byte) 4);
    }
}
