# CX Rota — Copiloto de rentabilidade para Uber Moto e 99 Moto

**Um projeto da CX Web Studio · Android nativo + painel web · versão de desenvolvimento `0.2.0-alpha`**

O **CX Rota** foi criado para ajudar motofretistas e motoristas de aplicativos a entender **quanto cada oferta e jornada pode render depois dos custos**. O projeto contempla motos **próprias ou alugadas**, acompanhamento de quilometragem, ganhos Uber/99, metas de rentabilidade, simulação de ofertas e histórico.

> [!IMPORTANT]
> **Estado real do projeto: ALFA — não é um produto comercial pronto.** O simulador web e os cálculos financeiros têm testes automatizados. O leitor Android de ofertas por Acessibilidade/OCR é **experimental** e ainda **não foi validado com ofertas reais** da Uber e da 99 no celular. O APK depende de compilação e testes. Não há login, pagamentos de assinatura, leitura autenticada por API das plataformas, sincronização entre aparelhos ou garantia de aprovação na Play Store.
>
> O CX Rota **não aceita nem recusa corridas automaticamente**. Ele apresenta uma recomendação; a decisão é sempre do motorista.

**Links rápidos:** [Guia do motorista](docs/GUIA-DE-USO.md) · [Publicar no GitHub](INSTRUCOES-PARA-PUBLICAR.md) · [Código Android](android/) · [Painel web](web/) · [Compilação automática](.github/workflows/android-apk.yml) · [Plano comercial](docs/PLANO-DE-LANCAMENTO.md)

---

## 1. O que está disponível hoje

| Componente | Situação | O que faz |
| --- | --- | --- |
| Painel web / PWA | **Prévia funcional** | Cadastro de veículos, custos, simulação manual, fechamento de jornada, relatórios e backup JSON |
| Motor financeiro | **Testado em cenários automatizados** | Combustível, manutenção, rateio de aluguel, metas e estimativa de resultado |
| Projeto Android em Java | **Código alfa** | Tela nativa, controle manual, serviço experimental de leitura de ofertas, alertas opcionais |
| Leitura automática Uber/99 | **Não validada no uso real** | Tenta ler valor, distância e duração a partir de textos acessíveis e OCR local com consentimento |
| GitHub Actions | **Workflow incluído** | Testa código e tenta compilar um `app-debug.apk` quando executado |
| Login, nuvem e assinaturas | **Não implementados** | Recursos necessários antes de vender planos |

As recomendações dependem dos dados que o aplicativo efetivamente consegue obter. **Sem distância total ou tempo confiável, deve-se conferir, e não assumir que a oferta é boa.**

## 2. Estrutura correta do repositório

```text
CX-Rota/
├── .github/
│   └── workflows/
│       └── android-apk.yml          # Testes + geração de APK debug
├── android/
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/cxwebstudio/cxrota/
│   │       │   ├── MainActivity.java
│   │       │   ├── OfferReaderService.java
│   │       │   ├── OfferParser.java
│   │       │   ├── OfferEngine.java
│   │       │   ├── CostModel.java
│   │       │   └── SettingsStore.java
│   │       └── res/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle.properties
│   ├── tests/
│   └── README.md
├── web/
│   ├── index.html                  # Página de apresentação
│   ├── app.html                    # Painel do motorista
│   ├── app.js, core.js, style.css  # Lógica/interface
│   ├── manifest.webmanifest, sw.js # PWA
│   └── test-core.js
├── docs/
│   ├── GUIA-DE-USO.md
│   ├── PLANO-DE-LANCAMENTO.md
│   └── WEB-README.md
├── .gitignore
├── INSTRUCOES-PARA-PUBLICAR.md
└── README.md
```

**Atenção ao GitHub:** alguns arquivos `.java` haviam sido enviados isoladamente para a **raiz** do repositório. Isso **não constitui um projeto Android compilável**. É preciso enviar a árvore completa acima — principalmente `android/app/src/main/...`, `android/app/build.gradle`, `android/settings.gradle` e `.github/workflows/android-apk.yml`. Os seis arquivos Java soltos na raiz são duplicações e não são utilizados pelo Gradle; após confirmar que as cópias corretas estão dentro de `android/`, os colaboradores poderão remover essas duplicações em uma alteração separada.

## 3. Publicar os arquivos pelo SITE do GitHub

O repositório desta iniciativa é **público**, por decisão do proprietário, para facilitar a colaboração. Evite colocar documentos pessoais, contatos de passageiros, senhas, tokens, chaves de API, certificados de assinatura ou bancos de dados reais nele.

