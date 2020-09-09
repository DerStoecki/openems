package io.openems.edge.bridge.mbus.api;

import java.util.Date;
import java.util.List;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DlmsUnit;
import org.openmuc.jmbus.VariableDataStructure;

import io.openems.edge.bridge.mbus.api.ChannelRecord.DataType;
import io.openems.edge.common.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.openems.common.channel.Unit.*;

public class ChannelDataRecordMapper {
	protected VariableDataStructure data;

	protected List<ChannelRecord> channelDataRecordsList;
	public ChannelDataRecordMapper(VariableDataStructure data, List<ChannelRecord> channelDataRecordsList) {
		this.data = data;
		this.channelDataRecordsList = channelDataRecordsList;

		for (ChannelRecord channelRecord : channelDataRecordsList) {
			mapDataToChannel(data, channelRecord.getdataRecordPosition(), channelRecord.getChannel(),
					channelRecord.getDataType());
		}
	}

	public VariableDataStructure getData() {
		return data;
	}

	public void setData(VariableDataStructure data) {
		this.data = data;
	}

	public List<ChannelRecord> getChannelDataRecordsList() {
		return channelDataRecordsList;
	}

	public void setChannelDataRecordsList(List<ChannelRecord> channelDataRecordsList) {
		this.channelDataRecordsList = channelDataRecordsList;
	}

	protected void mapDataToChannel(VariableDataStructure data, int index, Channel<?> channel, DataType dataType) {

		if (dataType == null) {
			if (data.getDataRecords().size() > index && index >= 0) {
			//	channel.setNextValue(data.getDataRecords().get(index).getScaledDataValue());
				mapScaledCheckedDataToChannel(data.getDataRecords().get(index),channel);
			}
			return;
		}
		switch (dataType) {
		case Manufacturer:
			channel.setNextValue(data.getSecondaryAddress().getManufacturerId());
			break;
		case DeviceId:
			channel.setNextValue(data.getSecondaryAddress().getDeviceId());
			break;
		case MeterType:
			channel.setNextValue(data.getSecondaryAddress().getDeviceType());
			break;
		}
	}

	protected void mapScaledCheckedDataToChannel(DataRecord record, Channel<?> channel) {
		Unit openemsUnit=channel.channelDoc().getUnit();

		// give epoch of timestamp in seconds. However, accuracy is only in minutes.
		if (record.getDataValueType() == DataRecord.DataValueType.DATE) {
			int divisor=-1;
			switch(openemsUnit) {
				case MILLISECONDS:
					divisor = 1;
					break;
				case SECONDS:
					divisor = 1000;
					break;
				case MINUTE:
					divisor = 60000;
					break;
				case HOUR:
					divisor = 3600000;
					break;
			}
			if ( divisor>0) {
				channel.setNextValue(((Date) record.getDataValue()).getTime() / divisor);
			}
			return;
		}

		//Nominator and denominator for multipliers which are not powers of 10, e.g. hours, minutes
		int nominator=-1;
		int denominator=-1;
		//Exponent for scaling by powers of 10
		int scaleFactor=record.getMultiplierExponent()-openemsUnit.getScaleFactor();
		DlmsUnit mbusUnit=record.getUnit();

		//replacing unit by its BaseUnit. For this reason channelScaleFactor has to be evaluated beforehand
		if (channel.channelDoc().getUnit().getBaseUnit() != null) {
			openemsUnit=openemsUnit.getBaseUnit();
		}

		//setting nominator and denominator: [mbusunit]=nominator/denominator * [openemsbaseunit]
		switch (openemsUnit) {
			case CUBIC_METER:
				switch(mbusUnit) {
					case CUBIC_METRE:
						nominator=1;
						denominator=1;
						break;
					}
				break;
			//return error
			case CUBICMETER_PER_HOUR:
				switch(mbusUnit) {
					case CUBIC_METRE_PER_DAY:
						nominator=1;
						denominator=24;
						break;
					case CUBIC_METRE_PER_HOUR:
						nominator=1;
						denominator=1;
						break;
					case CUBIC_METRE_PER_MINUTE:
						nominator=60;
						denominator=1;
						break;
					case CUBIC_METRE_PER_SECOND:
						nominator=3600;
						denominator=1;
						break;
				}
				break;
			case CUBICMETER_PER_SECOND:
				switch(mbusUnit) {
					case CUBIC_METRE_PER_DAY:
						nominator=1;
						denominator=86400;
						break;
					case CUBIC_METRE_PER_HOUR:
						nominator=1;
						denominator=3600;
						break;
					case CUBIC_METRE_PER_MINUTE:
						nominator=1;
						denominator=60;
						break;
					case CUBIC_METRE_PER_SECOND:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case SECONDS:
				switch (mbusUnit) {
					case SECOND:
						nominator=1;
						denominator=1;
						break;
					case MIN:
						nominator=60;
						denominator=1;
						break;
					case HOUR:
						nominator=3600;
						denominator=1;
						break;
					case DAY:
						nominator=86400;
						denominator=1;
						break;
				}
				break;
			case MINUTE:
				switch(mbusUnit) {
					case SECOND:
						nominator=1;
						denominator=60;
						break;
					case MIN:
						nominator=1;
						denominator=1;
						break;
					case HOUR:
						nominator=60;
						denominator=1;
						break;
					case DAY:
						nominator=1440;
						denominator=1;
						break;
				}
				break;
			case HOUR:
				switch(mbusUnit) {
					case SECOND:
						nominator=1;
						denominator=3660;
						break;
					case MIN:
						nominator=1;
						denominator=60;
						break;
					case HOUR:
						nominator=1;
						denominator=1;
						break;
					case DAY:
						nominator=60;
						denominator=1;
						break;
				}
				break;
			case DEGREE_CELSIUS:
				switch(mbusUnit) {
					case DEGREE_CELSIUS:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case BAR:
				switch(mbusUnit) {
					case BAR:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case WATT:
				switch(mbusUnit) {
					case WATT:
						nominator=1;
						denominator=1;
						break;
					case JOULE_PER_HOUR:
						nominator=1;
						denominator=3600;
						break;
				}
				break;
			case WATT_SECONDS:
				switch(mbusUnit) {
					case JOULE:
						nominator=1;
						denominator=1;
						break;
					case WATT_HOUR:
						nominator=3600;
						denominator=1;
						break;
				}
				break;
			case VOLT_AMPERE_REACTIVE:
				switch (mbusUnit) {
					case VAR:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case VOLT_AMPERE_REACTIVE_HOURS:
				switch (mbusUnit) {
					case VAR_HOUR:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case VOLT_AMPERE:
				switch (mbusUnit) {
					case VOLT_AMPERE:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case VOLT_AMPERE_HOURS:
				switch (mbusUnit) {
					case VOLT_AMPERE_HOUR:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case WATT_HOURS:
				switch(mbusUnit) {
					case JOULE:
						nominator=1;
						denominator=3600;
						break;
					case WATT_HOUR:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case VOLT:
				switch(mbusUnit) {
					case VOLT:
						nominator=1;
						denominator=1;
						break;
				}
				break;
			case AMPERE:
				switch (mbusUnit) {
					case AMPERE:
						nominator=1;
						denominator=1;
						break;
				}
				break;
		}
		if ( nominator > 0 & denominator > 0) {
			channel.setNextValue(((Number) record.getDataValue()).doubleValue() * nominator * Math.pow(10, scaleFactor) / denominator);
		}
	}


}
