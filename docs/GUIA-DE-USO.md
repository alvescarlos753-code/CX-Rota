# Guia de uso do CX Rota — motoristas

O CX Rota ajuda a organizar **ganhos reais, custos de moto própria ou alugada e rentabilidade de ofertas** da Uber Moto e 99 Moto. Atualmente, a versão web permite **análise manual**, enquanto a leitura automática Android ainda é **experimental**.

> **Segurança:** configure ou consulte o aplicativo **somente com a moto parada e em local seguro**. O CX Rota recomenda; ele não aceita/recusa corridas por você.

## Primeira configuração

1. Abra o **Painel web (`web/app.html`)**, ou um APK de teste compilado e instalado no Android.
2. Cadastre a **moto própria ou alugada**. Informe o tipo de aluguel e a frequência do contrato, quando aplicável.
3. Adicione **preço do combustível (R$/litro)** e **consumo médio (km/l)** da sua moto. Use números reais, não médias genéricas.
4. Configure uma reserva de **manutenção por km**, despesas fixas e a meta de km por dia.
5. Escolha o **mínimo por km** e **mínimo por hora** que deseja alcançar. Salve as configurações.
6. Confira se os custos exibidos fazem sentido antes de avaliar ofertas.

## Analisar uma oferta manualmente (painel web)

1. Abra **Ofertas** e selecione Uber Moto ou 99 Moto.
2. Preencha **valor oferecido (R$)**, **distância total (km)** e **tempo total (minutos)**.
3. Marque **inclui o trajeto até o embarque** apenas quando os valores realmente contemplarem esse deslocamento.
4. Toque em **Calcular rentabilidade**.
5. Leia a recomendação e os indicadores. **CONFERIR** significa que faltam dados essenciais ou há incerteza.
6. A decisão final de aceitar ou recusar é sua. Uma oferta avaliada **não é um ganho realizado**.

## Encerrar a jornada e calcular resultados

1. Abra **Jornada** (web) ou **Resumo da jornada** (Android).
2. Informe a data, o odômetro inicial e o final. O histórico inicial pessoal da preparação foi **35.179 km, em 06/11/2026 às 06h58**, mas cada novo dia precisa de odômetro próprio.
3. Informe separadamente os **ganhos efetivamente recebidos** na Uber e na 99.
4. Complete horas trabalhadas, **valor pago em combustível** (para controle de caixa) e despesas extras do dia.
5. Salve o fechamento e verifique o resultado econômico estimado.
6. Revise os cálculos: combustível consumido é estimado por km/l; abastecimento pago **não deve ser descontado novamente** desse mesmo resultado.

## Moto alugada: como preencher

- **Diário:** coloque o valor contratado por dia.
- **Semanal:** informe o aluguel semanal e a previsão de dias trabalhados por semana.
- **Mensal:** informe o aluguel mensal e a previsão de dias trabalhados no mês.

O painel usa um **rateio por dia trabalhado para estimar rentabilidade**. Esse rateio não altera o valor nem os vencimentos devidos à locadora — aluguel pode continuar vencendo mesmo se você não trabalhar.

## Histórico e backup

No menu **Histórico** da versão web, exporte **CSV** de jornadas e ofertas. Use **Baixar backup JSON** periodicamente. Para recuperar, utilize **Restaurar backup JSON**. Os dados ficam no navegador atual; limpar armazenamento ou mudar de domínio pode apagar o acesso aos registros, se você não tiver um backup.

No Android alfa, o histórico de ofertas pode ser compartilhado em **CSV**. Não há sincronização automática entre Android e web.

## Leitura automática Android — apenas testes

A leitura depende de **autorização expressa de Acessibilidade** e, se ativado, **OCR local opcional** e permissão para cartão flutuante. Nem todas as telas disponibilizam dados legíveis. Os recursos **ainda não foram validados em chamadas reais** da Uber/99. Para testes, autorize apenas se entender o acesso e desative nas configurações do Android quando não precisar.

Não existem integração oficial de conta Uber/99 nem automação de botões de aceitar/recusar. Nunca use recomendações incompletas como certeza de lucro.

## Entendendo os indicadores

- **R$/km:** valor dividido pelos quilômetros considerados na oferta.
- **R$/hora:** valor dividido pelo tempo estimado em horas.
- **Custo por km:** combustível + manutenção + custos rateados quando configurados.
- **Resultado econômico:** faturamento efetivo menos custos estimados e despesas da jornada.

**Nenhum resultado é garantido:** trânsito, espera, deslocamento não informado, cancelamentos, desgaste e riscos podem alterar o lucro real.

## Dúvidas rápidas

**O painel web lê a tela da Uber/99?** Não. Nele o preenchimento é manual.

**Posso pagar a assinatura Pro?** Não: os preços da vitrine são propostas, sem sistema de cobrança ativo.

**Preciso de internet?** A versão web deve ser servida inicialmente por navegador/servidor; uma PWA instalada pode manter arquivos em cache conforme suporte e configuração do navegador. O Android alfa é projetado para processamento local, mas depende de validação e instalação.

**É seguro publicar meu histórico no GitHub?** Não. O GitHub deste projeto é público; não coloque dados pessoais, históricos reais ou chaves nele.

Veja [README principal](../README.md) para compilação, testes e colaboração.
