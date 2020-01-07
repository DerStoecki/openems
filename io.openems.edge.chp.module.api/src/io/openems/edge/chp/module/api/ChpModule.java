package io.openems.edge.chp.module.api;

import io.openems.edge.relais.module.api.Mcp;

public interface ChpModule {

    String getId();

    Mcp getMcp();


}