1. No **notebook**, baixe o ZIP completo deste projeto e escolha **Extrair tudo**.
2. Abra `https://github.com/alvescarlos753-code/CX-Rota` e selecione a branch `main`.
3. Clique em **Add file → Upload files** (ou acesse `https://github.com/alvescarlos753-code/CX-Rota/upload/main`).
4. **Arraste o conteúdo extraído, mantendo as pastas**, para a área de envio do GitHub. Não envie apenas os `.java` e não envie o ZIP como se fosse código-fonte extraído.
5. Confira os caminhos antes de confirmar. Em particular, verifique se o workflow `.github/workflows/android-apk.yml` foi incluído; no Windows a pasta `.github` pode ficar pouco visível dependendo do explorador.
6. Preencha a mensagem de commit, por exemplo `feat: estrutura completa Android e web CX Rota`, e clique em **Commit changes**. Se o GitHub pedir uma proposta de alteração, conclua também o pull request.
7. Volte à aba **Code**. Devem aparecer `android/`, `web/`, `docs/` e `.github/` na **raiz**.

> Se os arquivos estiverem aparecendo sem as respectivas pastas, o upload não preservou os caminhos. Refaça pelo notebook arrastando pastas inteiras ou use o procedimento Git/VS Code no guia [INSTRUCOES-PARA-PUBLICAR.md](INSTRUCOES-PARA-PUBLICAR.md).

### Abrir no VS Code depois de publicar

