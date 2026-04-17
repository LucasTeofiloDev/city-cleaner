import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class GenerateCleanBackground {
    private static final int OUTPUT_WIDTH = 1152;
    private static final int OUTPUT_HEIGHT = 576;

    private record Patch(int srcX, int srcY, int width, int height, int dstX, int dstY, int feather) {
    }

    public static void main(String[] args) throws IOException {
        File input = new File("resources/sprites/Fase2New.png");
        File output = new File("resources/sprites/Fase2New.png");

        BufferedImage source = ImageIO.read(input);
        if (source == null) {
            throw new IOException("Could not read " + input.getAbsolutePath());
        }

        BufferedImage scaled = scaleToGameSize(source, OUTPUT_WIDTH, OUTPUT_HEIGHT);
        BufferedImage cleaned = copyImage(scaled);

        List<Patch> patches = List.of(
            // Upper-left hillside and dead branches.
            new Patch(152, 156, 142, 124, 296, 168, 16),
            // Left tree cluster.
            new Patch(52, 242, 154, 148, 170, 246, 16),
            // Ground in front of the house.
            new Patch(624, 344, 176, 92, 432, 338, 16),
            // Marsh / right-mid platform area.
            new Patch(852, 300, 170, 112, 676, 246, 16),
            // Overgrown right-side upper area.
            new Patch(702, 162, 182, 136, 874, 180, 18),
            // Lower-left sidewalk clutter: grass strip.
            new Patch(844, 378, 178, 36, 168, 388, 12),
            // Lower-left sidewalk clutter: pavement strip.
            new Patch(786, 432, 178, 52, 168, 424, 12)
        );

        for (Patch patch : patches) {
            blendPatch(scaled, cleaned, patch);
        }

        ImageIO.write(cleaned, "png", output);
        System.out.println("Saved cleaned background to " + output.getAbsolutePath());
    }

    private static BufferedImage scaleToGameSize(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(source, 0, 0, null);
        g2.dispose();
        return copy;
    }

    private static void blendPatch(BufferedImage source, BufferedImage destination, Patch patch) {
        int maxX = Math.min(patch.width(), Math.min(source.getWidth() - patch.srcX(), destination.getWidth() - patch.dstX()));
        int maxY = Math.min(patch.height(), Math.min(source.getHeight() - patch.srcY(), destination.getHeight() - patch.dstY()));

        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                float alpha = edgeAlpha(x, y, maxX, maxY, patch.feather());
                if (alpha <= 0f) {
                    continue;
                }

                int srcRgb = source.getRGB(patch.srcX() + x, patch.srcY() + y);
                int dstX = patch.dstX() + x;
                int dstY = patch.dstY() + y;
                int dstRgb = destination.getRGB(dstX, dstY);
                destination.setRGB(dstX, dstY, blend(srcRgb, dstRgb, alpha));
            }
        }
    }

    private static float edgeAlpha(int x, int y, int width, int height, int feather) {
        if (feather <= 0) {
            return 1f;
        }

        int distanceToEdge = Math.min(
            Math.min(x, width - 1 - x),
            Math.min(y, height - 1 - y)
        );

        if (distanceToEdge >= feather) {
            return 1f;
        }

        return Math.max(0f, distanceToEdge / (float) feather);
    }

    private static int blend(int srcRgb, int dstRgb, float alpha) {
        int srcA = (srcRgb >>> 24) & 0xFF;
        int srcR = (srcRgb >>> 16) & 0xFF;
        int srcG = (srcRgb >>> 8) & 0xFF;
        int srcB = srcRgb & 0xFF;

        int dstA = (dstRgb >>> 24) & 0xFF;
        int dstR = (dstRgb >>> 16) & 0xFF;
        int dstG = (dstRgb >>> 8) & 0xFF;
        int dstB = dstRgb & 0xFF;

        int outA = lerp(dstA, srcA, alpha);
        int outR = lerp(dstR, srcR, alpha);
        int outG = lerp(dstG, srcG, alpha);
        int outB = lerp(dstB, srcB, alpha);

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private static int lerp(int start, int end, float alpha) {
        return Math.round(start + ((end - start) * alpha));
    }
}
