package com.cxwebstudio.cxrota;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = 0xFF0B1420;
    private static final int PANEL = 0xFF152234;
    private static final int MUTED = 0xFF9DADC2;
    private static final int MINT = 0xFF37E1B0;
    private static final int WHITE = 0xFFF2F7FC;
    private static final int AMBER = 0xFFFFCD78;
    private static final int PINK = 0xFFFF7C88;
    private LinearLayout base;
    private TextView stateText;
    private LinearLayout history;
    private EditText kmStart, kmEnd, uberMoney, ninetyMoney, gasExpense;
    private EditText minKm, minHour, costKm;
    private EditText offerFare, offerKm, offerTime;
    private Spinner vehicleOwnership, rentalPeriod;
    private EditText rentalFee, daysWeek, daysMonth, fuelPrice, fuelKmLiter, maintKm, fixedDaily, plannedDailyKm, extras;

    private Spinner platform;
    private CheckBox pickupKnown;
    private TextView resultText, dailyResult;

    private int dp(float x) { return Math.round(x * getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        render();
    }
    @Override protected void onResume() {
        super.onResume();
        if (stateText != null) stateText.setText(status());
        refreshHistory();
    }
    private GradientDrawable shape(int bg, int radius, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(bg); g.setCornerRadius(dp(radius));
        if (stroke != 0) g.setStroke(dp(1), stroke);
        return g;
    }
    private LinearLayout vertical() { LinearLayout x = new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); return x; }
    private LinearLayout horizontal() { LinearLayout x = new LinearLayout(this); x.setOrientation(LinearLayout.HORIZONTAL); x.setGravity(Gravity.CENTER_VERTICAL); return x; }
    private TextView text(String s, int sp, int color, boolean strong) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(color);
        v.setFontFeatureSettings("kern");
        if (strong) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private void padBottom(LinearLayout parent, int height) {
        View space = new View(this); parent.addView(space, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private LinearLayout card(String eyebrow, String heading) {
        LinearLayout c = vertical();
        c.setPadding(dp(17), dp(18), dp(17), dp(18));
        c.setBackground(shape(PANEL, 20, 0xFF23384A));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.bottomMargin = dp(14);
        base.addView(c, cp);
        TextView e = text(eyebrow.toUpperCase(Locale.ROOT), 11, MINT, true);
        c.addView(e);
        TextView title = text(heading, 20, WHITE, true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(5); p.bottomMargin = dp(14);
        c.addView(title, p);
        return c;
    }
    private void note(LinearLayout c, String s) {
        TextView t = text(s, 12, MUTED, false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.topMargin = dp(9);
        c.addView(t, p);
    }
    private EditText field(LinearLayout c, String label, String def, boolean decimal) {
        TextView l = text(label, 12, MUTED, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.topMargin = dp(10);
        c.addView(l, lp);
        EditText inp = new EditText(this);
        inp.setSingleLine(true);
        inp.setText(def);
        inp.setSelectAllOnFocus(true);
        inp.setTextSize(17);
        inp.setTextColor(WHITE);
        inp.setHintTextColor(MUTED);
        inp.setPadding(dp(12), dp(9), dp(12), dp(9));
        inp.setBackground(shape(BG, 10, 0xFF344659));
        inp.setInputType(decimal ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                : InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, dp(47));
        ip.topMargin = dp(4); c.addView(inp, ip);
        return inp;
    }
    private Button button(LinearLayout parent, String title, boolean primary, View.OnClickListener cb) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(title);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? BG : WHITE);
        b.setBackground(shape(primary ? MINT : 0xFF26384C, 12, 0));
        b.setOnClickListener(cb);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(49));
        p.topMargin = dp(12); parent.addView(b, p);
        return b;
    }
    private String value(android.content.SharedPreferences p, String key, String def) { return p.getString(key,def); }
    private double number(EditText e) {
        try { return Double.parseDouble(e.getText().toString().trim().replace(',','.')); }
        catch (Exception ignored) { throw new IllegalArgumentException("Confira os campos numéricos."); }
    }
    private String status() {
        boolean permit = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) != null
                && Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                .contains(new ComponentName(this, OfferReaderService.class).flattenToString());
        if (!SettingsStore.consent(this)) return "●  Pendente: autorização da leitura";
        if (!permit) return "●  Pendente: ativar Acessibilidade do CX Rota";
        if (!Settings.canDrawOverlays(this)) return "●  Leitura autorizada, mas o cartão flutuante está desligado";
        return "●  Leitura habilitada · Uber Moto e 99 Moto";
    }
    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        base = vertical(); base.setPadding(dp(17), dp(18), dp(17), dp(40));
        scroll.addView(base);
        setContentView(scroll);
        LinearLayout logo = horizontal();
        TextView mark = text("CX", 25, MINT, true);
        logo.addView(mark);
        TextView name = text("  ROTA", 25, WHITE, true);
        logo.addView(name);
        base.addView(logo);
        TextView sub = text("Seu copiloto inteligente de corridas", 13, MUTED, false);
        LinearLayout.LayoutParams s = new LinearLayout.LayoutParams(-1,-2); s.bottomMargin = dp(24);
        base.addView(sub, s);

        LinearLayout live = card("Status da jornada", "Pronto para analisar ofertas");
        stateText = text(status(), 13, AMBER, true);
        live.addView(stateText);
        note(live, "Uber Moto + 99 Moto • A decisão final de aceitar ou recusar é sempre sua.");
        button(live, "1  •  Autorizar leitura das ofertas", true, v -> askConsent());
        button(live, "2  •  Permitir cartão sobre os aplicativos", false, v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+getPackageName()));
            startActivity(i);
        });
        note(live, "Leitura de texto acessível e, com sua autorização, reconhecimento visual OCR no aparelho. Ainda requer validação com ofertas reais.");
        CheckBox ocr = option("OCR local da tela (opcional; Android 11+)", SettingsStore.ocr(this));
        ocr.setOnCheckedChangeListener((b, checked) -> {
            if (!checked) { SettingsStore.prefs(this).edit().putBoolean("ocr", false).apply(); return; }
            new AlertDialog.Builder(this)
                    .setTitle("Autorizar leitura visual com OCR?")
                    .setMessage("Quando o texto de uma oferta não estiver disponível pelos recursos de acessibilidade, " +
                            "o CX Rota poderá capturar imagens temporárias da tela do Uber Driver ou 99 Motorista " +
                            "para reconhecer números por OCR no próprio aparelho. As imagens são descartadas após o processamento. " +
                            "O serviço pode visualizar também informações sensíveis mostradas nessas telas, " +
                            "mas não armazena imagens, nomes, telefones ou endereços. Essa opção pode ser desligada a qualquer momento.")
                    .setPositiveButton("Autorizar OCR", (d,w) -> SettingsStore.prefs(this).edit().putBoolean("ocr", true).apply())
                    .setNegativeButton("Não permitir", (d,w) -> ocr.setChecked(false)).show();
        });
        live.addView(ocr);
        CheckBox voice = option("Avisos por voz", SettingsStore.voice(this));
        voice.setOnCheckedChangeListener((b,checked)-> {
            SettingsStore.prefs(this).edit().putBoolean("voice", checked).apply();
            Toast.makeText(this, "Para aplicar a voz, reinicie o serviço de acessibilidade.", Toast.LENGTH_LONG).show();
        });
        live.addView(voice);
        CheckBox overlay = option("Exibir cartão flutuante", SettingsStore.overlay(this));
        overlay.setOnCheckedChangeListener((b,checked)->SettingsStore.prefs(this).edit().putBoolean("overlay",checked).apply());
        live.addView(overlay);

        LinearLayout vehicle = card("Custos do veículo", "Moto própria ou alugada");
        CostModel configured = SettingsStore.costModel(this);
        note(vehicle, "Informe seus custos reais. Se a moto for alugada, o valor será rateado pelos dias em que você trabalha, sem cobrar o aluguel a cada corrida.");
        vehicle.addView(text("Situação da moto",12,MUTED,true));
        vehicleOwnership = new Spinner(this);
        vehicleOwnership.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Moto própria", "Moto alugada"}));
        vehicleOwnership.setSelection(configured.ownership == CostModel.Ownership.RENTED ? 1 : 0);
        vehicle.addView(vehicleOwnership);
        vehicle.addView(text("Frequência da cobrança do aluguel",12,MUTED,true));
        rentalPeriod = new Spinner(this);
        rentalPeriod.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Diário", "Semanal", "Mensal"}));
        rentalPeriod.setSelection(configured.period == CostModel.Period.DAILY ? 0 :
                configured.period == CostModel.Period.MONTHLY ? 2 : 1);
        vehicle.addView(rentalPeriod);
        rentalFee = field(vehicle,"Aluguel cobrado por período • R$",
                configured.ownership == CostModel.Ownership.RENTED ? OfferEngine.number(configured.rentalFee) : "0",true);
        daysWeek = field(vehicle,"Dias de trabalho por semana (1–7)",""+configured.workDaysPerWeek,false);
        daysMonth = field(vehicle,"Dias de trabalho por mês (1–31)",""+configured.workDaysPerMonth,false);
        fuelPrice = field(vehicle,"Preço do combustível • R$/litro",
                configured.fuelPricePerLiter>0?OfferEngine.number(configured.fuelPricePerLiter):"",true);
        fuelKmLiter = field(vehicle,"Consumo médio da moto • km/litro",
                configured.kmPerLiter>0?OfferEngine.number(configured.kmPerLiter):"",true);
        maintKm = field(vehicle,"Reserva para manutenção • R$/km",OfferEngine.number(configured.maintenancePerKm),true);
        fixedDaily = field(vehicle,"Outros custos fixos • R$/dia trabalhado",OfferEngine.number(configured.otherDaily),true);
        plannedDailyKm = field(vehicle,"Quantos km planeja rodar por dia?",OfferEngine.number(configured.plannedDailyKm),true);
        button(vehicle,"Salvar os custos da moto",true,v -> {
            try {
                CostModel m = new CostModel(vehicleOwnership.getSelectedItemPosition()==1?
                        CostModel.Ownership.RENTED:CostModel.Ownership.OWNED,
                        rentalPeriod.getSelectedItemPosition()==0?CostModel.Period.DAILY:
                        rentalPeriod.getSelectedItemPosition()==2?CostModel.Period.MONTHLY:CostModel.Period.WEEKLY,
                        number(rentalFee),(int)number(daysWeek),(int)number(daysMonth),
                        fuelPrice.getText().toString().isBlank()?0:number(fuelPrice),
                        fuelKmLiter.getText().toString().isBlank()?0:number(fuelKmLiter),
                        number(maintKm),number(fixedDaily),number(plannedDailyKm));
                SettingsStore.saveCostModel(this,m);
                Toast.makeText(this,"Custo/km com rateio: "+OfferEngine.money(m.allocatedPerKm()),Toast.LENGTH_LONG).show();
                if (!m.costsComplete()) warn("Complete aluguel e combustível para calcular lucro confiável.");
            } catch(Exception ex){warn(ex.getMessage());}
        });
        note(vehicle,"Atenção: combustível e manutenção são custos por km. O aluguel só entra uma vez por dia. Se faltar consumo ou preço, o lucro não é considerado completo.");

        LinearLayout settings = card("Metas configuráveis", "Minha regra de rentabilidade");
        OfferEngine.Rules rules = SettingsStore.rules(this);
        minKm = field(settings, "Mínimo desejado • R$/km", OfferEngine.number(rules.minPerKm), true);
        minHour = field(settings, "Mínimo desejado • R$/hora", OfferEngine.number(rules.minPerHour), true);
        costKm = field(settings, "Custo total estimado da moto • R$/km", rules.costConfigured ? OfferEngine.number(rules.costPerKm) : "", true);
        note(settings, "As metas iniciais são apenas exemplos. Deixe o custo/km manual em branco para usar automaticamente combustível, manutenção e aluguel rateado cadastrados acima. Se preencher, ele substitui o cálculo automático.");
        button(settings, "Salvar minhas metas", true, v -> {
            try {
                double km=number(minKm), hr=number(minHour);
                boolean costConfigured = !costKm.getText().toString().isBlank();
                double cost = costConfigured ? number(costKm) : 0;
                if (!Double.isFinite(km) || !Double.isFinite(hr) || !Double.isFinite(cost)
                        || km<=0 || hr<=0 || cost<0)
                    throw new IllegalArgumentException("Use metas positivas e custo não negativo.");
                SettingsStore.updateRules(this, km, hr, cost, costConfigured);
                Toast.makeText(this, "Metas salvas no aparelho", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) { warn(ex.getMessage()); }
        });

        LinearLayout test = card("Teste sem corridas reais", "Simular uma oferta");
        platform = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Uber Moto", "99 Moto"});
        platform.setAdapter(adapter);
        test.addView(platform);
        offerFare = field(test, "Valor oferecido • R$", "14,50", true);
        offerKm = field(test, "Distância total • km", "5,2", true);
        offerTime = field(test, "Tempo total • minutos", "14", true);
        pickupKnown = option("Incluí deslocamento até o embarque", true);
        test.addView(pickupKnown);
        resultText = text("", 16, WHITE, true);
        button(test, "Avaliar agora", true, v -> {
            try {
                OfferEngine.Offer input = new OfferEngine.Offer(platform.getSelectedItem().toString(),
                        number(offerFare), number(offerKm), number(offerTime), pickupKnown.isChecked());
                OfferEngine.Result result = OfferEngine.evaluate(input, SettingsStore.rules(this));
                String name = result.decision == OfferEngine.Decision.ACCEPT ? "ACEITAR" :
                        result.decision == OfferEngine.Decision.DECLINE ? "RECUSAR" : "CONFERIR";
                resultText.setText(name + "\n" + OfferEngine.money(result.perKm) + "/km  •  "
                        + OfferEngine.money(result.perHour) + "/hora\n" + result.reason +
                        (result.costConfigured ? "\nSobra estimada: " + OfferEngine.money(result.estimatedNet) : "\nConfigure o custo/km para estimar a sobra."));
                resultText.setTextColor(result.decision == OfferEngine.Decision.ACCEPT ? MINT :
                        result.decision == OfferEngine.Decision.DECLINE ? PINK : AMBER);
                SettingsStore.writeOffer(this, result);
                refreshHistory();
            } catch (Exception ex) { warn(ex.getMessage()); }
        });
        test.addView(resultText);
        note(test, "O simulador não representa uma chamada recebida. Ofertas analisadas não são corridas realizadas nem ganhos confirmados.");

        LinearLayout session = card("Controle de quilometragem", "Resumo da jornada");
        android.content.SharedPreferences p = SettingsStore.prefs(this);
        note(session, "Início informado: 06/11/2026 às 06h58. KM inicial registrado: 35.179.");
        kmStart = field(session, "Odômetro no início", value(p,"session_start_km","35179"), false);
        kmEnd = field(session, "Odômetro final (preencher no encerramento)", value(p,"session_end_km",""), false);
        uberMoney = field(session, "Ganhos confirmados na Uber • R$", value(p,"session_uber","0"), true);
        ninetyMoney = field(session, "Ganhos confirmados na 99 • R$", value(p,"session_99","0"), true);
        gasExpense = field(session, "Combustível pago nesta jornada • R$ (informativo)", value(p,"session_gas",""), true);
        extras = field(session, "Pedágio, estacionamento e imprevistos • R$",value(p,"session_extras","0"),true);
        dailyResult = text("Preencha os dados ao encerrar para calcular a jornada.", 14, WHITE, false);
        button(session, "Salvar e calcular minha jornada", true, v -> saveSession());
        session.addView(dailyResult);
        note(session, "Lucro econômico: desconto consumo de combustível, manutenção e aluguel rateado 1x/dia. O abastecimento pago é informado à parte para não descontar combustível duas vezes.");

        LinearLayout log = card("Histórico local", "Últimas ofertas analisadas");
        history = vertical(); log.addView(history);
        button(log, "Compartilhar histórico CSV", false, v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_TEXT, SettingsStore.csv(this));
            share.putExtra(Intent.EXTRA_SUBJECT, "CX Rota - ofertas analisadas");
            startActivity(Intent.createChooser(share,"Exportar dados"));
        });
        note(log, "O histórico salva apenas valor, distância, tempo, classificação e data. Sem endereços ou informações de passageiros.");
        refreshHistory();
        TextView footer = text("CX WEB STUDIO   •   CX ROTA ALFA 0.2.0\nDados locais. Sem conexão com contas Uber ou 99.",11,MUTED,false);
        footer.setGravity(Gravity.CENTER); base.addView(footer);
    }
    private CheckBox option(String title, boolean value) {
        CheckBox b = new CheckBox(this);
        b.setText(title); b.setTextSize(13); b.setTextColor(WHITE); b.setChecked(value);
        b.setButtonTintList(android.content.res.ColorStateList.valueOf(MINT));
        return b;
    }
    private void askConsent() {
        new AlertDialog.Builder(this)
                .setTitle("Autorizar leitura de ofertas?")
                .setMessage("O CX Rota acessará os textos visíveis apenas nas telas do Uber Driver e 99 Motorista enquanto o serviço estiver ativado, para extrair valor, km e tempo e sugerir se a oferta atende às suas metas. " +
                        "O processamento é local e não há transmissão para servidores. Caso você autorize OCR separadamente, " +
                        "o serviço também poderá capturar imagens temporárias das telas da Uber e da 99. Não armazenamos imagens, nomes, contatos ou rotas de passageiros e não clicamos em aceitar/recusar. " +
                        "Você pode desativar a permissão a qualquer momento em Configurações > Acessibilidade.")
                .setNegativeButton("Agora não", null)
                .setPositiveButton("Concordo e quero ativar", (d,w) -> {
                    SettingsStore.prefs(this).edit().putBoolean("consent",true).apply();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }).show();
    }
    private void warn(String message) {
        Toast.makeText(this, message == null ? "Confira os dados" : message, Toast.LENGTH_LONG).show();
    }
    private void saveSession() {
        try {
            double start=number(kmStart);
            double end=kmEnd.getText().toString().isBlank() ? 0 : number(kmEnd);
            double uber=number(uberMoney), ninety=number(ninetyMoney);
            double fuel=gasExpense.getText().toString().isBlank() ? 0 : number(gasExpense);
            double incidental=number(extras);
            if (start<0 || end<0 || uber<0 || ninety<0 || fuel<0 || incidental<0 || (end!=0 && end<start))
                throw new IllegalArgumentException("Confira os valores do odômetro e dos gastos.");
            SettingsStore.prefs(this).edit()
                    .putString("session_start_km", kmStart.getText().toString())
                    .putString("session_end_km", kmEnd.getText().toString())
                    .putString("session_uber", uberMoney.getText().toString())
                    .putString("session_99", ninetyMoney.getText().toString())
                    .putString("session_gas", gasExpense.getText().toString())
                    .putString("session_extras", extras.getText().toString()).apply();
            double gross=uber+ninety;
            if (end==0) {
                dailyResult.setText("Ganhos confirmados: " + OfferEngine.money(gross) +
                        "\nInforme o odômetro final para calcular km e lucro.");
                return;
            }
            double km=end-start;
            OfferEngine.Rules r=SettingsStore.rules(this);
            CostModel m=SettingsStore.costModel(this);
            boolean manual=SettingsStore.prefs(this).getBoolean("cost_configured",false);
            double cost=manual ? r.costPerKm*km + incidental : m.dailyCost(km,incidental);
            // Se o preço e o consumo ainda não foram configurados, usa o pagamento como aproximação.
            if (!manual && !m.fuelConfigured()) cost+=fuel;
            double net=gross-cost;
            dailyResult.setText("Rodados: " + OfferEngine.number(km) + " km\nFaturamento: "
                    + OfferEngine.money(gross) + "\nAluguel rateado: " + OfferEngine.money(manual?0:m.rentalDaily())
                    + "\nCusto econômico estimado: " + OfferEngine.money(cost)
                    + "\nCombustível pago (não somado 2x): " + OfferEngine.money(fuel)
                    + "\nResultado estimado: " + OfferEngine.money(net)
                    + (!r.costConfigured?"\n⚠ Complete os custos para estimativa confiável.":""));
            dailyResult.setTextColor(net>=0 ? MINT : PINK);
            warn("Jornada salva no aparelho");
        } catch(Exception ex) { warn(ex.getMessage()); }
    }
    private void refreshHistory() {
        if (history == null) return;
        history.removeAllViews();
        List<JSONObject> entries = SettingsStore.recent(this,8);
        if (entries.isEmpty()) { history.addView(text("Nenhuma oferta analisada ainda.", 13, MUTED, false)); return; }
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR"));
        for (JSONObject j : entries) {
            String kind=j.optString("decision");
            int color = kind.equals("ACCEPT") ? MINT : kind.equals("DECLINE") ? PINK : AMBER;
            String label = kind.equals("ACCEPT") ? "ACEITAR" : kind.equals("DECLINE") ? "RECUSAR" : "CONFERIR";
            LinearLayout item=vertical(); item.setPadding(dp(10),dp(10),dp(10),dp(11));
            item.setBackground(shape(BG, 12, 0xFF334456));
            TextView line = text(j.optString("platform") + "  •  " + fmt.format(new Date(j.optLong("time"))),12,MUTED,true);
            item.addView(line);
            item.addView(text(OfferEngine.money(j.optDouble("fare")) + "   " + label,16,color,true));
            item.addView(text(OfferEngine.number(j.optDouble("km")) + " km • " +
                    OfferEngine.number(j.optDouble("minutes")) + " min",12,MUTED,false));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(8);
            history.addView(item,lp);
        }
    }
}
