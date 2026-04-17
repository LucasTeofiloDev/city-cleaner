"""
Script simples para limpar/regenerar áreas do background usando Pillow.
Ele substitui retângulos especificados por uma amostra de grama próxima para manter o estilo.

Uso:
  python tools/clean_background.py --input resources/sprites/CenarioFase1_2.png \
      --output resources/sprites/CenarioFase1_2_clean.png \
      --rect 50,120,420,380 --rect 920,420,1020,520

Os valores de retângulo são x1,y1,x2,y2 (coordenadas de pixels na imagem original).
Ajuste os retângulos conforme necessário e reexecute até o resultado ficar satisfatório.

Dependências:
  pip install pillow

Observação: este script faz uma substituição simples (preenchimento por patch de amostra). Para resultados de pixel art mais refinados,
recomendo editar manualmente com um editor de imagens (Aseprite, Photoshop, GIMP) ou fornecer as coordenadas exatas e um patch de textura.
"""

from PIL import Image
import argparse
import sys


def parse_rect(s):
    parts = s.split(',')
    if len(parts) != 4:
        raise argparse.ArgumentTypeError('Rect must be x1,y1,x2,y2')
    return tuple(int(p) for p in parts)


def sample_patch(img, sample_box, target_size):
    patch = img.crop(sample_box)
    patch = patch.resize(target_size)
    return patch


def fill_rect_with_patch(img, rect, patch_source_box=None):
    x1, y1, x2, y2 = rect
    w = x2 - x1
    h = y2 - y1

    # If no explicit source provided, pick sample to the right of the rect if possible,
    # otherwise to the left, otherwise above.
    if patch_source_box is None:
        img_w, img_h = img.size
        # try right
        if x2 + w <= img_w:
            src = (x2, max(0, y1 - h//4), x2 + w, min(img_h, y2 + h//4))
        # try left
        elif x1 - w >= 0:
            src = (x1 - w, max(0, y1 - h//4), x1, min(img_h, y2 + h//4))
        # else sample above
        else:
            src = (max(0, x1 - w//2), max(0, y1 - h), min(img_w, x2 + w//2), y1)
    else:
        src = patch_source_box

    # Clip source to image bounds
    img_w, img_h = img.size
    sx1 = max(0, min(img_w, src[0]))
    sy1 = max(0, min(img_h, src[1]))
    sx2 = max(0, min(img_w, src[2]))
    sy2 = max(0, min(img_h, src[3]))

    src_box = (sx1, sy1, sx2, sy2)
    if sx2 <= sx1 or sy2 <= sy1:
        return

    patch = sample_patch(img, src_box, (w, h))
    img.paste(patch, (x1, y1))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', '-i', required=True)
    parser.add_argument('--output', '-o', required=True)
    parser.add_argument('--rect', action='append', type=parse_rect,
                        help='Rect to clean: x1,y1,x2,y2 (can repeat)')
    args = parser.parse_args()

    if not args.rect:
        print('No rects provided. Nothing to do.')
        sys.exit(1)

    img = Image.open(args.input).convert('RGBA')

    for rect in args.rect:
        print(f'Filling rect {rect}...')
        fill_rect_with_patch(img, rect)

    img.save(args.output)
    print('Saved cleaned image to', args.output)


if __name__ == '__main__':
    main()
