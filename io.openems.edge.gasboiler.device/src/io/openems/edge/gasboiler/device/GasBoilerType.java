package io.openems.edge.gasboiler.device;

public enum GasBoilerType {

    VITOTRONIC_100(8,9,5,6,
            0,0,0,0,
            0, 0,0,0,
            0, 0,0,
            0, 0,0,
            0, 0,0,
            0, 11,0,
            0, 0,0,
            0, 0,0,
            0, 0,0,
            0, 0,0,
            0, 0,0,
            0, 0,0,
            8, 1,0,
            3, 0,4,
            0, 0,0,
            0, 0,0,
            0, 0,0
            );



    int outPutAM1_1_Address, outPutAm1_2_Address, output20Address, output_29Address,
            ambientTemperatureAddress, outPutEA1Address, input_EA_1Address, input_EA_2Address, input_EA_3Address,
            setPoint_EA_1Address, outPutSignalPM1_PercentAddress, gridVoltageBehaviourPM1Address,
            volumeFlowSetPoPm1Address, disturbanceInputPM1Address, temperatureSensorPM_1_1Address,
            temperatureSensorPM1_2Address, temperatureSensorPM1_3Address, temperatureSensorPM1_4Address,
            rewindTemperature_17_A_Address, sensor9address, tributaryPumpAddress, operatingModeA1_M1,
            boilerSetPoTemperatureAddress, combustionEngineExhaustTemperatureAddress, combustionEngineOnOffAddress,
            combustionEngineOperatingHoursTier1Address, combustionEngineOperatingHoursTier2Address,
            combustionEngineEfficiencyActualValuePercentAddress, combustionEngineStartCounterAddress,
            combustionEngineOperatingModeAddress, combustionEngineBoilerTemperatureAddress,
            temperatureSensor_1_PM_1_StatusAddress, temperatureSensor_1_PM_2_StatusAddress,
            temperatureSensor_1_PM_3_StatusAddress, temperatureSensor_1_PM_4_StatusAddress,
            operatingHoursCombustionEngineTier_1_expandedAddress, heatBoilerOperationModeAddress,
            heatBoilerTemperatureSetPointEffectiveAddress, heatBoilerPerformanceStatusAddress,
            heatBoilerPerformanceSetPointStatusAddress, heatBoilerPerformanceSetPointValueAddress,
            heatBoilerTemperatureSetPoAddress, heatBoilerTemperatureActualAddress,
            heatBoilerModulationValueAddress, warmWaterOperationModeAddress,
            warmWaterEffectiveSetPointTemperatureAddress, functioningWarmWaterSetPoTemperatureAddress,
            boilerSetPointPerformanceEffectiveAddress, boilerSetPointTemperatureEffectiveAddress,
            boilerMaxReachedExhaustTemperatureAddress, warmWaterStorageChargePumpAddress,
            warmWaterStorageTemperature_5_A_Address, warmWaterStorageTemperature_5_B_Address,
            warmWaterPreparationAddress, warmWaterTemperatureSetPoAddress,
            warmWaterSetPoEffectiveAddress, warmWaterCirculationPumpAddress;