1. Instale [Git](https://git-scm.com/downloads) e [Visual Studio Code](https://code.visualstudio.com/) no notebook, caso ainda não tenha.
2. No VS Code pressione `Ctrl + Shift + P`, procure **Git: Clone** e cole `https://github.com/alvescarlos753-code/CX-Rota.git`.
3. Selecione uma pasta local e clique em **Open / Abrir** ao terminar.
4. Abra `README.md` para conhecer o projeto, e `android/` para trabalhar na versão nativa.
5. Para enviar novas alterações: abra **Source Control** no VS Code, confira os arquivos modificados, escreva uma mensagem, faça **Commit** e **Sync / Push** (o GitHub poderá solicitar login).

## 4. Gerar um APK pelo GitHub Actions — sem Android Studio

**Pré-requisito:** o workflow `.github/workflows/android-apk.yml` e toda a estrutura `android/` devem estar publicados na branch `main`.

1. Abra `https://github.com/alvescarlos753-code/CX-Rota/actions`.
2. Escolha o workflow **CX Rota — Testes e APK Android**.
3. Clique em **Run workflow**, selecione `main` e confirme **Run workflow**. Ele também pode iniciar automaticamente após alterações em `android/`.
4. Aguarde o processamento. Abra a execução para ver os jobs de testes e a compilação.
5. **Somente se estiver verde (concluída com sucesso):** procure **Artifacts** e baixe `CX-Rota-Android-debug`.
6. Extraia o artefato. O arquivo esperado é `app-debug.apk`.
7. Se aparecer vermelho, abra o job **Testar motor e compilar APK debug** e leia a primeira falha real do log. Não existe APK válido até a compilação concluir.

**O APK é `debug`**, voltado a testes. Não é pacote de produção assinado, não deve ser vendido e não é equivalente à aprovação na Play Store. O workflow foi preparado, mas **a compilação Android e o uso em aparelho real ainda não foram validados**.

## 5. Instalar e testar no Android

1. Transfira `app-debug.apk` para o aparelho Android que fará os testes.
2. Abra o arquivo; se o Android solicitar, autorize **instalação de apps desconhecidos apenas para o aplicativo que abriu o APK**, verificando a origem do arquivo. Desative a autorização após a instalação, se não for mais necessária.
3. Abra **CX Rota**. Teste primeiro **Simular uma oferta** com valores fictícios.
4. Em **Custos do veículo**, escolha **Moto própria** ou **Moto alugada**, informe os valores reais e toque em **Salvar os custos da moto**.
5. Em **Metas configuráveis**, defina seu mínimo desejado em **R$/km** e **R$/hora**; toque em **Salvar minhas metas**.
6. **Opcional e experimental:** leia a explicação de acesso, toque em **Autorizar leitura das ofertas**, e ative o serviço **CX Rota** nas configurações de **Acessibilidade** do Android. Esse tipo de permissão é sensível; habilite-a apenas se compreender e concordar.
7. **Opcional:** autorize **Exibir sobre outros apps** para o cartão flutuante; habilite avisos por voz e OCR se fizer sentido para o teste. OCR requer um consentimento separado e pode não funcionar em todas as telas.
8. Abra Uber Driver ou 99 Motorista **em situação segura e com o veículo parado** e avalie o comportamento. A leitura pode falhar ou mostrar **CONFERIR** quando não houver dados suficientes.
9. Ao encerrar o dia, preencha **Resumo da jornada** com odômetro inicial/final, ganhos confirmados de cada plataforma, abastecimento e despesas extras. Toque em **Salvar e calcular minha jornada**.
10. Exporte o histórico em CSV se precisar de uma cópia dos resultados de ofertas.

### Sobre os acessos solicitados

- **Acessibilidade:** leitura local do texto exibido em telas reconhecidas do Uber Driver e da 99 Motorista. Não equivale a API oficial de nenhuma das duas empresas.
- **OCR local:** tenta reconhecer texto em imagens temporárias da tela após consentimento; a implementação ainda depende de validação em aparelho real.
- **Sobreposição:** exibe uma indicação flutuante; pode exigir autorização separada no Android.
- **Voz:** é um alerta opcional; não substitui observar o trânsito.

Nenhum desses recursos autoriza o app a decidir ou clicar automaticamente numa oferta. **Nunca manipule o aplicativo enquanto pilota.**

## 6. Usar o painel WEB/PWA

A versão web **não lê ofertas automaticamente**; é um simulador e organizador financeiro manual.

1. Abra `web/index.html` para a página do produto ou `web/app.html` para a área do motorista. Prefira servir os arquivos por **HTTPS** ou `localhost` para os recursos da PWA funcionarem corretamente.
2. No menu **Moto**, cadastre a moto (própria/alugada) e suas metas. Pressione **Salvar veículo e metas**.
3. Em **Ofertas**, selecione Uber Moto ou 99 Moto, informe **valor, distância total e duração**, marque o deslocamento até o embarque somente se estiver incluído, e toque em **Calcular rentabilidade**.
4. Em **Jornada**, informe odômetro, horas, ganhos reais da Uber e 99 e despesas; selecione **Salvar fechamento da jornada**.
5. Em **Início**, acompanhe o resumo; em **Histórico**, exporte relatórios CSV e use **Baixar backup JSON**.
6. Para transferir registros a outro navegador, use **Restaurar backup JSON** com o arquivo exportado anteriormente.

**Armazenamento:** na versão web, tudo fica no `localStorage` **do navegador e da origem atual**. Limpar dados, mudar de domínio ou usar outro aparelho pode fazer os registros não aparecerem. Não existe sincronização automática: faça backups JSON com frequência. Não armazene dados de terceiros ou informações sensíveis na prévia.

### Executar a prévia web localmente no notebook

Com Python instalado:

```bash
cd web
python -m http.server 8080
```

Abra `http://localhost:8080/` para a página comercial ou `http://localhost:8080/app.html` para o painel. Encerrar o servidor fecha o acesso local; não publica o projeto na internet. Para instalar como PWA, a interface deve ser hospedada por HTTPS e o navegador precisa oferecer essa função.

## 7. Como os cálculos funcionam

- **Receita bruta da jornada:** ganhos reais informados da Uber **+** ganhos reais da 99.
- **Custo estimado de combustível:** km percorridos **÷** consumo médio (`km/l`) **×** preço por litro.
- **Manutenção estimada:** quilômetros percorridos **×** reserva configurada em `R$/km`.
- **Moto alugada:** aluguel informado por dia, ou **rateado por dias planejados de trabalho** para contratos semanais/mensais; não é deduzido de novo a cada oferta.
- **Resultado econômico estimado:** receita bruta menos combustível consumido estimado, manutenção, aluguel rateado, outros custos fixos e despesas extras informadas.
- **R$/km da oferta:** valor da oferta dividido pela distância total considerada; **R$/hora:** valor dividido pelo tempo em horas.
- **ACEITAR / RECUSAR / CONFERIR:** comparação com metas configuradas pelo motorista, não uma garantia de ganho nem avaliação de riscos externos, tempo de espera ou segurança da região.

**Evite desconto duplicado:** o dinheiro pago em abastecimento na jornada é registrado como movimento de caixa/informação. O custo econômico do combustível é estimado pelo consumo da moto e quilômetros efetivamente rodados. Os dois conceitos não devem ser subtraídos ao mesmo tempo como se fossem gastos diferentes.

### Exemplo demonstrativo (não é uma oferta real)

- Oferta: R$ 15,00 por 6 km e 15 minutos, incluindo deslocamento até embarque.
- Cálculo: `15 ÷ 6 = R$ 2,50/km` e `15 ÷ (15/60) = R$ 60,00/h`.
- O indicador final depende **das suas metas e custos**, portanto duas motos podem ter decisões diferentes para a mesma oferta.

> **Importante para locação:** o rateio por dia trabalhado serve à análise de rentabilidade. O contrato pode exigir pagamentos mesmo em dias sem corrida. Acompanhe o valor total devido e os vencimentos separadamente.

## 8. Testes disponíveis para desenvolvedores

**Requisitos locais:** Node.js (para web) e Java JDK 17 ou superior (para testar o núcleo Java). Para **compilar o Android**, é necessário Android SDK 35, Gradle compatível e dependências do projeto, ou usar o workflow do GitHub.

```bash
# Testes da versão web (na raiz do repositório):
node web/test-core.js

# Testes Java do núcleo (bash, Linux/macOS/Git Bash):
cd android
mkdir -p tests/build
javac -encoding UTF-8 -d tests/build \
  app/src/main/java/com/cxwebstudio/cxrota/OfferEngine.java \
  app/src/main/java/com/cxwebstudio/cxrota/OfferParser.java \
  app/src/main/java/com/cxwebstudio/cxrota/CostModel.java \
  tests/EngineTest.java tests/CostModelTest.java
java -cp tests/build EngineTest
java -cp tests/build CostModelTest
```

Na cópia local revisada, foram executadas **19 verificações de extração/análise**, **17 de custos** e **34 da lógica web** (70 verificações). Isso **não prova** que o aplicativo completo funciona em ofertas reais ou que o APK será compilado sem ajustes no pipeline.

Para Android Studio: abra **a pasta `android/`**, aguarde sincronização do Gradle, configure o SDK 35 e use **Build → Build APK(s)**. O projeto **não contém Gradle Wrapper (`gradlew`)** no pacote atual; o Android Studio precisa ter Gradle disponível ou você pode usar o workflow que configura Gradle na nuvem.

## 9. Problemas frequentes

| Problema | O que verificar |
| --- | --- |
| Aba **Actions** não mostra workflow | Confirme `.github/workflows/android-apk.yml` na raiz do repositório, branch `main` e permissões do Actions nas configurações |
| Erro ao compilar APK | Abra o log do job que falhou; verifique `android/` completo, SDK/Gradle/Java e dependências |
| Só vejo `.java` soltos | O upload ficou incompleto: envie as pastas completas, mantendo os caminhos |
| **Nenhum APK em Artifacts** | O job pode ter falhado, ainda estar executando ou o artefato ter expirado |
| APK abre, mas não lê Uber/99 | Acessibilidade, consentimento, pacote correto e compatibilidade de layout; a integração está **em validação** |
| Não aparece cartão flutuante | Verifique autorização **Exibir sobre outros apps** e configurações do app |
| OCR não reconhece | Permissão/captura, versão do Android ou tela sem imagem legível; **não assuma dados incorretos como válidos** |
| Valores de custo estranhos | Confira separador decimal, km/l, preço/litro, período e dias de trabalho; não duplique combustível |
| Histórico web sumiu | Verifique mesmo navegador/domínio; restaure um backup JSON exportado antes, se houver |
| Quero cobrar assinaturas | Ainda falta implementar autenticação, backend, cobrança verificável, suporte, políticas e testes de segurança |

## 10. O que falta para comercializar

1. **Compilação e instalação reais:** gerar APK debug, corrigir erros e testar em Android físico.
2. **Validação de ofertas:** testar diferentes versões do Uber Driver/99 Motorista com consentimento, sem expor dados de passageiros; medir taxa de leitura incorreta e adotar comportamento seguro **CONFERIR**.
3. **Qualidade e segurança:** testes de interfaces, persistência, permissões, bateria, acessibilidade, falhas de OCR e compatibilidade com fabricantes.
4. **Contas reais:** autenticação, armazenamento isolado por usuário, controles de privacidade, recuperação de acesso e exclusão de dados.
5. **Monetização:** gateway de pagamentos, gestão de assinatura, renovação/cancelamento, tributos, termos de serviço e suporte.
6. **Conformidade:** política de privacidade, LGPD, regras de Acessibilidade da Play Store e termos aplicáveis de Uber e 99; aprovação de distribuição não é garantida.
7. **Piloto:** testar com motoristas que têm motos próprias e alugadas antes de anunciar desempenho ou publicar comercialmente.

## 11. Colaboração no GitHub

Este repositório está **público** para facilitar a leitura e colaboração. Para seu amigo editar diretamente, convide-o em **Settings → Collaborators** (não compartilhe sua senha). O ideal é usar **branches** e **Pull Requests** para revisar alterações antes de incluir na `main`.

**Padrão sugerido de mudança:** descrever o problema → criar branch → modificar → rodar testes → abrir Pull Request com capturas e evidências → revisar → incorporar à `main`. Não suba `.env`, chaves, credenciais, APKs com dados particulares ou históricos reais de passageiros.

O código-fonte é publicado para colaboração, mas isso **não significa que uma licença de redistribuição comercial já tenha sido concedida**. Defina licenciamento e propriedade intelectual com os colaboradores antes de comercializar.

---

**CX Web Studio · CX Rota · Desenvolvimento em andamento.** Versões e metas de lucro são estimativas configuráveis; nunca use o aparelho enquanto pilota.
