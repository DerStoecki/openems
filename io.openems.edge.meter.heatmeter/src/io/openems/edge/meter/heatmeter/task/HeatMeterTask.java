package io.openems.edge.meter.heatmeter.task;

import io.openems.edge.bridge.mbus.api.AbstractOpenemsMbusComponent;
import io.openems.edge.bridge.mbus.api.BridgeMbus;
import io.openems.edge.bridge.mbus.api.ChannelDataRecordMapper;
import io.openems.edge.bridge.mbus.api.task.MbusTask;
import io.openems.edge.common.channel.Channel;
import org.openmuc.jmbus.VariableDataStructure;

public class HeatMeterTask extends MbusTask {
    //1 min timestamp
    private int waitTime = 1 * 60 * 1000;
    private long lastTimeStamp = 0;
    private int[] consumptionData = new int[60];
    private int counterFilling = 0;

    private int maxValueOfData;
    private int minValueOfData;

    private Channel<Integer> averageHourConsumption;
    private Channel<Integer> totalConsumption;

    private static int KILOWATT_CONVERTER = 1000;

    public HeatMeterTask(BridgeMbus bridgeMbus, AbstractOpenemsMbusComponent openemsMbusComponent, Channel<Integer> averageHourConsumption, Channel<Integer> consumption) {
        super(bridgeMbus, openemsMbusComponent);
        this.averageHourConsumption = averageHourConsumption;
        this.totalConsumption = consumption;
    }

    @Override
    public void setResponse(VariableDataStructure data) {
        new ChannelDataRecordMapper(data, getOpenemsMbusComponent().getChannelDataRecordsList());
        //check if 1 min is up
        if (System.currentTimeMillis() - lastTimeStamp >= waitTime || lastTimeStamp == 0) {

            if (totalConsumption.getNextValue().isDefined()) {
                //Position of new Data
                this.consumptionData[counterFilling % consumptionData.length] = totalConsumption.getNextValue().get();
                //max Value is always the new data
                this.maxValueOfData = this.consumptionData[counterFilling % consumptionData.length];
                //min Value is either 0 or the Value next to the new Data
                if (counterFilling < consumptionData.length) {
                    minValueOfData = consumptionData[0];
                } else {
                    //check that minValue is not out of bounce;
                    // occurs when counterFilling%consumptionData.length = consumptionData.length -1

                    int minValuePosition;

                    if ((counterFilling % consumptionData.length) == (consumptionData.length - 1)) {
                        minValuePosition = 0;
                    } else {
                        minValuePosition = (counterFilling % consumptionData.length) + 1;
                    }
                    minValueOfData = consumptionData[minValuePosition];
                }

                //Average Hour Consumption is either (max-min) / available values XOR (max-min)/60 <-- hours
                if (this.counterFilling <= consumptionData.length) {
                    //from watthours to kw
                    if (counterFilling != 0) {
                        averageHourConsumption.setNextValue((maxValueOfData - minValueOfData) / (counterFilling * KILOWATT_CONVERTER));
                    }
                } else {
                    averageHourConsumption.setNextValue((maxValueOfData - minValueOfData) / (consumptionData.length * KILOWATT_CONVERTER));
                }
                counterFilling++;
                lastTimeStamp = System.currentTimeMillis();

                //if counterFilling was at max once --> e.g. 60 --> always stay in between 60-119 --> for correct filling
                if (counterFilling >= consumptionData.length) {
                    counterFilling = (counterFilling % consumptionData.length) + consumptionData.length;
                }
            }
        }
    }
}
