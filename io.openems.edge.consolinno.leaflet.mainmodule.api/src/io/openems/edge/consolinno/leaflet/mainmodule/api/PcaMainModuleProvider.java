package io.openems.edge.consolinno.leaflet.mainmodule.api;

import java.io.IOException;
import java.util.List;

public interface PcaMainModuleProvider {
    boolean getDataOnPinPosition(int address) throws IOException;

    // void writeToPinPosition(List<Boolean> onOff) throws IOException;
    void writeToPinPosition(boolean onOff) throws IOException;

    String getVersion();

    String getModuleId();

}
