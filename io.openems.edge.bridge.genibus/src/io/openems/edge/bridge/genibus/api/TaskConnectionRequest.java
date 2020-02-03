package io.openems.edge.bridge.genibus.api;

import java.util.Map;

import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;

public class TaskConnectionRequest implements GenibusTask {
    protected Map<Byte, Device> deviceList;
    protected Telegram requestTelegram;
    protected Handler handler;

    public TaskConnectionRequest(Handler handler, Map<Byte, Device> deviceList) {
        this.deviceList = deviceList;
        this.handler = handler;
        this.requestTelegram = new Telegram();

        requestTelegram.setStartDelimiterDataRequest(); //SET SD = 0x27
        requestTelegram.setDestinationAddress(0xFE); //Broadcast
        requestTelegram.setSourceAddress(0x01);

        // Set first apdu

        ApplicationProgramDataUnit apduProtocolData = new ApplicationProgramDataUnit();
        apduProtocolData.setHeadClass(0x00);
        apduProtocolData.setOSGet();
        apduProtocolData.putDataField(0x02); // df_buf_len = ID 2
        apduProtocolData.putDataField(0x03); // unit_bus_mode = ID 3
        requestTelegram.getProtocolDataUnit().putAPDU(apduProtocolData);

        // Set second apdu

        ApplicationProgramDataUnit apduConfigParams = new ApplicationProgramDataUnit();
        apduConfigParams.setHeadClassConfigurationParameters();
        apduConfigParams.setOSGet();
        apduConfigParams.putDataField(0x2E); // unit_addr = ID 46
        apduConfigParams.putDataField(0x2F); // group_addr = ID 47
        requestTelegram.getProtocolDataUnit().putAPDU(apduConfigParams);

        // Set third apdu

        ApplicationProgramDataUnit apduMeasuredData = new ApplicationProgramDataUnit();
        apduMeasuredData.setHeadClassMeasuredData();
        apduMeasuredData.setOSGet();
        apduMeasuredData.putDataField(0x94); // unit_familiy = ID 148
        apduMeasuredData.putDataField(0x95); // unit_type = ID 149
        requestTelegram.getProtocolDataUnit().putAPDU(apduMeasuredData);

    }

    public boolean getRequest() {
        // Send serial request with given telegram
        return handler.writeTelegram(1000, this);
    }

    public void setResponse(Telegram telegram) {
        System.out.println("handle response for device address: " + telegram.getSourceAddress());
    }

    @Override
    public Telegram getRequestTelegram() {
        return requestTelegram;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }
}
