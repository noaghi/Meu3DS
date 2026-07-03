# **Meu3DS**

**Equipe:** Hiago Andrade, João Lucas, Mariane Barbosa

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Retrofit](https://img.shields.io/badge/Retrofit-DarkGreen?style=for-the-badge)
![Biometria](https://img.shields.io/badge/Biometric%20Auth-00BCD4?style=for-the-badge)

O **Meu3DS** é um ecossistema de gerenciamento de coleções projetado para entusiastas do console Nintendo 3DS. O aplicativo funciona como um catálogo interativo, permitindo que os usuários busquem dados atualizados de lançamentos globais, gerenciem seus títulos favoritos, adicionem amigos e acompanhem as coleções uns dos outros em tempo real. 

Este projeto foi desenvolvido como requisito avaliativo para a disciplina de **Desenvolvimento de Aplicações Móveis** do curso de **Sistemas e Mídias Digitais (SMD)** na **Universidade Federal do Ceará (UFC)**.

---

## Requisitos do Projeto e Cobertura Técnica

O projeto atende de forma estrita às seguintes especificações funcionais e arquiteturais exigidas:

1. **Cliente Android Nativo e Cross-Platform:** 
   * **Cliente Nativo:** Construído de forma robusta em ambiente Android Studio nativo com **Kotlin** e **Jetpack Compose**, utilizando o paradigma declarativo e injeção assíncrona por meio de *Coroutines* e *State Management*.
   * **Cliente Cross-Platform:** Desenvolvido paralelamente utilizando tecnologias híbridas para o compartilhamento de base de dados comuns expostos pela API e persistência na nuvem.
2. **Consumo de Serviço REST Externo:** Integração direta com a base de dados profissional de jogos da **IGDB (Internet Game Database)** via **Retrofit 2** e **Gson Converter**, injetando filtros de requisição RAW no corpo de chamadas `@POST`.
3. **Persistência em Backend no Firebase:** Integração centralizada com serviços corporativos Google através do `google-services.json`:
   * **Firebase Authentication:** Autenticação de usuários segura e fluxo de recuperação/reset de senhas criptografadas por e-mail.
   * **Firebase Realtime Database:** Estrutura NoSQL reativa para sincronização assíncrona instantânea de dados cadastrais, listas de favoritos e redes de relacionamentos de amigos.
4. **CRUD Completo e Persistente:** O ecossistema permite a Inserção, Leitura, Atualização e Deleção (CRUD) de relacionamentos e listas de jogos síncronas.
5. **Segurança via Autenticação Biométrica:** Uso da API nativa `androidx.biometric` para blindar o acesso e a alteração a fluxos sensíveis e dados internos do usuário.
6. **Internacionalização (i18n):** Suporte completo e dinâmico a múltiplos idiomas, adaptando labels locais para **Português (Brasil)** e **Inglês (EUA)** por meio do gerenciamento de recursos de strings nativas do sistema.

---

## Tecnologias e Bibliotecas Utilizadas

* **UI Framework:** Jetpack Compose (Material Design 3 com suporte a animações de transição de estado).
* **Navegação:** Compose Navigation (`NavHost` com tipagem estática e gerenciamento de pilha através de `popUpTo`).
* **Networking (REST):** Retrofit 2, OkHttp 3 (para construção de payloads dinâmicos text/plain) e Gson para desserialização de objetos.
* **Segurança:** Biometric Crypto API (`BiometricManager` & `BiometricPrompt`).
* **Concorrência:** Kotlin Coroutines (`Dispatchers.IO`, `withContext(Dispatchers.Main)` e `launch`).

---

## Funcionalidades do Ecossistema

* **Autenticação Avançada:** Criação de conta, login persistente integrado e recuperação de conta automatizada por e-mail.
* **Catálogo Global (IGDB API):** Exibição paginada (20 itens por página) de todos os jogos cadastrados na plataforma ID `37` (Nintendo 3DS), ordenados dinamicamente por relevância global (`total_rating`).
* **Motor de Busca Local e Global:** Filtragem em tempo real na aba de favoritos ou requisições dinâmicas de busca textual diretamente nos servidores da IGDB.
* **Painel Expandido (*AnimateContentSize*):** Cards interativos de jogos que se expandem com animações fluidas para revelar sinopses completas e formatações locais de data de lançamento (`dd/MM/yyyy`).
* **Painel de Perfil e Controle Cadastral:** Área dedicada para alteração de dados de cadastro do usuário. A edição permanece completamente travada até que o usuário confirme sua identidade por biometria.
* **Rede Social Interativa (Amigos):** Inserção de amigos via busca por e-mail no Firebase, exibição e remoção de vínculos. Ao clicar em um amigo, um modal reativo consome os nós do banco de dados e renderiza os jogos favoritos dele em tempo real.

---

## Implementação de Funcionalidade Sensível com Biometria

A segurança de dados do usuário é tratada de forma crítica. Na **Tela de Perfil**, os campos de alteração de dados permanecem em modo somente-leitura. O aplicativo invoca a API nativa de segurança do Android.

O gerenciador (`BiometricManager`) verifica se o dispositivo do usuário possui sensores biométricos ativos (`BIOMETRIC_STRONG` ou autenticação por credencial de segurança do dispositivo). Caso positivo, exibe o modal do sistema operacional isolando o ciclo de vida da aplicação. Uma vez concedido o sucesso da identidade, o aplicativo libera em memória as alterações reativas e os botões de escrita no Firebase.

---

## Relatório de Acessibilidade (Accessibility Scanner)

Como parte do compromisso de engenharia de software e design inclusivo do curso de SMD, o aplicativo passou por uma auditoria automatizada completa utilizando a ferramenta **Accessibility Scanner**. Os testes identificaram melhorias necessárias na interface, cujas **correções encontram-se pendentes** de implementação futura:

### Apontamentos Identificados (Ajustes Pendentes)

1.  **Taxas de Contraste Insuficientes (Texto/Fundo):**
    *   **Tela de Cadastro:** O botão de envio apresentou uma taxa de contraste estimada em **2,50** (Texto `#63646C` sobre fundo `#27282E`), abaixo do mínimo exigido de 4,50 para textos pequenos. *Ajuste planejado: Mudar o background para `#efeafb` ou clarear a tipografia.*
    *   **Tela de Login:** O link *"Esqueceu a senha"* apresentou contraste de **2,58** (Texto `#56575E` sobre fundo `#121318`), dificultando a leitura.
    *   **Tela de Perfil:** Os rótulos de dados cadastrais e o botão *"Adicionar"* apresentaram taxas de **3,04** e **2,50** respectivamente, necessitando de substituição por tons mais legíveis como `#d9d9d9`.
2.  **Redundância e Descrição de Itens:**
    *   **Botão Favoritar (Cards de Jogos):** O scanner apontou que o texto falado clicável *"Favoritar"* é idêntico em múltiplos itens da lista (detectado em 7 a 9 itens simultâneos). Para leitores de tela (TalkBack), isso gera ambiguidade. *Ajuste planejado: Concatenar dinamicamente o nome do respectivo jogo à descrição de acessibilidade do botão.*
3.  **Rótulos Indisponíveis e Textos Ocultos:**
    *   Determinados elementos estruturais geraram alertas de que o rótulo pode não ser interpretado corretamente por leitores de tela. Além disso, em elementos dinâmicos do catálogo (como os textos longos extraídos da API), há risco de o rótulo de acessibilidade divergir do texto visível na tela.

---

## Suporte Internacional (Inglês e Português)

O aplicativo conta com suporte completo a dois idiomas por meio dos mecanismos nativos de internacionalização (i18n) do ecossistema Android. O isolamento rígido de strings permite que o sistema alterne de forma fluida a linguagem gráfica inteira com base na preferência global do dispositivo.

### Estrutura de Recursos Utilizada
*   **`app/src/main/res/values/strings.xml`:** Atua como o arquivo de recursos padrão do sistema (fallback).
*   **`app/src/main/res/values-en/strings.xml`:** Contém as chaves localizadas e traduzidas especificamente para o idioma Inglês.

Toda a construção visual no Jetpack Compose consome exclusivamente referências dinâmicas via método `stringResource(R.string.[id_da_chave])`. Isso erradica por completo a presença de textos travados (*hardcoded strings*) dentro dos códigos fontes em Kotlin, garantindo a modularidade técnica exigida para a internacionalização do ecossistema móvel.

---

## Demonstração Visual da Aplicação (Fluxos do Sistema)

### **Fluxo de Autenticação e Entrada**
*Descrição:* Telas iniciais de controle de acesso ao ecossistema do aplicativo Meu3DS.
* **Tela de Login:** *(Inserir print da tela de login aqui)*
* **Tela de Cadastro:** *(Inserir print da tela de cadastro de usuário aqui)*

### **Catálogo de Jogos (Aba: Todos os Jogos)**
*Descrição:* Consumo em tempo real da API REST externa da IGDB com paginação e cards expansíveis.
* **Lista Principal Paginada:** *(Inserir print do catálogo geral carregado aqui)*
* **Card Expandido com Descrição:** *(Inserir print do card de um jogo expandido aqui)*
* **Barra de Busca Ativa:** *(Inserir print filtrando o catálogo por termo aqui)*

### **Gerenciamento de Favoritos (Aba: Meus Favoritos)**
*Descrição:* Sincronização em tempo real (Escrita/Deleção no Firebase Realtime Database) ao acionar o ícone de estrela.
* **Lista de Favoritos do Usuário:** *(Inserir print da lista de favoritos aqui)*

### **Perfil de Usuário, Biometria e Rede de Amigos**
*Descrição:* Interface de segurança para dados sensíveis e tela de conexões sociais entre usuários.
* **Perfil Bloqueado para Edição:** *(Inserir print da tela de perfil antes de liberar a edição aqui)*
* **Prompt Biométrico do Sistema Ativo:** *(Inserir print com o modal nativo de leitura de impressão digital ativo aqui)*
* **Painel de Amigos e Busca por E-mail:** *(Inserir print da listagem de amigos e campo de busca aqui)*
* **Modal com Favoritos do Amigo (Leitura Firebase):** *(Inserir print do diálogo contendo os jogos favoritos de um amigo selecionado aqui)*

---

### **Link para Vídeo Demonstrativo do Aplicativo**
https://youtu.be/897ARxNKqW8
https://youtube.com/shorts/jls5v0MmXZs
---
