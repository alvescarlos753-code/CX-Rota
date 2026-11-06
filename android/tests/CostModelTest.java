import com.cxwebstudio.cxrota.CostModel;
import com.cxwebstudio.cxrota.OfferEngine;
public class CostModelTest {
    static int checks = 0;
    static void check(boolean condition, String message) {
        checks++;
        if(!condition) throw new AssertionError(message);
    }
    static void near(double a,double b,String msg){check(Math.abs(a-b)<1e-7,msg+" ("+a+" != "+b+")");}
    public static void main(String[] args) {
        CostModel rental = new CostModel(CostModel.Ownership.RENTED,CostModel.Period.WEEKLY,
                420,6,26,6.30,35,0.12,8,150);
        near(rental.rentalDaily(),70,"aluguel semanal por dias de trabalho");
        near(rental.fuelPerKm(),0.18,"combustível por km");
        near(rental.variablePerKm(),0.30,"custo variável");
        near(rental.fixedPerDay(),78,"fixo diário");
        near(rental.allocatedPerKm(),0.82,"custo/km rateado");
        near(rental.dailyCost(100,0),108,"aluguel não descontado duas vezes");
        near(rental.netDay(200,100,12),80,"líquido econômico com incidentais");
        near(rental.breakEvenGrossPerDay(),123,"ponto de equilíbrio diário planejado");
        check(rental.costsComplete(),"custos configurados");
        CostModel own = new CostModel(CostModel.Ownership.OWNED,CostModel.Period.MONTHLY,
                1500,6,22,6,30,0.15,10,100);
        near(own.rentalDaily(),0,"próprio não cobra aluguel");
        near(own.dailyCost(100,5),50,"custos moto própria");
        CostModel monthly=new CostModel(CostModel.Ownership.RENTED,CostModel.Period.MONTHLY,
                1300,5,26,6,30,0,0,100);
        near(monthly.rentalDaily(),50,"aluguel mensal rateado");
        CostModel daily=new CostModel(CostModel.Ownership.RENTED,CostModel.Period.DAILY,
                45,5,22,6,30,0,0,100);
        near(daily.rentalDaily(),45,"aluguel diário");
        CostModel missing=new CostModel(CostModel.Ownership.RENTED,CostModel.Period.WEEKLY,
                420,6,26,0,0,0,0,100);
        check(!missing.costsComplete(),"sem dados combustível não declarar lucro completo");
        try { new CostModel(CostModel.Ownership.RENTED, CostModel.Period.WEEKLY,
                -1,6,26,6,30,0,0,100); throw new AssertionError("aceitou aluguel negativo"); }
        catch(IllegalArgumentException expected){checks++;}
        OfferEngine.Result good = OfferEngine.evaluate(new OfferEngine.Offer("Uber Moto",14.5,5.2,14,true),
                new OfferEngine.Rules(1.8,35,rental.allocatedPerKm(),true));
        check(good.decision == OfferEngine.Decision.ACCEPT,"corrida boa alugada");
        near(good.estimatedNet,14.5-5.2*0.82,"custo rateado na oferta");
        System.out.println("OK: "+checks+" verificações de custo próprio/alugado.");
    }
}
