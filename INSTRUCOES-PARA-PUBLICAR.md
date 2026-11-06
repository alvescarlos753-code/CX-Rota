# Publicar o CX Rota completo no GitHub

**Destino do projeto:** https://github.com/alvescarlos753-code/CX-Rota

O repositório já existe e está **público por escolha do proprietário**. Antes de começar, baixe e **extraia** o ZIP do projeto completo. Não publique dados pessoais ou credenciais.

## Método 1 — pelo site do GitHub, no notebook

1. Abra o repositório na branch `main` e clique **Add file → Upload files**.
2. Extraia o ZIP no Windows e arraste **as pastas e arquivos completos**, incluindo `.github`, `android`, `web`, `docs`, `.gitignore`, `README.md` e este guia, para a página de upload.
3. Confirme que aparecem caminhos aninhados como `android/app/src/main/AndroidManifest.xml` e `.github/workflows/android-apk.yml`.
4. Escreva um commit como `feat: publicar estrutura Android e web CX Rota` e clique **Commit changes**.
5. Verifique na aba Code se aparecem as pastas principais. Os Java soltos na raiz que foram enviados anteriormente não substituem a estrutura Android e podem ser removidos depois que a estrutura estiver completa.
6. Abra **Actions → CX Rota — Testes e APK Android**. Execute **Run workflow**; baixe o artefato de APK **somente se o workflow ficar verde**.

Se o GitHub não conseguir preservar pastas no upload, use o método 2.

## Método 2 — pelo VS Code / terminal Git

Com Git instalado, abra o terminal em uma pasta de projetos, execute:

```bash
git clone https://github.com/alvescarlos753-code/CX-Rota.git
```

Copie o **conteúdo extraído** do ZIP para a pasta local clonada `CX-Rota` (sem sobrescrever o diretório oculto `.git`). Em seguida, no terminal dentro dessa pasta:

```bash
git add .
git commit -m "feat: publicar projeto completo CX Rota"
git push origin main
```

**Não use `git init` ou `git push --force`** nesse repositório já existente. Revise as alterações antes do commit. Se o Git pedir autenticação, entre na sua conta por fluxo oficial do GitHub.

## Estrutura mínima para compilar

- `android/settings.gradle`
- `android/build.gradle`
- `android/app/build.gradle`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/com/cxwebstudio/cxrota/*.java`
- `android/app/src/main/res/...`
- `.github/workflows/android-apk.yml`

O resultado do GitHub Actions será um **APK de testes (debug)** caso a compilação tenha êxito. O APK nativo ainda precisa de testes em ofertas reais, permissões e compatibilidade Android, conforme [README.md](README.md).

## Atualizar somente o README pelo site

Se a estrutura do projeto já estiver no repositório, abra `README.md`, clique no **ícone de lápis** (Edit this file), cole o conteúdo atualizado e confirme **Commit changes**. Isso não substitui o envio dos demais arquivos do projeto.
