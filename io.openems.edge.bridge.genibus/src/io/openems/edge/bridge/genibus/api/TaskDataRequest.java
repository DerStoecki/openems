package io.openems.edge.bridge.genibus.api;

import java.util.List;

import io.openems.edge.bridge.genibus.protocol.ApplicationProgramDataUnit;
import io.openems.edge.bridge.genibus.protocol.ProtocolDataUnit;
import io.openems.edge.bridge.genibus.protocol.Telegram;

public class TaskDataRequest implements GenibusTask {
    protected Device device;
    protected Telegram requestTelegram;
    protected Handler handler;

    public TaskDataRequest(Handler handler, Device device) {
        this.handler = handler;
        this.requestTelegram = new Telegram();

        requestTelegram.setStartDelimiterDataRequest(); //SET SD = 0x27
        requestTelegram.setDestinationAddress(device.getAddress()); //id
        requestTelegram.setSourceAddress(0x01);

        // Set first apdu

        ApplicationProgramDataUnit apduProtocolData = new ApplicationProgramDataUnit();
        apduProtocolData.setHeadClassMeasuredData();
        apduProtocolData.setOSInfo();
        apduProtocolData.putDataField(0x02); // i_rst = ID 2
        apduProtocolData.putDataField(0x10); // t_mo = ID 16
        apduProtocolData.putDataField(0x1A); // p_hi = ID 26
        requestTelegram.getProtocolDataUnit().putAPDU(apduProtocolData);

        ApplicationProgramDataUnit apduConfigParams = new ApplicationProgramDataUnit();
        apduConfigParams.setHeadClassConfigurationParameters();
        apduConfigParams.setOSGet();
        apduConfigParams.putDataField(0x04); // i_rst = ID 2
        apduConfigParams.putDataField(0x05); // t_mo = ID 16
        requestTelegram.getProtocolDataUnit().putAPDU(apduConfigParams);

        ApplicationProgramDataUnit apduCommands = new ApplicationProgramDataUnit();
        apduCommands.setHeadClassCommands();
        apduCommands.setOSSet();
        apduCommands.putDataField(0x07); //Activate remote control
        apduCommands.putDataField(0x06); //Start pump
        apduCommands.putDataField(0x22); //CONST_FREQ
        requestTelegram.getProtocolDataUnit().putAPDU(apduCommands);

    }

    public boolean getRequest() {

        // Send serial request with given telegram
        return handler.writeTelegram(200, this);
    }

    public void setResponse(Telegram telegram) {
        ProtocolDataUnit protocolDataUnit = new ProtocolDataUnit();

        List<ApplicationProgramDataUnit> applicationProgramDataUnitListResponse = protocolDataUnit.getApplicationProgramDataUnitList();
        List<ApplicationProgramDataUnit> applicationProgramDataUnitListRequest = requestTelegram.getProtocolDataUnit().getApplicationProgramDataUnitList();


        for(ApplicationProgramDataUnit applicationProgramDataUnit: applicationProgramDataUnitListRequest) {
            //TODO: get response type etc...
            if(applicationProgramDataUnit.getHeadClass() == 0) {

            }
            byte[] dataFields = applicationProgramDataUnit.getDataFields();
            //TODO: handle data response
        }
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
