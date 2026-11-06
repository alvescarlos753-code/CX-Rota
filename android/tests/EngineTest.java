import com.cxwebstudio.cxrota.OfferEngine;
import com.cxwebstudio.cxrota.OfferParser;

public class EngineTest {
    private static int checks;
    private static void that(boolean pass, String msg) {
        checks++;
        if (!pass) throw new AssertionError(msg);
    }
    public static void main(String[] args) {
        OfferEngine.Rules rules = new OfferEngine.Rules(1.8, 35, 0.25, true);
        OfferEngine.Offer a = OfferParser.parse("Uber", "R$ 14,50\n2 min (0,8 km) até o embarque\n12 min (4,4 km) viagem");
        that(a != null, "leitura uber");
        that(Math.abs(a.distanceKm - 5.2) < 1e-9, "soma dos km");
        that(a.minutes == 14, "soma dos minutos");
        that(OfferEngine.evaluate(a, rules).decision == OfferEngine.Decision.ACCEPT, "aceitar boa");
        OfferEngine.Offer b = OfferParser.parse("99", "R$ 5,00\n5 min 2 km até o embarque\n19 min 9 km viagem");
        that(b != null, "leitura 99");
        that(OfferEngine.evaluate(b, rules).decision == OfferEngine.Decision.DECLINE, "recusar ruim");
        OfferEngine.Offer c = OfferParser.parse("Uber", "R$ 12,00\n10 min\n5 km\nAceitar");
        that(c != null && !c.includesPickup, "segmento sem pickup");
        that(OfferEngine.evaluate(c, rules).decision == OfferEngine.Decision.REVIEW, "precisa conferir deslocamento");
        that(OfferParser.parse("99", "Promoção R$ 25,00\nbonificação de R$ 4,00") == null, "ignora promocao");
        that(OfferParser.parse("99", "R$ 15,00\n2 km\n3 km\n20 min") == null, "rejeita ambiguidade");
        that(OfferEngine.evaluate(new OfferEngine.Offer("99", 5, 10, 20, true), new OfferEngine.Rules(0.1, 1, 1, true)).decision == OfferEngine.Decision.DECLINE, "custo superior a tarifa");
        OfferEngine.Offer win = OfferParser.parse("Uber", "R$ 14,50\r2 min (0,8 km) até o embarque\r12 min (4,4 km) viagem");
        that(win != null && win.includesPickup, "linhas do Windows CR");
        that(OfferParser.parse("99", "R$ 10,00\n2 km\n6 min") == null, "não confunde painel com oferta");
        that(OfferParser.parse("Uber", "R$ 10,00\n3 km\n9 min\nAceitar") != null, "texto simples com aceitar");
        that(OfferParser.parse("99", "R$ 0,00\n3 km\n9 min\nAceitar") == null, "recusa tarifa zero");
        that(OfferParser.parse("99", "R$ 12,00\nAceitar") == null, "recusa campos faltantes");
        that(OfferEngine.evaluate(new OfferEngine.Offer("Uber", 20, 5, 20, true), rules).decision == OfferEngine.Decision.ACCEPT, "aceita boa oferta");
        that(OfferEngine.evaluate(new OfferEngine.Offer("Uber", 8, 8, 10, true), rules).decision == OfferEngine.Decision.REVIEW, "uma meta abaixo");
        that(OfferEngine.evaluate(new OfferEngine.Offer("Uber", 8, -1, 10, true), rules).decision == OfferEngine.Decision.INCOMPLETE, "distância inválida");
        System.out.println("OK: " + checks + " verificações de leitura e análise.");
    }
}
