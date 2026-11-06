package com.cxwebstudio.cxrota;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Leitor conservador: rejeita campos ambíguos em vez de inventar valores. */
public final class OfferParser {
    private OfferParser() {}

    private static final Pattern FARE = Pattern.compile("(?:R\\$)\\s*([0-9]{1,4}(?:[.,][0-9]{2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KM = Pattern.compile("(?<![0-9])([0-9]+(?:[.,][0-9]+)?)\\s*(?:km|quil[oô]metros?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN = Pattern.compile("(?<![0-9])([0-9]+)\\s*(?:min(?:utos?)?|m)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOUR = Pattern.compile("(?<![0-9])([0-9]+)\\s*(?:h|horas?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern IGNORE = Pattern.compile("(?i)(R\\$\\s*[0-9,.]+\\s*/\\s*(?:km|h|hora)|ganho\\s+por\\s+km|tarifa\\s+por\\s+km)");

    public static OfferEngine.Offer parse(String platform, String content) {
        if (content == null || content.isBlank()) return null;
        String normalized = content.replace('\u00a0', ' ').replace('\r', '\n');
        String[] lines = normalized.split("\\n");
        // Evita confundir painel de ganhos com uma oferta nova.
        if (!Pattern.compile("(?i)(aceitar|recusar|nova corrida|nova viagem|oferta|solicita[cç][aã]o|embarque|destino|passageiro)")
                .matcher(normalized).find()) return null;
        double fare = -1;
        List<Double> kms = new ArrayList<>();
        List<Double> minutes = new ArrayList<>();
        boolean sawPickup = false;
        boolean sawTrip = false;
        boolean sawTotal = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || IGNORE.matcher(line).find()) continue;
            String lower = line.toLowerCase(Locale.ROOT);
            Matcher mFare = FARE.matcher(line);
            // A primeira cifra da oferta pode ser a tarifa, mas nunca valores/km, bônus ou metas.
            if (fare < 0 && !lower.matches(".*(b[oô]nus|incentivo|promo[cç][aã]o|ped[aá]gio).*")) {
                if (mFare.find()) fare = decimal(mFare.group(1));
            }
            Matcher mKm = KM.matcher(line);
            while (mKm.find()) {
                double v = decimal(mKm.group(1));
                if (v > 0 && v < 1000) kms.add(v);
            }
            Matcher mMin = MIN.matcher(line);
            while (mMin.find()) {
                double v = decimal(mMin.group(1));
                if (v > 0 && v < 600) minutes.add(v);
            }
            Matcher mH = HOUR.matcher(line);
            while (mH.find()) {
                double v = decimal(mH.group(1));
                if (v > 0 && v <= 10) minutes.add(v * 60);
            }
            if (lower.matches(".*(embarque|buscar|passageiro|at[eé]\\s+o\\s+local).*")) sawPickup = true;
            if (lower.matches(".*(viagem|destino|percurso|trajeto).*")) sawTrip = true;
            if (lower.matches(".*(total|dist[aâ]ncia total|tempo total).*")) sawTotal = true;
        }
        if (fare <= 0 || kms.isEmpty() || minutes.isEmpty()) return null;
        // Quando há pickup e viagem legíveis, somamos os dois segmentos.
        // Quando há apenas um segmento, NÃO pressupomos que inclui embarque.
        boolean includesPickup = (sawPickup && sawTrip && kms.size() >= 2 && minutes.size() >= 2) || sawTotal;
        double km = 0, time = 0;
        if (includesPickup && !sawTotal && kms.size() == 2 && minutes.size() == 2) {
            km = kms.get(0) + kms.get(1);
            time = minutes.get(0) + minutes.get(1);
        } else if (kms.size() == 1 && minutes.size() == 1) {
            km = kms.get(0); time = minutes.get(0);
        } else {
            // Ambíguo: poderia misturar percurso, tarifa e distância total.
            return null;
        }
        if (fare > 2000 || km <= 0 || time <= 0) return null;
        return new OfferEngine.Offer(platform, fare, km, time, includesPickup);
    }

    private static double decimal(String v) {
        String s = v.replace(".", "").replace(',', '.');
        // Kilometragem "2.5" também é permitida.
        if (v.indexOf('.') >= 0 && v.indexOf(',') < 0) s = v;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return -1; }
    }
}
