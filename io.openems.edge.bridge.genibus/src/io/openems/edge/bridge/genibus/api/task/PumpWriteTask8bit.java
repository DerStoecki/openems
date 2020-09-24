package io.openems.edge.bridge.genibus.api.task;

import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.taskmanager.Priority;

public class PumpWriteTask8bit extends PumpWriteTask16bitOrMore {

    /*
    private WriteChannel<Double> channel;
    private boolean sendGet = false;
    private int readData;
    */


    public PumpWriteTask8bit(int address, int headerNumber, WriteChannel<Double> channel, String unitString, Priority priority, double channelMultiplier) {
        super(1, address, headerNumber, channel, unitString, priority, channelMultiplier);
    }

    public PumpWriteTask8bit(int address, int headerNumber, WriteChannel<Double> channel, String unitString, Priority priority) {
        this(address, headerNumber, channel, unitString, priority, 1);
    }

    /*
    @Override
    public int getRequest(int byteCounter) {
        int request = -256;
        if (super.InformationDataAvailable() && this.channel.getNextWriteValue().isPresent()) {
            //NOTE! The Correct Calculated Digit will be calc by a Controller!
            double dataOfChannel = this.channel.getNextWriteValue().get();
            return (byte) dataOfChannel;
            }
        return request;
    }

    @Override
    public void setSendGet(boolean value) {
        sendGet = value;
    }

    @Override
    public boolean getSendGet() {
        return sendGet;
    }
    */

}
