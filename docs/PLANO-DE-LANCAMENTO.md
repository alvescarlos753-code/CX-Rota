# CX Rota · Plano de lançamento e monetização

**Projeto:** CX Web Studio · 06/11/2026 · versão de produto **beta / pré-comercial**.

## Proposta

O CX Rota é um copiloto financeiro para motoristas de Uber Moto e 99 Moto, com foco no resultado **líquido estimado** por jornada e por oferta, sem depender do veículo ser próprio ou alugado. Mensagem central: **“Seu trabalho. Seus números.”**

## Segmentos

- Motorista com moto própria que quer entender combustível, manutenção e margem.
- Motorista com veículo alugado que precisa pagar diária/semanal/mensal e descobrir o ponto de equilíbrio.
- Profissional que alterna Uber e 99 e quer saber o resultado consolidado.
- Futuro: entregadores e outros prestadores de mobilidade **apenas após validar integrações e capacidade de suporte**.

## Modelo comercial proposto — ainda sem pagamentos ativos

| Plano | Preço de teste | Escopo |
| --- | --- | --- |
| Essencial | grátis | cadastro de veículo, registro de jornadas, análise manual básica, backup local |
| Pro Mensal | R$ 14,90/mês | leitura de ofertas e voz **somente quando tecnicamente validados**, histórico avançado e indicadores |
| Pro Anual | R$ 129,90/ano | benefícios do Pro com desconto anual |

**Hipótese a validar:** sete dias de acesso Pro gratuitos. Não implementar cobrança automática antes do consentimento, criação de conta segura, verificação de compra e opção de cancelamento.

**Concorrência:** consulta de 06/11/2026 encontrou o GigU a R$ 12,90/mês, com sete dias grátis, em https://www.gigu.app/br . O diferencial proposto é dar clareza sobre custo real e locação, mas isso precisa ser testado com usuários antes de ser tratado como vantagem competitiva comprovada.

## Receita, não lucro

Exemplo com R$ 14,90 por assinante/mês, somente assinaturas mensais ativas:

| Assinantes ativos | Bruto mensal | Bruto anual, se estável |
| ---: | ---: | ---: |
| 100 | R$ 1.490 | R$ 17.880 |
| 500 | R$ 7.450 | R$ 89.400 |
| 1.000 | R$ 14.900 | R$ 178.800 |
| 5.000 | R$ 74.500 | R$ 894.000 |

**Não é previsão nem garantia.** Receita líquida depende de impostos, taxas da loja/processador, hospedagem, suporte, reembolso, fraude, aquisição de clientes, churn e inadimplência. Não misturar aquisição total com assinantes pagantes.

## Requisitos de qualidade antes da primeira cobrança

1. **Android real:** APK/AAB compilado, assinado e testado em aparelhos populares (incluindo Android 16), com análise de permissão, consumo de bateria e comportamento em segundo plano.
2. **Leitura de ofertas:** testes reais e autorizados em Uber Driver e 99 Motorista, sob diferentes resoluções, acessibilidade, tema e versões; classificar como **CONFERIR** quando dados estiverem incompletos. Nenhuma promessa de funcionamento universal.
3. **Segurança:** consentimento explícito para os dados visualizados, minimização de coleta, não armazenar telas ou nomes/endereço de passageiros sem necessidade/consentimento, política de privacidade e direito de apagar dados.
4. **Decisão humana:** recomendação é consultiva; o app não toca automaticamente no botão de aceitar/recusar ofertas.
5. **Cobrança:** autenticação, backend, entitlements por usuário, assinatura pela plataforma compatível, verificação servidor, restauração de compras, suspensão/cancelamento, antifraude e recibos.
6. **Suporte:** central de ajuda, contato funcional, instruções de desativação, termos, política de privacidade, teste beta e canal de bugs.
7. **Observabilidade:** erros, taxa de reconhecimento de oferta, ANR/crashes, retenção, sessões, churn e eventos sem dados pessoais sensíveis.
8. **Teste em piloto fechado:** 20–50 motoristas voluntários com motos próprias e alugadas, sem cobrança, com consentimento para feedback.

## Política relevante

- Google Play exige declaração destacada e consentimento para apps não assistivos que utilizam AccessibilityService, além de formulário de declaração; veja https://support.google.com/googleplay/android-developer/answer/10964491?hl=pt-BR
- Regras de taxa de serviço e de faturamento variam por região/estrutura; consulte a política atual antes da configuração de assinatura: https://support.google.com/googleplay/android-developer/answer/112622?hl=pt-BR
- Use permissão somente quando indispensável. Não declare `isAccessibilityTool=true` se o app não é uma ferramenta para acessibilidade de pessoas com deficiência.
- Além de Google Play, validar termos de uso e atualizações da Uber e 99; não afirmar aprovação ou parceria.

## Plano de execução

**Fase 1 — produto confiável:** compilar e instalar APK, testar heurísticas de leitura, segurança e cálculos, resolver bugs de usabilidade e casos incompletos. Não cobrar.

**Fase 2 — beta real:** 20–50 pilotos, comparar cálculo com valores reais, medir reconhecimento e falhas, fechar UX e política de dados.

**Fase 3 — monetização:** backend, acesso por conta, pagamentos, assinatura e cancelamento, funcionalidades gratuitas que ajudam a aquisição, publicação depois de aprovação.

**Fase 4 — escala:** atendimento, análises agregadas e anônimas, programa de indicações, experimentos de preços e retenção, expansão após evidências.

## Métricas que importam

- **Reconhecimento válido:** ofertas lidas corretamente / ofertas verificadas manualmente.
- **Taxa de erro:** falsos positivos de aceitar ou recusar devem ser monitorados com prioridade alta.
- **Retenção D7/D30:** motoristas que continuam usando após 7/30 dias.
- **Conversão beta → pago:** pagos / beta elegíveis; não é igual a downloads.
- **Churn mensal:** assinantes perdidos / assinantes no início do mês.
- **CAC e LTV:** custo por assinante adquirido e valor gerado ao longo da permanência.
- **Ticket de suporte por 100 usuários:** informa capacidade operacional.

## Entrega nesta versão

- Apresentação comercial responsiva e preços explicitamente rotulados como propostos.
- Aplicativo web de gestão de jornada e análise manual com visual refinado, mantendo o modelo de custos já testado.
- PWA preparada para instalação em hospedagem HTTPS.
- Arquivo HTML offline de uso local (sem leitura automática).

**Pendências explícitas:** APK/AAB compilado, validação em Uber/99 reais, backend multiusuário, faturamento, cobrança e publicação comercial. Não comercializar funções não testadas.
