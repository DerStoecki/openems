package io.openems.edge.heatpump.task;

import io.openems.common.exceptions.OpenemsError;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.genibus.api.GenibusTask;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;

public class HeatPumpTask implements GenibusTask {

    private int address;
    private Channel<?> channel;
    private boolean isWriteable;
    private int headerNumber;

    public HeatPumpTask(int address, int headerNumber,WriteChannel<?> channel, boolean isWriteable) {
        this.address = address;
        this.isWriteable = isWriteable;
        this.channel = channel;
        this.headerNumber = headerNumber;
    }

    @Override
    public void setResponse(double data) {
        //        if (isWriteable) {
        //            try {
        //            } catch (OpenemsError.OpenemsNamedException e) {
        //                e.printStackTrace();
        //            }
        //        }
        this.channel.setNextValue(data);

    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public boolean isWriteable() {
        return isWriteable;
    }

    @Override
    public int getHeader() {
        return headerNumber;
    }


}
