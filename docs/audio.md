# Documentacao de Audio - City Cleaner

## Resumo
A trilha sonora de fundo do jogo foi implementada com as classes nativas do Java, sem uso de bibliotecas externas.

A versao final utiliza arquivo WAV para garantir compatibilidade no ambiente atual.

## Arquivo de audio utilizado
- Caminho atual da musica: resources/audio/music/GameMusic.wav
- Observacao: o arquivo original em MP3 foi convertido para WAV para funcionar corretamente com Java Sound.

## Como foi implementado
A implementacao foi feita em 3 pontos principais:

1. Gerenciador de audio
- Arquivo: src/citycleaner/util/AudioManager.java
- Responsabilidades:
  - Tocar musica em loop continuo
  - Parar musica
  - Alternar som ligado/desligado (mute)
  - Manter estado global de audio
- Tecnica usada para WAV:
  - AudioSystem.getAudioInputStream(...)
  - AudioSystem.getClip()
  - clip.loop(Clip.LOOP_CONTINUOUSLY)

2. Integracao com fluxo da janela
- Arquivo: src/citycleaner/view/MainWindow.java
- Comportamento:
  - A musica inicia ja no menu principal
  - Continua nas cutscenes e no gameplay
  - Ao sair do jogo, a musica e interrompida

3. Controle no menu
- Arquivo: src/citycleaner/view/MenuPanel.java
- Foi adicionado um botao de audio:
  - Som: ligado
  - Som: desligado
- Ao clicar:
  - Se desligar, para a musica imediatamente
  - Se ligar, retoma a trilha automaticamente

## Sobre bibliotecas
Nenhuma biblioteca externa foi adicionada para audio.

- Nao foi usado JavaFX para a solucao final
- Nao foi usada biblioteca de terceiros para MP3
- A solucao final depende apenas de Java Sound (javax.sound.sampled) e arquivo WAV

## Motivo da conversao MP3 -> WAV
No ambiente atual, o suporte a MP3 nao estava disponivel nativamente.

Para evitar dependencia externa e manter o projeto simples, o audio foi convertido para WAV, formato suportado diretamente pelo Java Sound.

## Como trocar a musica
1. Substituir o arquivo resources/audio/music/GameMusic.wav por outro WAV
2. Manter o mesmo nome, ou atualizar o caminho em src/citycleaner/view/MainWindow.java
3. Recompilar e executar o jogo

## Validacao realizada
- Compilacao sem erros apos integracao
- Execucao sem erros de runtime
- Musica iniciando no menu e respeitando o botao de ligar/desligar
