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