    GasBoilerType(
            int outPutAM1_1_Address, int outPutAm1_2_Address, int output20Address, int output_29Address,
            int ambientTemperatureAddress, int outPutEA1Address, int input_EA_1Address, int input_EA_2Address, int input_EA_3Address,
            int setPoint_EA_1Address, int outPutSignalPM1_PercentAddress, int gridVoltageBehaviourPM1Address,
            int volumeFlowSetPointPm1Address, int disturbanceInputPM1Address, int temperatureSensorPM_1_1Address,
            int temperatureSensorPM1_2Address, int temperatureSensorPM1_3Address, int temperatureSensorPM1_4Address,
            int rewindTemperature_17_A_Address, int sensor9address, int tributaryPumpAddress, int operatingModeA1_M1,
            int boilerSetPointTemperatureAddress, int combustionEngineExhaustTemperatureAddress, int combustionEngineOnOffAddress,
            int combustionEngineOperatingHoursTier1Address, int combustionEngineOperatingHoursTier2Address,
            int combustionEngineEfficiencyActualValuePercentAddress, int combustionEngineStartCounterAddress,
            int combustionEngineOperatingModeAddress, int combustionEngineBoilerTemperatureAddress,
            int temperatureSensor_1_PM_1_StatusAddress, int temperatureSensor_1_PM_2_StatusAddress,
            int temperatureSensor_1_PM_3_StatusAddress, int temperatureSensor_1_PM_4_StatusAddress,
            int operatingHoursCombustionEngineTier_1_expandedAddress, int heatBoilerOperationModeAddress,
            int heatBoilerTemperatureSetPointEffectiveAddress, int heatBoilerPerformanceStatusAddress,
            int heatBoilerPerformanceSetPointStatusAddress, int heatBoilerPerformanceSetPointValueAddress,
            int heatBoilerTemperatureSetPointAddress, int heatBoilerTemperatureActualAddress,
            int heatBoilerModulationValueAddress, int warmWaterOperationModeAddress,
            int warmWaterEffectiveSetPointTemperatureAddress, int functioningWarmWaterSetPointTemperatureAddress,
            int boilerSetPointPerformanceEffectiveAddress, int boilerSetPointTemperatureEffectiveAddress,
            int boilerMaxReachedExhaustTemperatureAddress, int warmWaterStorageChargePumpAddress,
            int warmWaterStorageTemperature_5_A_Address, int warmWaterStorageTemperature_5_B_Address,
            int warmWaterPreparationAddress, int warmWaterTemperatureSetPointAddress,
            int warmWaterSetPointEffectiveAddress, int warmWaterCirculationPumpAddress
    ) {

        this.outPutAM1_1_Address = outPutAM1_1_Address;
        this.outPutAm1_2_Address = outPutAm1_2_Address;
        this.output20Address = output20Address;
        this.output_29Address = output_29Address;
        this.ambientTemperatureAddress = ambientTemperatureAddress;
        this.outPutEA1Address = outPutEA1Address;
        this.input_EA_1Address = input_EA_1Address;
        this.input_EA_2Address = input_EA_2Address;
        this.input_EA_3Address = input_EA_3Address;

        this.setPoint_EA_1Address = setPoint_EA_1Address;
        this.outPutSignalPM1_PercentAddress = outPutSignalPM1_PercentAddress;
        this.gridVoltageBehaviourPM1Address = gridVoltageBehaviourPM1Address;

        this.volumeFlowSetPoPm1Address = volumeFlowSetPointPm1Address;
        this.disturbanceInputPM1Address = disturbanceInputPM1Address;
        this.temperatureSensorPM_1_1Address = temperatureSensorPM_1_1Address;

        this.temperatureSensorPM1_2Address = temperatureSensorPM1_2Address;
        this.temperatureSensorPM1_3Address = temperatureSensorPM1_3Address;
        this.temperatureSensorPM1_4Address = temperatureSensorPM1_4Address;

        this.rewindTemperature_17_A_Address = rewindTemperature_17_A_Address;
        this.sensor9address = sensor9address;
        this.tributaryPumpAddress = tributaryPumpAddress;
        this.operatingModeA1_M1 = operatingModeA1_M1;

        this.boilerSetPoTemperatureAddress = boilerSetPointTemperatureAddress;
        this.combustionEngineExhaustTemperatureAddress = combustionEngineExhaustTemperatureAddress;
        this.combustionEngineOnOffAddress = combustionEngineOnOffAddress;

        this.combustionEngineOperatingHoursTier1Address = combustionEngineOperatingHoursTier1Address;
        this.combustionEngineOperatingHoursTier2Address = combustionEngineOperatingHoursTier2Address;

        this.combustionEngineEfficiencyActualValuePercentAddress = combustionEngineEfficiencyActualValuePercentAddress;
        this.combustionEngineStartCounterAddress = combustionEngineStartCounterAddress;

        this.combustionEngineOperatingModeAddress = combustionEngineOperatingModeAddress;
        this.combustionEngineBoilerTemperatureAddress = combustionEngineBoilerTemperatureAddress;

        this.temperatureSensor_1_PM_1_StatusAddress = temperatureSensor_1_PM_1_StatusAddress;
        this.temperatureSensor_1_PM_2_StatusAddress = temperatureSensor_1_PM_2_StatusAddress;

        this.temperatureSensor_1_PM_3_StatusAddress = temperatureSensor_1_PM_3_StatusAddress;
        this.temperatureSensor_1_PM_4_StatusAddress = temperatureSensor_1_PM_4_StatusAddress;

        this.operatingHoursCombustionEngineTier_1_expandedAddress = operatingHoursCombustionEngineTier_1_expandedAddress;
        this.heatBoilerOperationModeAddress = heatBoilerOperationModeAddress;

        this.heatBoilerTemperatureSetPointEffectiveAddress = heatBoilerTemperatureSetPointEffectiveAddress;
        this.heatBoilerPerformanceStatusAddress = heatBoilerPerformanceStatusAddress;

        this.heatBoilerPerformanceSetPointStatusAddress = heatBoilerPerformanceSetPointStatusAddress;
        this.heatBoilerPerformanceSetPointValueAddress = heatBoilerPerformanceSetPointValueAddress;

        this.heatBoilerTemperatureSetPoAddress = heatBoilerTemperatureSetPointAddress;
        this.heatBoilerTemperatureActualAddress = heatBoilerTemperatureActualAddress;

        this.heatBoilerModulationValueAddress = heatBoilerModulationValueAddress;
        this.warmWaterOperationModeAddress = warmWaterOperationModeAddress;

        this.warmWaterEffectiveSetPointTemperatureAddress = warmWaterEffectiveSetPointTemperatureAddress;
        this.functioningWarmWaterSetPoTemperatureAddress = functioningWarmWaterSetPointTemperatureAddress;

        this.boilerSetPointPerformanceEffectiveAddress = boilerSetPointPerformanceEffectiveAddress;
        this.boilerSetPointTemperatureEffectiveAddress = boilerSetPointTemperatureEffectiveAddress;

        this.boilerMaxReachedExhaustTemperatureAddress = boilerMaxReachedExhaustTemperatureAddress;
        this.warmWaterStorageChargePumpAddress = warmWaterStorageChargePumpAddress;

        this.warmWaterStorageTemperature_5_A_Address = warmWaterStorageTemperature_5_A_Address;
        this.warmWaterStorageTemperature_5_B_Address = warmWaterStorageTemperature_5_B_Address;

        this.warmWaterPreparationAddress = warmWaterPreparationAddress;
        this.warmWaterTemperatureSetPoAddress = warmWaterTemperatureSetPointAddress;

        this.warmWaterSetPoEffectiveAddress = warmWaterSetPointEffectiveAddress;
        this.warmWaterCirculationPumpAddress = warmWaterCirculationPumpAddress;

    }


