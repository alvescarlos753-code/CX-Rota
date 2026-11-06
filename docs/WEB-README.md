# CX Rota — Versão visual Premium 2026

Criado para a CX Web Studio. O produto ainda é uma versão **beta** e **não cobra nem autentica usuários**.

## Abrir no VS Code

1. Abra esta pasta no VS Code.
2. Execute `npx serve .` ou outra hospedagem estática HTTPS/localhost.
3. Acesse `index.html` para a apresentação comercial, ou `app.html` para o aplicativo web.
4. Para testar os cálculos: `node test-core.js`.
5. O arquivo `CX-Rota-Premium-offline.html` na pasta mãe do ZIP pode ser aberto em navegadores compatíveis sem hospedagem; guarde backups de dados.

## Arquivos

- `index.html`, `landing.css`: vitrine de produto premium (preços simulados, sem checkout).
- `app.html`, `style.css`, `app.js`, `core.js`: controle e análise manual.
- `manifest.webmanifest`, `sw.js`, ícones: PWA em HTTPS.
- `test-core.js`: testes do cálculo.
- `PLANO-DE-LANCAMENTO.md`: monetização e requisitos para lançamento.

## Segurança e limitações

O protótipo guarda dados em localStorage do navegador; **não há sincronização em nuvem nem isolamento de contas**. Os preços são uma proposta comercial, não há botão real de compra. Não há leitura automática ativa na versão web. O projeto nativo Android está separado para compilação e testes em aparelho real.
