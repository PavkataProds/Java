package bg.sofia.uni.fmi.mjt.simcity.utility;

import bg.sofia.uni.fmi.mjt.simcity.property.billable.Billable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UtilityService implements UtilityServiceAPI {

    private final Map<UtilityType, Double> taxRates;

    public UtilityService(Map<UtilityType, Double> taxRates) {
        this.taxRates = taxRates;
    }

    @Override
    public <T extends Billable> double getUtilityCosts(UtilityType utilityType, T billable) {
        if (utilityType == null || billable == null) {
            throw new IllegalArgumentException();
        }
        double taxRate = taxRates.get(utilityType);
        if (utilityType.equals(UtilityType.ELECTRICITY)) {
            return taxRate * billable.getElectricityConsumption();
        }
        if (utilityType.equals(UtilityType.NATURAL_GAS)) {
            return taxRate * billable.getNaturalGasConsumption();
        }
        if (utilityType.equals(UtilityType.WATER)) {
            return taxRate * billable.getWaterConsumption();
        }
        return 0;
    }

    @Override
    public <T extends Billable> double getTotalUtilityCosts(T billable) {
        if (billable == null) {
            throw new IllegalArgumentException();
        }
        return taxRates.get(UtilityType.ELECTRICITY) * billable.getElectricityConsumption()
                + taxRates.get(UtilityType.WATER) * billable.getWaterConsumption()
                + taxRates.get(UtilityType.NATURAL_GAS) * billable.getNaturalGasConsumption();
    }

    @Override
    public <T extends Billable> Map<UtilityType, Double> computeCostsDifference(T firstBillable, T secondBillable) {
        if (firstBillable == null || secondBillable == null) {
            throw new IllegalArgumentException();
        }
        Map<UtilityType, Double> costsDifference = new HashMap<>();

        double electricityBill = taxRates.get(UtilityType.ELECTRICITY);
        double gasBill = taxRates.get(UtilityType.NATURAL_GAS);
        double waterBill = taxRates.get(UtilityType.WATER);

        costsDifference.put(UtilityType.ELECTRICITY,
                Math.abs(electricityBill * firstBillable.getElectricityConsumption()
                        - electricityBill * secondBillable.getElectricityConsumption()));
        costsDifference.put(UtilityType.NATURAL_GAS, Math.abs(gasBill * firstBillable.getNaturalGasConsumption()
                - gasBill * secondBillable.getNaturalGasConsumption()));
        costsDifference.put(UtilityType.WATER, Math.abs(waterBill * firstBillable.getWaterConsumption()
                - waterBill * secondBillable.getWaterConsumption()));
        return Collections.unmodifiableMap(costsDifference);
    }
}