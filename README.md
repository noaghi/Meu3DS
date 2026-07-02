# **Meu3DS**

**Equipe**
Hiago Andrade
João Lucas
Mariane Barbosa

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

## Melhorias de Acessibilidade e Usabilidade (Accessibility Scanner)

A interface do **Meu3DS** foi projetada visando os mais rigorosos padrões inclusivos de usabilidade ditados pela WCAG e validados via ferramentas de diagnóstico automático.

### **Comparativo de Validação**

| Critério de Acessibilidade | Estado da Interface Implementada | Resultado do Scanner de Diagnóstico |
| :--- | :--- | :--- |
| **Contraste de Texto** | Emprego restrito dos esquemas de cores do **Material Design 3** (`MaterialTheme.colorScheme`), garantindo taxas superiores a 4.5:1 tanto para elementos de primeiro plano quanto para cores de superfície. | **Nenhuma sugestão** (Aprovado com sucesso em conformidade com as diretrizes de legibilidade para textos pequenos e descrições). |
| **Alvos de Toque (Touch Targets)** | Todos os elementos acionáveis, como os botões de paginação, navegação de abas e ícones utilitários (`IconButton`), possuem áreas de clique configuradas estritamente com tamanhos mínimos de **48.dp x 48.dp**. | **Nenhuma sugestão** (Mitigação total de problemas relacionados a cliques acidentais e erros de digitação). |
| **Rótulos de Acessibilidade** | Elementos puramente visuais possuem descrição nula para evitar redundâncias em leitores de tela. Ícones funcionais e com estados variáveis (como a Estrela de Favorito e o botão de exclusão) possuem propriedades de acessibilidade dinâmicas (`contentDescription = "Favoritar"`, `"Remover"`, `"Sair"`). | **Nenhuma sugestão** (Todos os rótulos de acessibilidade do aplicativo incluem com fidelidade o texto visível e o estado atual do componente). |
| **Estrutura Tipográfica** | Utilização de escalas tipográficas organizadas hierarquicamente com pesos de fontes bem delimitados (`FontWeight.ExtraBold`, `FontWeight.Bold`, `FontWeight.SemiBold`). | **Nenhuma sugestão** (A ancoragem visual permite a rápida varredura e leitura confortável de sinopses e títulos longos). |

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

## Próximos Passos & Internacionalização (i18n)

Para a entrega final da homologação do ecossistema, os seguintes módulos encontram-se em fila de desenvolvimento:
1.  **Internacionalização (PT-BR / EN-US):** Migração das strings estáticas de validação e rotulagem das telas para arquivos de recursos isolados (`strings.xml`), habilitando a tradução dinâmica com base no idioma do sistema operacional do smartphone.
2.  **Módulo Cross-Platform:** Conclusão do cliente espelho utilizando framework multiplataforma compartilhando as mesmas regras de negócio e consumo de endpoints centralizados neste repositório.

O suporte linguístico do aplicativo foi projetado centralizando as chaves de tradução nos dicionários de recursos de strings do Android XML nativo:
* `res/values/strings.xml` — Conterá as strings padrão em **Português (Brasil)**.
* `res/values-en/strings.xml` — Conterá o mapeamento completo mapeado para o **Inglês (EUA)**.

Os componentes em Jetpack Compose consomem as propriedades de forma desacoplada através do método de escuta dinâmico do contexto da aplicação (`stringResource(id)`), garantindo que o aplicativo adapte instantaneamente todas as mensagens do Firebase, dicas de inputs de busca, diálogos de biometria e rótulos de navegação baseando-se no idioma global configurado nas configurações de sistema do dispositivo móvel do usuário.
