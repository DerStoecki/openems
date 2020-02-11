package io.openems.edge.heatpump.device.task;

import io.openems.edge.common.channel.WriteChannel;

public class HeatPumpWriteTask extends HeatPumpTask {

    private WriteChannel<Double> channel;


    public HeatPumpWriteTask(int address, int headerNumber, WriteChannel<Double> channel) {
        super(address, headerNumber);
        this.channel = channel;
    }

    @Override
    public byte getRequest() {
        byte request = -1;
        if (super.informationAvailable && this.channel.getNextWriteValue().isPresent()) {
            double dataOfChannel = this.channel.getNextWriteValue().get();
            //for REST
            this.channel.setNextValue(dataOfChannel);

            switch (super.sif) {

                //TODO Calc data
                case 1:
                    break;

                case 2:
                    break;
                case 3:
                    break;
                case 0:
                default:
            }
        }
        return request;
    }

    @Override
    public void setResponse(byte data) {
        //do nothing
    }
}
