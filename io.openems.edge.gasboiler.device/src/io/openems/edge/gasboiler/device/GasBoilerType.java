package io.openems.edge.gasboiler.device;

public enum GasBoilerType {

    ;


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
    ){


    }
}