    public int getOutPutAM1_1_Address() {
        return outPutAM1_1_Address;
    }

    public int getOutPutAm1_2_Address() {
        return outPutAm1_2_Address;
    }

    public int getOutput20Address() {
        return output20Address;
    }

    public int getOutput_29Address() {
        return output_29Address;
    }

    public int getAmbientTemperatureAddress() {
        return ambientTemperatureAddress;
    }

    public int getOutPutEA1Address() {
        return outPutEA1Address;
    }

    public int getInput_EA_1Address() {
        return input_EA_1Address;
    }

    public int getInput_EA_2Address() {
        return input_EA_2Address;
    }

    public int getInput_EA_3Address() {
        return input_EA_3Address;
    }

    public int getSetPoint_EA_1Address() {
        return setPoint_EA_1Address;
    }

    public int getOutPutSignalPM1_PercentAddress() {
        return outPutSignalPM1_PercentAddress;
    }

    public int getGridVoltageBehaviourPM1Address() {
        return gridVoltageBehaviourPM1Address;
    }

    public int getVolumeFlowSetPoPm1Address() {
        return volumeFlowSetPoPm1Address;
    }

    public int getDisturbanceInputPM1Address() {
        return disturbanceInputPM1Address;
    }

    public int getTemperatureSensorPM_1_1Address() {
        return temperatureSensorPM_1_1Address;
    }

    public int getTemperatureSensorPM1_2Address() {
        return temperatureSensorPM1_2Address;
    }

    public int getTemperatureSensorPM1_3Address() {
        return temperatureSensorPM1_3Address;
    }

    public int getTemperatureSensorPM1_4Address() {
        return temperatureSensorPM1_4Address;
    }

    public int getRewindTemperature_17_A_Address() {
        return rewindTemperature_17_A_Address;
    }

    public int getSensor9address() {
        return sensor9address;
    }

    public int getTributaryPumpAddress() {
        return tributaryPumpAddress;
    }

    public int getOperatingModeA1_M1() {
        return operatingModeA1_M1;
    }

    public int getBoilerSetPoTemperatureAddress() {
        return boilerSetPoTemperatureAddress;
    }

