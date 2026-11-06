# CX Rota — copiloto para Uber Moto + 99 Moto

**Versão 0.2.0 alfa / projeto Android nativo Java.**

Aplicativo original da CX Web Studio. Analisa ofertas exibidas em Uber Driver e 99 Motorista **por texto acessível ou OCR local autorizado**, e mostra uma indicação de aceitar, conferir ou recusar com base em limites ajustáveis. **Não aceita nem recusa corridas automaticamente.**

## Novidades 0.2.0 — veículo próprio e alugado

- Moto própria ou alugada, tarifa de aluguel diária, semanal ou mensal.
- Rateio semanal/mensal do aluguel por dias planejados de trabalho.
- Preço do combustível e autonomia real (km/l) para cálculo do custo energético por km.
- Reserva de manutenção por km, despesas fixas por dia, meta de quilometragem diária.
- Comparação de ofertas usando custo por km **incluindo rateio do aluguel**, quando todos os dados estão presentes; permite custo manual alternativo.
- Resumo da jornada deduz custos variáveis por km, aluguel apenas uma vez por dia e despesas incidentais; evita dupla dedução de combustível.
- Testes de regressão Java: 19 cenários de ofertas + 17 verificações de custos próprios/alugados.

**Limite:** ainda não existe APK assinado/testado. A versão web é uma ferramenta manual e não lê Uber/99. A leitura nativa segue experimental até validar em dispositivo real.

## O que está implementado

- Serviço de Acessibilidade filtrado apenas para `com.ubercab.driver` e `com.app99.driver`.
- Leitura de textos acessíveis na tela, extração de tarifa, distância e tempo, com rejeição de dados ambíguos.
- OCR visual opcional: no Android 11+, com consentimento separado, captura a tela da Uber/99 quando o texto não está acessível, faz reconhecimento ML Kit local e descarta a imagem imediatamente.
- Recomendação por R$/km e R$/h, com alerta se a distância até o embarque não estiver comprovadamente incluída.
- Cartão flutuante com autorização de sobreposição; voz opcional.
- Metas ajustáveis, custo/km, histórico local de 100 ofertas, exportação CSV sob comando do usuário.
- Simulador e controle manual da jornada com odômetro inicial **35.179 km em 06/11/2026 às 06h58**, Uber/99 e combustível.
- Nenhuma permissão de internet; nenhuma conta ou servidor. OCR opcional com imagens temporárias não armazenadas. Não armazena nomes, contatos ou endereços de passageiros.

## Limitações importantes

1. **NÃO foi validado no Uber Driver e na 99 reais.** O parser é experimental e precisa ser testado com ofertas reais, sem compartilhar informações de passageiros. Algumas telas podem ser desenhadas como imagens e não disponibilizar texto. O OCR local pode ajudar, mas sua leitura também pode falhar por variação de layout ou proteção de tela; nenhuma captura ou interpretação foi validada no aparelho real.
2. Ofertas lidas **não são corridas concluídas**. Ganhos efetivos precisam ser lançados no resumo da jornada.
3. O app não monitora GPS nem calcula trajetos; apenas interpreta valores visíveis. Distância de deslocamento até embarque precisa constar ou o sistema pede para conferir.
4. A publicação na Play Store exige cumprir regras de divulgação e consentimento para AccessibilityService, documentação e revisão; aprovação não garantida.
5. O projeto compila em ambiente Android Studio/Gradle com SDK 35, mas **não foi possível compilar o APK neste ambiente** (SDK Android não instalado). Testes Java do motor (19 verificações) estão executados; testes no celular ainda pendentes.
6. Não há integração autenticada ou oficial com Uber ou 99.

## Como obter e compilar APK

### Android Studio no notebook

1. Abra a pasta **android/** deste repositório no Android Studio (Open project).
2. Deixe o Android Studio sincronizar o Gradle e instalar os componentes necessários (`compileSdk 35`, JDK 17).
3. Use **Build > Build APK(s)**, ou execute `gradle :app:assembleDebug` se tiver Gradle configurado.
4. Instale `app/build/outputs/apk/debug/app-debug.apk` no seu celular Android com autorização para instalar apps de teste, depois valide os recursos experimentalmente.

### GitHub Actions (alternativa sem compilar localmente)

1. Envie o conteúdo deste pacote para um repositório **privado** no GitHub, preservando `.github/workflows/android-apk.yml` na raiz.
2. Na aba **Actions > CX Rota — Testes e APK Android**, execute o fluxo ou aguarde a execução após push na branch `main`.
3. Se o fluxo passar, baixe o artefato **CX-Rota-Android-debug**.

## Primeiro uso

1. Abra CX Rota; ajuste as metas (valores iniciais são exemplos, não recomendações financeiras garantidas).
2. Leia e aceite a declaração de uso de acessibilidade, se concordar, e habilite o serviço CX Rota nas configurações do Android.
3. Se quiser, habilite **OCR local** após ler a declaração específica de captura de imagens da tela.
4. Autorize **Exibir sobre outros apps** para o cartão flutuante, se desejar.
5. Abra Uber Driver/99 Motorista. A leitura só ocorrerá quando houver textos acessíveis reconhecíveis.
6. Teste primeiro pelo simulador, sempre parado. Não interaja com o celular pilotando.

## Testes de cálculo sem Android

```bash
javac -d tests/build app/src/main/java/com/cxwebstudio/cxrota/OfferEngine.java app/src/main/java/com/cxwebstudio/cxrota/OfferParser.java tests/EngineTest.java
java -cp tests/build EngineTest
```

## Aviso de privacidade

O serviço processa localmente textos acessíveis dos dois apps autorizados e, se o usuário habilitar OCR separadamente, imagens temporárias da tela, para extrair valor, distância e tempo. Essas imagens são descartadas sem salvamento. Os dados salvos são apenas números, plataforma, classificação e horário. O app não tem permissão de internet. A leitura pode ser interrompida em **Configurações > Acessibilidade**. O compartilhamento de CSV é opcional e manual. Não há clicks automáticos. Antes de qualquer distribuição pública, elaborar política de privacidade completa e submeter o uso de Acessibilidade à revisão aplicável.

## Prévia web pública

O painel de demonstração funciona em navegador Android/desktop:

Abra `../web/app.html` usando um servidor local/HTTPS. A URL antiga da demonstração foi desativada.

**Importante:** a prévia web NÃO faz leitura automática do Uber/99 e NÃO substitui o APK nativo. O simulador web passou por 6 cenários de interface (aceitar, recusar, conferir, entrada decimal, odômetro e fechamento de jornada). Os controles de ganhos e custos são preenchidos pelo motorista e ficam no armazenamento local do navegador; não sincronizam com o app nativo.

## Ajustes na versão 0.1.1

- Corrigido salvamento de metas quando o campo opcional de custo/km está vazio.
- Corrigida normalização de quebras de linha `\r` no extrator de ofertas.
- 19 verificações do motor Java aprovadas.
- Confirmados os pacotes Android das plataformas nas páginas oficiais do Google Play em novembro/2026.
- A versão Android **não foi compilada neste ambiente**: não há SDK Android/Gradle para gerar APK e não há conta CI com repositório disponível. Não há justificativa para chamar o ZIP do projeto de APK pronto. O teste real de Acessibilidade/OCR permanece pendente.
