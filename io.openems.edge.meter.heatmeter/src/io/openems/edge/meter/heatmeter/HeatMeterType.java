package io.openems.edge.meter.heatmeter;


public enum HeatMeterType {

    ITRON_CF_51(3, 4, 1, 5, 6),
    ZELSIUS_C5_CMF(0,0,0,0,0),
    SHARKY_775(4, 5, 0, 6, 7);


    int powerAddress;
    int percolationAddress;
    int totalConsumptionEnergyAddress;
    int flowTempAddress;
    int returnTempAddress;

    HeatMeterType(int power, int percolation, int totalConsumptionEnergy, int flowTemp, int returnTemp) {
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
