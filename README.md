# Ficha Ensaio Regional — primeira versão

Aplicativo Android nativo em Kotlin para preencher a ficha de contagem do Ensaio Regional, salvar histórico local e gerar PDF A4.

## O que esta versão inclui
- Campos de localidade, data, presidência, palavra e duas regências.
- Quantidade para cada instrumento, incluindo acordeon.
- Categorias de ministério/participantes.
- Até 25 campos para hinos ensaiados.
- Observações gerais.
- Histórico salvo localmente no armazenamento privado do app.
- Revisão antes de gerar PDF.
- Geração de PDF e opção de compartilhar/salvar.

## Regras de totalização nesta versão
- Total de músicos: soma das quantidades dos instrumentos, a menos que seja informado um total manual maior que zero.
- Total de organistas: campo informado pelo usuário.
- Ministério: soma de Anciães, Diáconos, Cooperadores e Cooperadores de Jovens.
- Total geral calculado: músicos + organistas + ministério calculado + irmandade.
- Os campos de encarregados, examinadoras e instrutores aparecem na ficha, mas não são somados novamente no total geral para reduzir risco de dupla contagem. Confirme as regras oficiais e ajuste se necessário.
- O app não preenche totais a partir da foto; começa com campos vazios/zero.

## Compilar no Android Studio
1. Instale Android Studio (versão atual estável) em um computador.
2. Extraia o ZIP.
3. No Android Studio, escolha **Open** e selecione a pasta `EnsaioRegionalApp`.
4. Aguarde a sincronização do Gradle e instale os componentes Android SDK solicitados.
5. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. O APK de depuração costuma ficar em `app/build/outputs/apk/debug/app-debug.apk`.
7. Transfira o APK para o Android e abra-o. Se solicitado, permita instalar apps dessa origem.

## Observações
- Este ZIP contém código-fonte, não um APK compilado.
- A ficha PDF é redesenhada de forma limpa, não uma reprodução pixel a pixel do formulário fotografado.
- A primeira versão gera uma página A4; se muitos hinos/observações forem preenchidos, o conteúdo pode ficar apertado. Recomenda-se testar e revisar antes de uso oficial.
- O histórico fica somente no próprio celular e é apagado se os dados do aplicativo forem apagados ou o app for desinstalado.
