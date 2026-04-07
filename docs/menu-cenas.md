# Documentacao de Menu e Cenas - City Cleaner

## Resumo
Esta documentacao descreve como o menu inicial e as cenas (cutscenes) foram implementados no jogo.

O fluxo atual e:
1. Menu principal
2. Sequencia de cenas
3. Gameplay (jogo principal)

## Bibliotecas usadas
A implementacao usa apenas bibliotecas nativas do Java.

- javax.swing
- java.awt
- java.awt.image
- java.util (listas e timer de logica)

Nenhuma biblioteca externa foi adicionada para menu ou cenas.

## Arquivos principais
- src/citycleaner/view/MainWindow.java
- src/citycleaner/view/MenuPanel.java
- src/citycleaner/view/IntroScenePanel.java
- src/citycleaner/util/ResourceLoader.java

## Logica de navegacao entre telas
A classe MainWindow controla qual painel aparece na janela em cada etapa.

1. Ao abrir o jogo:
- MainWindow cria e exibe MenuPanel

2. Ao clicar em Iniciar jogo no menu:
- MainWindow troca o content pane para IntroScenePanel

3. Ao finalizar as cenas:
- IntroScenePanel chama callback que aciona MainWindow.startGame()
- MainWindow troca para GamePanel (jogo ja existente)

4. Ao clicar em Sair:
- MainWindow encerra a aplicacao

## Menu principal
### Painel
Arquivo: src/citycleaner/view/MenuPanel.java

### Imagem de fundo
- Caminho: resources/sprites/MenuImage.png
- Carregamento feito via ResourceLoader

### Botoes do menu
Foram adicionados 3 botoes no centro da tela:
1. Iniciar jogo
- Inicia a sequencia de cenas

2. Sair
- Fecha o jogo

3. Som: ligado/desligado
- Alterna o estado do audio global

### Posicionamento
O painel usa layout manual (setLayout(null)) e define os botoes em doLayout() para manter centralizacao proporcional ao tamanho da janela.

## Cenas (cutscene)
### Painel
Arquivo: src/citycleaner/view/IntroScenePanel.java

### Imagens usadas
- Cena 1: resources/sprites/Cena1Gemini.png
- Cena 2: resources/sprites/Cena2Gemini.png
- Cena 3: resources/sprites/Cena3Gemini.png

### Estrutura de dados
As cenas sao definidas em uma lista de SceneData contendo:
- Titulo da cena
- Caminho da imagem
- Linhas de texto da narrativa

### Efeito de texto digitado
- Implementado com javax.swing.Timer
- O texto aparece por palavra (estilo caixa de dialogo)
- A velocidade e controlada por constante de tempo (milissegundos)

### Botao de progressao
Existe um botao de acao na cutscene:
- Enquanto nao termina de digitar: botao desabilitado
- Quando termina:
  - Se ainda houver proxima cena: texto do botao = Proxima cena
  - Na ultima cena: texto do botao = Iniciar jogo

## Renderizacao visual
A renderizacao das cenas faz:
1. Desenho da imagem em tela cheia
2. Caixa de dialogo no rodape
3. Titulo da cena
4. Texto narrativo digitado

Se uma imagem nao carregar, o sistema desenha fundo de fallback (gradiente) e aviso visual.

## Carregamento de recursos
O ResourceLoader tenta:
1. Classpath
2. Caminhos relativos no projeto (resources/...)
3. Caminhos baseados em user.dir

Isso permite carregar sprites mesmo em diferentes formas de execucao local.

## Validacao funcional realizada
- Compilacao sem erros apos integracao do menu e cenas
- Execucao sem erros de runtime
- Troca correta de telas: menu -> cenas -> jogo
- Botoes de menu funcionando (iniciar, sair, som)
- Texto das cenas aparecendo com efeito de digitacao
