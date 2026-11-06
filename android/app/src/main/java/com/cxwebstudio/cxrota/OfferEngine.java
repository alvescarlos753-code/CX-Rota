package com.cxwebstudio.cxrota;

import java.util.Locale;

/** Regra pura Java. Os limites são configuráveis, não uma garantia de lucro. */
public final class OfferEngine {
    private OfferEngine() {}

    public enum Decision { ACCEPT, REVIEW, DECLINE, INCOMPLETE }

    public static final class Offer {
        public final String platform;
        public final double fare;
        public final double distanceKm;
        public final double minutes;
        public final boolean includesPickup;

        public Offer(String platform, double fare, double distanceKm, double minutes, boolean includesPickup) {
            this.platform = platform;
            this.fare = fare;
            this.distanceKm = distanceKm;
            this.minutes = minutes;
            this.includesPickup = includesPickup;
        }
    }

    public static final class Rules {
        public final double minPerKm;
        public final double minPerHour;
        public final double costPerKm;
        public final boolean costConfigured;

        public Rules(double minPerKm, double minPerHour, double costPerKm, boolean costConfigured) {
            this.minPerKm = minPerKm;
            this.minPerHour = minPerHour;
            this.costPerKm = costPerKm;
            this.costConfigured = costConfigured;
        }
    }

    public static final class Result {
        public final Offer offer;
        public final Decision decision;
        public final double perKm;
        public final double perHour;
        public final double estimatedNet;
        public final boolean costConfigured;
        public final String reason;

        Result(Offer offer, Decision decision, double perKm, double perHour, double estimatedNet, boolean costConfigured, String reason) {
            this.offer = offer; this.decision = decision; this.perKm = perKm; this.perHour = perHour;
            this.estimatedNet = estimatedNet; this.costConfigured = costConfigured; this.reason = reason;
        }
    }

    public static Result evaluate(Offer offer, Rules rules) {
        if (offer == null || !Double.isFinite(offer.fare) || !Double.isFinite(offer.distanceKm)
                || !Double.isFinite(offer.minutes) || offer.fare <= 0 || offer.distanceKm <= 0 || offer.minutes <= 0) {
            return new Result(offer, Decision.INCOMPLETE, 0, 0, 0, rules.costConfigured,
                    "Valor, distância e tempo precisam estar disponíveis.");
        }
        double perKm = offer.fare / offer.distanceKm;
        double perHour = offer.fare * 60 / offer.minutes;
        double net = offer.fare - offer.distanceKm * rules.costPerKm;
        if (!offer.includesPickup) {
            return new Result(offer, Decision.REVIEW, perKm, perHour, net, rules.costConfigured,
                    "Confira deslocamento até o embarque: pode não estar incluído.");
        }
        boolean kmOk = perKm >= rules.minPerKm;
        boolean hourOk = perHour >= rules.minPerHour;
        Decision decision = kmOk && hourOk ? Decision.ACCEPT : (!kmOk && !hourOk ? Decision.DECLINE : Decision.REVIEW);
        if (rules.costConfigured && net <= 0) decision = Decision.DECLINE;
        String reason = decision == Decision.ACCEPT ? "Acima das duas metas configuradas."
                : decision == Decision.DECLINE ? "Abaixo das metas ou do custo estimado."
                : "Uma das metas não foi atingida; avalie o contexto.";
        return new Result(offer, decision, perKm, perHour, net, rules.costConfigured, reason);
    }

    public static String money(double amount) {
        return String.format(new Locale("pt", "BR"), "R$ %.2f", amount);
    }
    public static String number(double amount) {
        return String.format(new Locale("pt", "BR"), "%.2f", amount);
    }
}
