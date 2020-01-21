package io.openems.edge.meter.gasmeter;

public enum GasMeterType {
    PLACEHOLDER(0, 0, 0, 0, 0);

    int powerAddress;
    int percolationAddress;
    int totalConsumptionEnergyAddress;
    int flowTempAddress;
    int returnTempAddress;

    GasMeterType(int power, int percolation, int totalConsumptionEnergy, int flowTemp, int returnTemp) {
        this.powerAddress = power;
        this.percolationAddress = percolation;
        this.totalConsumptionEnergyAddress = totalConsumptionEnergy;

        this.flowTempAddress = flowTemp;
        this.returnTempAddress = returnTemp;
    }


    public int getPowerAddress() {
        return powerAddress;
    }

    public int getPercolationAddress() {
        return percolationAddress;
    }

    public int getTotalConsumptionEnergyAddress() {
        return totalConsumptionEnergyAddress;
    }

    public int getFlowTempAddress() {
        return flowTempAddress;
    }

    public int getReturnTempAddress() {
        return returnTempAddress;
    }
}
