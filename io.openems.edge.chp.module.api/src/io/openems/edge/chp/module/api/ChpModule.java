package io.openems.edge.chp.module.api;

import io.openems.edge.relais.board.api.Mcp;

public interface ChpModule {

    String getId();

    Mcp getMcp();


}
