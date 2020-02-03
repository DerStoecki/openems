package io.openems.edge.bridge.genibus.api;

import io.openems.edge.bridge.genibus.protocol.Telegram;

public interface GenibusTask {
     Telegram getRequestTelegram();
     Handler getHandler();

     boolean getRequest();
     void setResponse(Telegram telegram);
}
