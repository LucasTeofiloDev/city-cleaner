# Documentacao da Fase 2 e Cenas Finais - City Cleaner

## Visao geral
A Fase 2 e uma fase de coleta de lixo com tempo limitado.
Depois do encerramento da fase, o jogo mostra uma historia final que pode ser:

- Final bom
- Final ruim

O tipo de final depende do nivel de poluicao herdado da Fase 1.

## Regras de jogo da Fase 2
Objetivo:
- Coletar lixo no mapa e depositar na lixeira para aumentar a pontuacao.

Duracao:
- 20 segundos.

Fluxo basico:
1. O jogador fecha o overlay de instrucoes e inicia a fase.
2. Aproxima do lixo e aperta E para coletar.
3. Aproxima da lixeira e aperta E para depositar.
4. Cada deposito gera pontos e respawna um novo lixo no mapa.
5. Quando o tempo acaba, a fase termina e mostra o resultado.

## Controles da Fase 2
Movimentacao:
- A ou seta esquerda: andar para esquerda
- D ou seta direita: andar para direita
- W, espaco ou seta cima: pular

Interacao:
- E: coletar lixo quando estiver perto
- E: depositar lixo na lixeira quando estiver carregando

Inicio da fase:
- Botao Iniciar Fase 2
- Enter ou E (enquanto o overlay de instrucoes estiver aberto)

## HUD e feedback visual
Durante a Fase 2:
- Timer central (Tempo: Xs)
- Score da fase
- Barra de poluicao (valor vindo da Fase 1)
- HUD inferior com titulo e status

Feedback de acao:
- Quando o jogador deposita lixo, aparece +10 (ou valor do item) perto da lixeira.
- Hint contextual E: Coletar lixo quando estiver proximo de item.
- Hint contextual E: Depositar lixo quando estiver proximo da lixeira e carregando item.

## Pontuacao e cronometro
Pontuacao:
- Cada item de lixo vale POINTS_ITEM (atualmente 10).
- A pontuacao da Fase 2 fica isolada em PhaseTwoScore.

Cronometro:
- Gerenciado por PhaseTwoTimer.
- Quando chega a zero, a Fase 2 e encerrada.

Pontuacao final exibida:
- Pontuacao final = pontuacao da Fase 1 + pontuacao da Fase 2.

## Como o jogo decide final bom ou ruim
Regra:
- Final bom quando poluicao < 60.
- Final ruim quando poluicao >= 60.

Importante:
- A poluicao exibida na Fase 2 nao muda nessa fase.
- Ela vem como resultado da Fase 1 e define o tipo de final.

## Cenas finais
As cenas finais aparecem apos um pequeno overlay de resultado.

### Final bom
Caracteristicas:
- Sequencia automatica de 4 imagens.
- Troca de imagem a cada 3 segundos.
- Frase unica no card inferior durante toda a sequencia.
- FIM na ultima imagem.

Ordem de imagens:
1. Cena1Gemini.png
2. CenaFinal1.png
3. CenaFinal2.png
4. CenaFinal3.png

Musica do final bom:
- Sunrise_on_the_Lowlands.mp3

### Final ruim
Caracteristicas:
- Sequencia de 2 imagens.
- Avanco manual com botao Proxima cena.
- Uma frase especifica para cada imagem.
- FIM na ultima imagem.

Ordem de imagens:
1. CenaFinalRuim.jpg
2. CenaFinalRuim2.png

Musica do final ruim:
- Final_Heartbeat.mp3

## Estrutura tecnica no codigo
## Arquivos principais
- src/citycleaner/view/GamePanel.java
- src/citycleaner/view/phase2/PhaseTwoController.java
- src/citycleaner/view/phase2/PhaseTwoTimer.java
- src/citycleaner/view/phase2/PhaseTwoScore.java
- src/citycleaner/view/MainWindow.java
- src/citycleaner/util/AudioManager.java

## Responsabilidades por classe
GamePanel:
- Orquestra a Fase 2 inteira.
- Renderiza gameplay, HUD, overlay de resultado e finais.
- Faz o gate de visibilidade das cenas finais.
- Escolhe final bom/ruim com base na poluicao.
- Troca a trilha para musica especifica do final.

PhaseTwoController:
- Regras da Fase 2 (coletar, depositar, encerrar por tempo).
- Controle de itens de lixo e proximidade com lixeira.
- Geracao de novos itens apos deposito.

PhaseTwoTimer:
- Contagem regressiva da fase.

PhaseTwoScore:
- Acumulo de pontos da Fase 2.

MainWindow:
- Faz transicao da Fase 1 para Fase 2.
- Inicia Tidal_Warning.mp3 ao entrar na Fase 2.

AudioManager:
- Troca de trilha global.
- Suporte a WAV por Clip.
- Suporte a MP3 por JavaFX e fallback por JLayer.
- Controle para evitar sobreposicao de trilhas em troca de musica.

## Fluxo de audio por etapa
1. Menu e jogo inicial:
- GameMusic.wav

2. Entrada da Fase 2:
- Tidal_Warning.mp3

3. Quando as cenas finais ficam visiveis:
- Final bom: Sunrise_on_the_Lowlands.mp3
- Final ruim: Final_Heartbeat.mp3

O painel tenta garantir que a musica ativa seja a correta do final.

## Detalhes de implementacao de finais
Gate de exibicao:
- Primeiro aparece o overlay de resultado por alguns milissegundos.
- Depois inicia a camada de cena final.

Renderizacao:
- Imagens finais sao desenhadas com escala padronizada para manter consistencia visual.
- Card de texto com transparencia no rodape.

Interacao no final ruim:
- Botao Proxima cena visivel apenas enquanto houver proxima imagem.

## Observacoes de manutencao
Para mudar duracao das cenas do final bom:
- Ajustar GOOD_ENDING_SCENE_DURATION_MS em GamePanel.

Para adicionar mais imagens no final ruim:
1. Inserir imagem no array badEndingStorySprites.
2. Inserir texto correspondente em BAD_ENDING_STORY_TEXTS.
3. Validar logica do botao Proxima cena.

Para trocar musicas:
- Alterar GOOD_ENDING_MUSIC_PATH e BAD_ENDING_MUSIC_PATH em GamePanel.
- Garantir arquivo em resources/audio/music.

Para alterar regra de final bom/ruim:
- Ajustar GOOD_ENDING_POLLUTION_THRESHOLD.