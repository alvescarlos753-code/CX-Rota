package com.cxwebstudio.cxrota;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Dados locais. O app deliberadamente não solicita INTERNET. */
public final class SettingsStore {
    private static final String NAME = "cx_rota_private";
    private SettingsStore() {}
    public static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
    public static CostModel costModel(Context ctx) {
        SharedPreferences p=prefs(ctx);
        String ownership=p.getString("vehicle_ownership", "OWNED");
        String period=p.getString("rental_period", "WEEKLY");
        return new CostModel(
                CostModel.Ownership.valueOf(ownership), CostModel.Period.valueOf(period),
                number(p,"rental_fee",0), p.getInt("workdays_week",6),p.getInt("workdays_month",26),
                number(p,"fuel_price",0),number(p,"fuel_kml",0),number(p,"maint_km",0),
                number(p,"fixed_daily",0),number(p,"planned_km",150));
    }
    private static double number(SharedPreferences p, String key, double fallback) {
        return Double.longBitsToDouble(p.getLong(key,Double.doubleToLongBits(fallback)));
    }
    public static void saveCostModel(Context ctx, CostModel m) {
        m.validate();
        prefs(ctx).edit().putString("vehicle_ownership",m.ownership.name())
                .putString("rental_period",m.period.name())
                .putLong("rental_fee",Double.doubleToLongBits(m.rentalFee))
                .putInt("workdays_week",m.workDaysPerWeek)
                .putInt("workdays_month",m.workDaysPerMonth)
                .putLong("fuel_price",Double.doubleToLongBits(m.fuelPricePerLiter))
                .putLong("fuel_kml",Double.doubleToLongBits(m.kmPerLiter))
                .putLong("maint_km",Double.doubleToLongBits(m.maintenancePerKm))
                .putLong("fixed_daily",Double.doubleToLongBits(m.otherDaily))
                .putLong("planned_km",Double.doubleToLongBits(m.plannedDailyKm)).apply();
    }
    public static OfferEngine.Rules rules(Context ctx) {
        SharedPreferences p = prefs(ctx);
        CostModel m=costModel(ctx);
        boolean manual=p.getBoolean("cost_configured", false);
        return new OfferEngine.Rules(
                number(p,"min_km",1.80), number(p,"min_h",35.00),
                manual ? number(p,"cost_km",0) : m.allocatedPerKm(),
                manual || m.costsComplete());
    }
    public static void updateRules(Context ctx, double km, double hour, double cost, boolean costConfigured) {
        prefs(ctx).edit().putLong("min_km", Double.doubleToLongBits(km))
                .putLong("min_h", Double.doubleToLongBits(hour))
                .putLong("cost_km", Double.doubleToLongBits(cost))
                .putBoolean("cost_configured", costConfigured).apply();
    }
    public static boolean consent(Context ctx) { return prefs(ctx).getBoolean("consent", false); }
    public static boolean ocr(Context ctx) { return prefs(ctx).getBoolean("ocr", false); }
    public static boolean voice(Context ctx) { return prefs(ctx).getBoolean("voice", false); }
    public static boolean overlay(Context ctx) { return prefs(ctx).getBoolean("overlay", true); }
    public static void writeOffer(Context ctx, OfferEngine.Result r) {
        if (r == null || r.offer == null) return;
        try {
            SharedPreferences p = prefs(ctx);
            JSONArray old = new JSONArray(p.getString("history", "[]"));
            JSONArray fresh = new JSONArray();
            JSONObject entry = new JSONObject();
            entry.put("time", System.currentTimeMillis());
            entry.put("platform", r.offer.platform);
            entry.put("fare", r.offer.fare);
            entry.put("km", r.offer.distanceKm);
            entry.put("minutes", r.offer.minutes);
            entry.put("pickupIncluded", r.offer.includesPickup);
            entry.put("decision", r.decision.name());
            fresh.put(entry);
            for (int i = 0; i < old.length() && i < 99; i++) fresh.put(old.getJSONObject(i));
            p.edit().putString("history", fresh.toString()).apply();
        } catch (Exception ignored) { }
    }
    public static List<JSONObject> recent(Context ctx, int count) {
        List<JSONObject> items = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(ctx).getString("history", "[]"));
            for (int i = 0; i < Math.min(count, arr.length()); i++) items.add(arr.getJSONObject(i));
        } catch (Exception ignored) { }
        return items;
    }
    public static String csv(Context ctx) {
        StringBuilder b = new StringBuilder("data_hora;plataforma;oferta_reais;distancia_km;tempo_min;embarque_incluido;classificacao\n");
        for (JSONObject item : recent(ctx, 100)) {
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.forLanguageTag("pt-BR"))
                    .format(new java.util.Date(item.optLong("time")));
            b.append(date).append(';').append(item.optString("platform")).append(';')
                    .append(item.optDouble("fare")).append(';').append(item.optDouble("km"))
                    .append(';').append(item.optDouble("minutes")).append(';')
                    .append(item.optBoolean("pickupIncluded")).append(';')
                    .append(item.optString("decision")).append('\n');
        }
        return b.toString();
    }
}
