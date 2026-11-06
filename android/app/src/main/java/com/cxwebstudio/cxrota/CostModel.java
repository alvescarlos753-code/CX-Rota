package com.cxwebstudio.cxrota;

/** Modelo puro e auditável para moto própria ou alugada. Valores monetários em reais. */
public final class CostModel {
    public enum Ownership { OWNED, RENTED }
    public enum Period { DAILY, WEEKLY, MONTHLY }
    public final Ownership ownership;
    public final Period period;
    public final double rentalFee;
    public final int workDaysPerWeek;
    public final int workDaysPerMonth;
    public final double fuelPricePerLiter;
    public final double kmPerLiter;
    public final double maintenancePerKm;
    public final double otherDaily;
    public final double plannedDailyKm;

    public CostModel(Ownership ownership, Period period, double rentalFee, int workDaysPerWeek,
                     int workDaysPerMonth, double fuelPricePerLiter, double kmPerLiter,
                     double maintenancePerKm, double otherDaily, double plannedDailyKm) {
        this.ownership = ownership;
        this.period = period;
        this.rentalFee = rentalFee;
        this.workDaysPerWeek = workDaysPerWeek;
        this.workDaysPerMonth = workDaysPerMonth;
        this.fuelPricePerLiter = fuelPricePerLiter;
        this.kmPerLiter = kmPerLiter;
        this.maintenancePerKm = maintenancePerKm;
        this.otherDaily = otherDaily;
        this.plannedDailyKm = plannedDailyKm;
        validate();
    }
    public void validate() {
        if (ownership == null || period == null || workDaysPerWeek < 1 || workDaysPerWeek > 7
                || workDaysPerMonth < 1 || workDaysPerMonth > 31 ||
                !finiteNonnegative(rentalFee) || !finiteNonnegative(fuelPricePerLiter)
                || !finiteNonnegative(kmPerLiter) || !finiteNonnegative(maintenancePerKm)
                || !finiteNonnegative(otherDaily) || !finiteNonnegative(plannedDailyKm)) {
            throw new IllegalArgumentException("Confira aluguel, consumo, custos e dias trabalhados.");
        }
    }
    private static boolean finiteNonnegative(double v) { return Double.isFinite(v) && v >= 0; }
    public boolean fuelConfigured() { return fuelPricePerLiter > 0 && kmPerLiter > 0; }
    public boolean costsComplete() { return fuelConfigured() && plannedDailyKm > 0
            && (ownership != Ownership.RENTED || rentalFee > 0); }
    public double fuelPerKm() { return fuelConfigured() ? fuelPricePerLiter/kmPerLiter : 0; }
    public double variablePerKm() { return fuelPerKm() + maintenancePerKm; }
    public double rentalDaily() {
        if (ownership != Ownership.RENTED) return 0;
        switch(period) {
            case DAILY: return rentalFee;
            case WEEKLY: return rentalFee/workDaysPerWeek;
            case MONTHLY: return rentalFee/workDaysPerMonth;
            default: throw new IllegalStateException();
        }
    }
    public double fixedPerDay() { return rentalDaily() + otherDaily; }
    /** Rateio para comparação entre ofertas; não equivale ao custo incremental da corrida. */
    public double allocatedPerKm() {
        return plannedDailyKm > 0 ? variablePerKm() + fixedPerDay()/plannedDailyKm : variablePerKm();
    }
    /** Despesa econômica da jornada, independente de quando o tanque foi abastecido. */
    public double dailyCost(double drivenKm, double incidentals) {
        if (!finiteNonnegative(drivenKm) || !finiteNonnegative(incidentals))
            throw new IllegalArgumentException("KM e outras despesas não podem ser negativos.");
        return fixedPerDay() + drivenKm*variablePerKm() + incidentals;
    }
    public double netDay(double gross, double drivenKm, double incidentals) {
        if (!finiteNonnegative(gross)) throw new IllegalArgumentException("Receita inválida.");
        return gross - dailyCost(drivenKm, incidentals);
    }
    public double breakEvenGrossPerDay() { return fixedPerDay() + plannedDailyKm*variablePerKm(); }
}
