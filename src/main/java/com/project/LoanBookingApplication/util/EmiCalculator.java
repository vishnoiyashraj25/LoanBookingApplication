package com.project.LoanBookingApplication.util;

public final class EmiCalculator {

    private EmiCalculator(){

    }
    public static Double calculateEmi(Double principal, Double annualRate, Integer tenure) {

        double monthlyRate = annualRate / 12 / 100;

        if (monthlyRate == 0) {
            return Math.round((principal / tenure) * 100.0) / 100.0;
        }

        double numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, tenure);
        double denominator = Math.pow(1 + monthlyRate, tenure) - 1;

        return Math.round((numerator / denominator) * 100.0) / 100.0;
    }

}
