# Documentacao da Fase 1 - City Cleaner

## Visao geral
A Fase 1 representa o bloco de decisoes urbanas do jogo.
O jogador atravessa cenarios com dilemas ambientais e cada escolha altera dois indicadores:

- Eco score
- Poluicao

Objetivo da fase:
- Concluir todas as decisoes sem atingir o limite de poluicao.
- Ao concluir, liberar a transicao para a Fase 2.

Condicao de derrota:
- Quando a poluicao chega a 100% (Game Over).

## Fluxo da fase
1. Tutorial inicial em overlay (botao Entendi).
2. Escolha de locomocao (bike ou carro).
3. Cenario do cafe (copo sustentavel ou descartavel).
4. Cenario da torneira e do grupo de criancas.
5. Cenario final da arvore com decisao em tempo.
6. Tela de fase concluida e botao para continuar para a Fase 2.

As mudancas de cenario usam transicoes com fade e reposicionamento do jogador.

## Controles e interacao
Controles de movimento:
- A ou seta esquerda: andar para a esquerda.
- D ou seta direita: andar para a direita.
- W, espaco ou seta cima: pular.

Controles de interacao:
- E: interagir com o elemento proximo (abrir escolha de transporte, abrir decisoes, iniciar decisao da arvore).
- 1 e 2: selecionar opcoes nos overlays de decisao.
- Enter ou E (durante tutorial): fechar tutorial inicial.

UI de apoio:
- Botao Entendi: fecha tutorial.
- Botao Continuar para a Fase 2: aparece quando a fase termina com sucesso.

## Como funciona a barra de poluicao
Estado inicial:
- Poluicao inicia em 60%.
- Eco score inicia em 0.

Regra geral de pontuacao por escolha:
- Escolha sustentavel: eco score +20 e poluicao -10.
- Escolha nao sustentavel: eco score -5 e poluicao +10.

Excecao na decisao da arvore:
- Intervir conversando: eco score +6 e sem reducao direta de poluicao.

Limites aplicados no codigo:
- Poluicao nunca fica abaixo de 0.
- Poluicao nunca passa de 100.
- Eco score nunca fica abaixo de 0 nos casos com penalidade.

Feedback visual da barra:
- Verde para niveis baixos de poluicao.
- Amarelo para faixa intermediaria.
- Vermelho para niveis altos.

Condicao de Game Over:
- Poluicao >= 100.

## Decisoes da Fase 1
### 1) Transporte
Tema:
- Locomocao consciente.

Opcoes:
- Bike (sustentavel).
- Carro (nao sustentavel).

Detalhe de fluxo:
- A decisao de transporte precisa ser resolvida antes de avancar para os proximos cenarios.

### 2) Cafe
Tema:
- Loja Sao Joao (Takeaway Coffee).

Opcoes:
- Pegar copo sustentavel (sustentavel).
- Pegar copo descartavel (nao sustentavel).

### 3) Torneira
Tema:
- Torneira da rua.

Opcoes:
- Fechar a torneira (sustentavel).
- Deixar a torneira aberta (nao sustentavel).

### 4) Criancas jogando lixo
Tema:
- Grupo de criancas.

Opcoes:
- Intervir e conscientizar (sustentavel).
- Deixar pra la (nao sustentavel).

### 5) Corte ilegal de arvore (decisao em tempo)
Fluxo em etapas:
- Etapa ALERTA: aviso curto antes da escolha.
- Etapa PRINCIPAL: decidir se intervem ou ignora.
- Etapa DETALHE: se intervir, escolher denunciar ao orgao ambiental ou conversar.

Temporizador:
- Se o tempo acabar nas etapas de escolha, o jogo resolve como escolha negativa (nao intervir).

Impactos:
- Nao intervir: eco score -5, poluicao +10.
- Denunciar ao orgao ambiental: eco score +20, poluicao -10.
- Intervir conversando: eco score +6.

## Estrutura tecnica da Fase 1
## Arquivos principais
- src/citycleaner/view/PhaseOnePanel.java
- src/citycleaner/controller/KeyboardController.java
- src/citycleaner/model/entity/Player.java
- src/citycleaner/model/physics/PhysicsEngine.java
- src/citycleaner/model/physics/CollisionManager.java
- src/citycleaner/model/world/Platform.java
- src/citycleaner/util/Constants.java
- src/citycleaner/view/MainWindow.java

## Responsabilidades por classe
PhaseOnePanel:
- Controla estado completo da fase.
- Roda o game loop (update + repaint).
- Gerencia transicoes de cenario.
- Processa decisoes, pontuacao e poluicao.
- Desenha HUD, overlays e dicas de interacao.
- Emite resultado final pela classe interna PhaseOneResult.

KeyboardController:
- Trata movimento base do jogador.
- Direcoes esquerda/direita e pulo.

DecisionInputController (classe interna de PhaseOnePanel):
- Trata interacoes contextuais da fase.
- Tecla E para abrir interacoes.
- Teclas 1 e 2 para decidir opcoes.

Player:
- Mantem posicao, velocidade, direcao e estado de pulo.
- Aplica fisica basica no metodo update().

PhysicsEngine:
- Atualiza jogador e delega colisao para CollisionManager.

Constants:
- Define dimensoes da janela, area jogavel, HUD, fisica e FPS.

MainWindow:
- Controla troca de telas.
- Fluxo: menu -> intro -> fase 1 -> fase 2.
- Recebe PhaseOneResult e injeta dados da Fase 1 na Fase 2.

## Maquina de estados da fase
Estados relevantes gerenciados no painel:
- Tutorial aberto/fechado.
- Decisao de transporte pendente/resolvida.
- Cenario atual (inicio, pos-transporte, terceiro, quarto).
- Decisao ativa (overlay aberto) ou exploracao livre.
- Game Over.
- Tela final da fase pronta para continuar.

Essa modelagem por flags booleanas simplifica o controle de fluxo sem depender de frameworks externos.

## Como a fase termina e integra com o resto do jogo
Quando a fase termina com sucesso:
- O painel mostra overlay de fase concluida.
- O botao Continuar para a Fase 2 dispara callback.
- O callback envia PhaseOneResult com:
  - completedSteps
  - totalSteps
  - ecoScore
  - pollutionLevel

Na MainWindow:
- startGame(PhaseOneResult) recebe esse resultado.
- Esses dados sao passados ao construtor da GamePanel (Fase 2).

## Recursos visuais usados
Exemplos de sprites da Fase 1:
- Fase1.png
- CenarioFase1_2.png
- Fase1_3.png
- Fase1_4.png
- Personagem1.png
- Personagem2.png
- Bike.png
- Carro.png
- Cartoes de escolha (Escolha*.png)

Todos os recursos sao carregados por ResourceLoader.

## Observacoes de manutencao
Para adicionar uma nova decisao:
1. Criar novo DecisionPoint em createDecisionPoints().
2. Definir trigger, tema, opcoes e impacto sustentavel.
3. Ajustar fluxo de cenarios se a nova decisao depender de ordem.
4. Incluir card/sprite de opcao, se necessario.
5. Validar efeitos em eco score, poluicao e condicao de Game Over.

Para alterar dificuldade:
- Ajustar POLLUTION_LIMIT.
- Ajustar valores de ganho/perda de eco score e poluicao.
- Ajustar tempos de decisao (TREE_*_MS).
- Ajustar distancia de gatilho de interacao.