    public int getCombustionEngineExhaustTemperatureAddress() {
        return combustionEngineExhaustTemperatureAddress;
    }

    public int getCombustionEngineOnOffAddress() {
        return combustionEngineOnOffAddress;
    }

    public int getCombustionEngineOperatingHoursTier1Address() {
        return combustionEngineOperatingHoursTier1Address;
    }

    public int getCombustionEngineOperatingHoursTier2Address() {
        return combustionEngineOperatingHoursTier2Address;
    }

    public int getCombustionEngineEfficiencyActualValuePercentAddress() {
        return combustionEngineEfficiencyActualValuePercentAddress;
    }

    public int getCombustionEngineStartCounterAddress() {
        return combustionEngineStartCounterAddress;
    }

    public int getCombustionEngineOperatingModeAddress() {
        return combustionEngineOperatingModeAddress;
    }

    public int getCombustionEngineBoilerTemperatureAddress() {
        return combustionEngineBoilerTemperatureAddress;
    }

    public int getTemperatureSensor_1_PM_1_StatusAddress() {
        return temperatureSensor_1_PM_1_StatusAddress;
    }

    public int getTemperatureSensor_1_PM_2_StatusAddress() {
        return temperatureSensor_1_PM_2_StatusAddress;
    }

    public int getTemperatureSensor_1_PM_3_StatusAddress() {
        return temperatureSensor_1_PM_3_StatusAddress;
    }

    public int getTemperatureSensor_1_PM_4_StatusAddress() {
        return temperatureSensor_1_PM_4_StatusAddress;
    }

    public int getOperatingHoursCombustionEngineTier_1_expandedAddress() {
        return operatingHoursCombustionEngineTier_1_expandedAddress;
    }

    public int getHeatBoilerOperationModeAddress() {
        return heatBoilerOperationModeAddress;
    }

    public int getHeatBoilerTemperatureSetPointEffectiveAddress() {
        return heatBoilerTemperatureSetPointEffectiveAddress;
    }

    public int getHeatBoilerPerformanceStatusAddress() {
        return heatBoilerPerformanceStatusAddress;
    }

    public int getHeatBoilerPerformanceSetPointStatusAddress() {
        return heatBoilerPerformanceSetPointStatusAddress;
    }

    public int getHeatBoilerPerformanceSetPointValueAddress() {
        return heatBoilerPerformanceSetPointValueAddress;
    }

    public int getHeatBoilerTemperatureSetPoAddress() {
        return heatBoilerTemperatureSetPoAddress;
    }

    public int getHeatBoilerTemperatureActualAddress() {
        return heatBoilerTemperatureActualAddress;
    }

    public int getHeatBoilerModulationValueAddress() {
        return heatBoilerModulationValueAddress;
    }

    public int getWarmWaterOperationModeAddress() {
        return warmWaterOperationModeAddress;
    }

    public int getWarmWaterEffectiveSetPointTemperatureAddress() {
        return warmWaterEffectiveSetPointTemperatureAddress;
    }

    public int getFunctioningWarmWaterSetPoTemperatureAddress() {
        return functioningWarmWaterSetPoTemperatureAddress;
    }

    public int getBoilerSetPointPerformanceEffectiveAddress() {
        return boilerSetPointPerformanceEffectiveAddress;
    }

    public int getBoilerSetPointTemperatureEffectiveAddress() {
        return boilerSetPointTemperatureEffectiveAddress;
    }

    public int getBoilerMaxReachedExhaustTemperatureAddress() {
        return boilerMaxReachedExhaustTemperatureAddress;
    }

    public int getWarmWaterStorageChargePumpAddress() {
        return warmWaterStorageChargePumpAddress;
    }

    public int getWarmWaterStorageTemperature_5_A_Address() {
        return warmWaterStorageTemperature_5_A_Address;
    }

    public int getWarmWaterStorageTemperature_5_B_Address() {
        return warmWaterStorageTemperature_5_B_Address;
    }

    public int getWarmWaterPreparationAddress() {
        return warmWaterPreparationAddress;
    }

    public int getWarmWaterTemperatureSetPoAddress() {
        return warmWaterTemperatureSetPoAddress;
    }

    public int getWarmWaterSetPoEffectiveAddress() {
        return warmWaterSetPoEffectiveAddress;
    }

    public int getWarmWaterCirculationPumpAddress() {
        return warmWaterCirculationPumpAddress;
    }
}
