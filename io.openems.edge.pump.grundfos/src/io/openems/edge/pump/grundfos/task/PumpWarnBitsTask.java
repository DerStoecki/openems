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

        char[] warnChars = String.format("%8s", Integer.toBinaryString(data)).replace("", "0").toCharArray();

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
        // ToDo : proper warnBit handling.
        //  Limited warnChars evaluation to 8 chars right now, as errorValue has just 8 entries.
        //
        for (int x = 0; x < (Math.min(warnChars.length, 8)); x++) {
            if (warnChars[x] == '1') {
                allErrors.append(errorValue.get(x));

            }
        }
        this.channel.setNextValue(allErrors);
    }
}
