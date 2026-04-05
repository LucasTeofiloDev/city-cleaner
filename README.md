# City Cleaner — Uso do background por imagem

Este repositório contém um protótipo de jogo 2D em Java (Java2D + Swing) organizado em MVC.

Resumo das mudanças implementadas:
- `ResourceLoader` — método robusto para carregar imagens do classpath ou filesystem.
- `BackgroundRenderer` — desenha a imagem de background com `Graphics2D`.
- `GamePanel` — chama `BackgroundRenderer.draw(...)` antes de desenhar elementos do jogo.

Onde colocar sua imagem de background
- Coloque um PNG em `resources/images/background.png` para o caminho canônico.
- O projeto também aceita `resources/sprites/background.png` (compatibilidade com versão anterior).
- Recomendações: PNG, 1152x576 px (opcional — a imagem será escalada).

Estrutura de pastas relevante:
```
city-cleaner/
├─ src/
│  └─ citycleaner/
│     ├─ Main.java
│     ├─ controller/
│     ├─ model/
│     ├─ util/
│     │  └─ ResourceLoader.java
│     └─ view/
│        ├─ GamePanel.java
│        └─ renderer/
│           └─ BackgroundRenderer.java
└─ resources/
   ├─ images/
   │  └─ background.png   <-- preferencial
   └─ sprites/
      └─ background.png   <-- também aceito
```

Como compilar e rodar (terminal PowerShell):

```powershell
cd c:\Users\Lucas\Desktop\city-cleaner\src
javac -d ..\bin -sourcepath . (Get-ChildItem -Recurse -Include "*.java").FullName
cd ..
java -cp bin citycleaner.Main
```

Como rodar no VSCode:
1. Abra a pasta `city-cleaner` no VSCode.
2. Instale a extensão Java (Extension Pack for Java) se necessário.
3. Coloque a imagem em `resources/images/background.png`.
4. Abra `src/citycleaner/Main.java` e clique em Run/Debug ou use o terminal integrado com os comandos acima.

Observações técnicas
- A imagem é carregada via `ResourceLoader.loadBackgroundImage()`; `BackgroundRenderer.draw()` usa `Graphics2D.drawImage(...)` para desenhar redimensionada.
- O `Model` não contém recursos visuais — a imagem pertence à `view`.
- Em caso de falha no carregamento, um fallback gradiente é desenhado e uma mensagem é impressa no console.

Se quiser, eu posso:
- empacotar `resources/images/background.png` no classpath para gerar um JAR executável,
- ou gerar uma imagem de exemplo diferente e adicioná-la automaticamente ao projeto.