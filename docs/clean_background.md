Instruções para limpar o background e ajustar áreas do mapa

Objetivo
- Remover elementos (pilhas de lixo e objetos) em áreas marcadas.
- Remover o chão indicado pela seta e substituir por grama/cenário limpo.
- Manter estética pixel art e paleta original.

Procedimento (rápido)
1) Instale Pillow:

   pip install pillow

2) Execute o script de limpeza fornecido para gerar uma cópia limpa da imagem:

   python tools/clean_background.py \
     --input resources/sprites/CenarioFase1_2.png \
     --output resources/sprites/CenarioFase1_2_clean.png \
     --rect 40,120,420,380 \
     --rect 880,420,1020,520

   - Ajuste os parâmetros `--rect x1,y1,x2,y2` até o resultado ficar satisfatório.

3) Reinicie o jogo; o `ResourceLoader` agora prefere `CenarioFase1_2_clean.png`.

Recomendações para pixel art
- Para qualidade máxima, abra `resources/sprites/CenarioFase1_2_clean.png` em um editor de pixel art
  (Aseprite, GrafX2 ou GIMP) e refine os detalhes manualmente.
- Evite gradientes suaves geradas automaticamente; prefira clonar texturas/tiles próximas.

Notas técnicas
- O script aplica um patch de amostra simples — não é um inpainting avançado.
- Se quiser que eu gere patches mais específicos, diga as coordenadas exatas das áreas a limpar
  (x1,y1,x2,y2 em pixels relativos à imagem original) e eu ajusto o script ou testo localmente.
