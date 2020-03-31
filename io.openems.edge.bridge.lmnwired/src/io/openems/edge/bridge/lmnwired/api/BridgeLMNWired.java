package io.openems.edge.bridge.lmnwired.api;

import io.openems.edge.bridge.lmnwired.api.task.LMNWiredTask;
import io.openems.edge.bridge.lmnwired.hdlc.PackageHandler;
import io.openems.edge.common.channel.Doc;
import io.openems.common.channel.Level;

import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

import io.openems.common.channel.Debounce;
import io.openems.common.types.OpenemsType;

public interface BridgeLMNWired {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        SLAVE_COMMUNICATION_FAILED(Doc.of(Level.FAULT) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        CYCLE_TIME_IS_TOO_SHORT(Doc.of(Level.WARNING) //
                .debounce(10, Debounce.TRUE_VALUES_IN_A_ROW_TO_SET_TRUE)), //
        EXECUTION_DURATION(Doc.of(OpenemsType.LONG));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    List<Device> deviceList = new ArrayList<Device>();
    PackageHandler packageHandler = null;
    public PackageHandler getPackageHandler();

    public void addTask(String sourceId, LMNWiredTask task);

    public SerialPort getSerialConnection();

    public void removeTask(String sourceId);

    public List<Device> getDeviceList();

}
