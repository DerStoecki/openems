package io.openems.edge.heatpump.task;

import io.openems.edge.bridge.genibus.api.GenibusTask;
import io.openems.edge.bridge.genibus.api.Handler;
import io.openems.edge.bridge.genibus.protocol.Telegram;

public class HeatPumpTask implements GenibusTask {



    @Override
    public Telegram getRequestTelegram() {
        return null;
    }

    @Override
    public Handler getHandler() {
        return null;
    }

    @Override
    public boolean getRequest() {
        return false;
    }

    @Override
    public void setResponse(Telegram telegram) {

    }

}
