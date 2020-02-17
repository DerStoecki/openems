package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.WriteChannel;

public class HeatPumpCommandsTask extends HeatPumpTask {

    private WriteChannel<Boolean> channel;

    public HeatPumpCommandsTask(int address, int headerNumber, WriteChannel<Boolean> channel) {
        super(address, headerNumber);
        this.channel = channel;
    }

    @Override
    public byte getRequest() {
        if (this.channel.getNextWriteValue().isPresent()) {
            //for REST
            this.channel.setNextValue(this.channel.getNextWriteValue().get());
            if (this.channel.getNextWriteValue().get()) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void setResponse(byte data) {
        //DO NOTHING
    }
}
