package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.Channel;

public class HeatPumpReadTask extends HeatPumpTask {

    private Channel<Double> channel;

    public HeatPumpReadTask(int address, int headerNumber, Channel<Double> channel) {
        super(address, headerNumber);
        this.channel = channel;
    }

    @Override
    public byte getRequest() {
        return -1;
    }

    @Override
    public void setResponse(byte data) {
        switch (super.sif) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;

            case 0:
            default:
                break;


        }
    }
}
