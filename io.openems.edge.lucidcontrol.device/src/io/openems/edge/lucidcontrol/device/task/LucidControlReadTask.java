package io.openems.edge.lucidcontrol.device.task;

import io.openems.edge.bridge.lucidcontrol.task.AbstractLucidControlBridgeTask;
import io.openems.edge.bridge.lucidcontrol.task.LucidControlBridgeTask;
import io.openems.edge.common.channel.Channel;

public class LucidControlReadTask extends AbstractLucidControlBridgeTask implements LucidControlBridgeTask {

    private Channel<Double> barChannel;
    private String path;
    private String voltage;
    private int pinPos;
    private double lastBarValue = 0;
    //will be changed, just a placeholder atm
    private double maxBar = 10;
    private int maxVoltage;

    public LucidControlReadTask(String moduleId, String deviceId, String path, String voltage, int pinPos, Channel<Double> barChannel) {
        super(moduleId, deviceId);

        this.barChannel = barChannel;
        this.path = path;
        this.voltage = voltage;
        this.pinPos = pinPos;
        allocateMaxVoltage();
    }

    private void allocateMaxVoltage() {

        switch (voltage) {
            case "5V":
            case "+-5V":
                maxVoltage = 5;
                break;
            case "10V":
            case "+-10V":
                maxVoltage = 10;
                break;
            default:
                maxVoltage = 24;
        }
    }

    /**
     * Gets the CommandLine result of the Bridge and calculates pressure.
     *
     * @param voltageRead result of the Bridge command line.
     */
    @Override
    public void setResponse(double voltageRead) {

        this.barChannel.setNextValue((voltageRead * maxBar) / maxVoltage);

    }

    /**
     * path of the LucidControModule.
     *
     * @return the path.
     */
    @Override
    public String getPath() {
        return this.path;
    }


    /**
     * Pin Position of the Device.
     *
     * @return pinPosition.
     */
    @Override
    public int getPinPos() {
        return this.pinPos;
    }
}
