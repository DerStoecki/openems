package io.openems.edge.consolinno.leaflet.mainmodule.api;

import java.io.IOException;

public interface PcaMainModuleProvider {
    boolean getDataOnPinPosition(int address) throws IOException;

    void writeToPinPosition(boolean onOff) throws IOException;
}
