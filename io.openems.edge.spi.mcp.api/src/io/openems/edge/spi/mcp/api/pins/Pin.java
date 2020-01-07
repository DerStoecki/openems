package io.openems.edge.spi.mcp.api.pins;

public interface Pin {
    int getPosition();

    long getValue();

    boolean isUsed();

    String getUsedBy();

    boolean setUsedBy(String usedBy);

    void setUnused();
}
