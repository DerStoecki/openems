package io.openems.edge.pump.grundfos.task;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.pump.grundfos.api.WarnBits;

import java.util.List;

public class PumpWarnBitsTask extends AbstractPumpTask {


    private Channel<String> channel;
    private WarnBits warnBits;

    public PumpWarnBitsTask(int address, int headerNumber, Channel<String> channel, String type) {
        super(address, headerNumber, "");
        this.channel = channel;
        switch (type) {
            case "Magna3":
            default:
                this.warnBits = WarnBits.MAGNA_3;
        }

    }

    @Override
    public int getRequest() {
        return -1;
    }

    @Override
    public void setResponse(byte data) {

        // When vi == 0 (false), then 0xFF means "data not available". Then there is no error and no error bit whose
        // string needs to be looked up. -> Exit method.
        if (super.vi == false) {
            if ((data & 0xFF) == 0xFF) {
                return;
            }
        }

        List<String> errorValue;
        StringBuilder allErrors = new StringBuilder();
        switch (this.channel.channelId().id()) {
            case "WarnBits1":
                errorValue = this.warnBits.getErrorBits1();
                break;
            case "WarnBits2":
                errorValue = this.warnBits.getErrorBits2();
                break;
            case "WarnBits3":
                errorValue = this.warnBits.getErrorBits3();
                break;
            case "WarnBits4":
            default:
                errorValue = this.warnBits.getErrorBits4();
                break;
        }

        for (int x = 0; x < 8; x++) {
            if ((data & (1 << x)) == (1 << x)) {
                System.out.println("Bit: " + x + ", message: " + errorValue.get(x));
                allErrors.append(errorValue.get(x));
            }
        }
        this.channel.setNextValue(allErrors);
    }
}
