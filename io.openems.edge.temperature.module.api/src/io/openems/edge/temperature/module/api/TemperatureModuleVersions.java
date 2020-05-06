package io.openems.edge.temperature.module.api;

public enum TemperatureModuleVersions {

    TEMPERATURE_MODULE_V_1(0.0000038937,0.021592132466482,-40.8774465191316);


    private final double regressionValueA;
    private final double regressionValueB;
    private final double regressionValueC;


    TemperatureModuleVersions(double regressionValueA, double regressionValueB, double regression_value_c) {


        this.regressionValueA = regressionValueA;
        this.regressionValueB = regressionValueB;
        this.regressionValueC = regression_value_c;

    }

    public double getRegressionValueA() {
        return regressionValueA;
    }

    public double getRegressionValueB() {
        return regressionValueB;
    }

    public double getRegressionValueC() {
        return regressionValueC;
    }

}