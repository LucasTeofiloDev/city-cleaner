package citycleaner.util;

import citycleaner.model.world.TrashItem;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Detecta pontos visuais salientes no fundo para virar lixo coletavel.
 * A heuristica prioriza objetos pequenos e contrastantes na metade inferior
 * da cena. Se nao encontrar pontos suficientes, usa marcadores de fallback.
 */
public final class TrashDetector {
    private static final int SMALL_RADIUS = 4;
    private static final int LARGE_RADIUS = 14;
    private static final int SAMPLE_STEP = 12;
    private static final int MIN_DISTANCE_BETWEEN_MARKERS = 80;
    private static final int DESIRED_MARKERS = 6;
    private static final int MIN_AUTO_MARKERS = 4;

    private TrashDetector() {
    }

    public static List<TrashItem> detectTrashItems(int targetWidth, int targetHeight) {
        BufferedImage rawBackground = ResourceLoader.loadBackgroundImage();
        if (rawBackground == null) {
            System.out.println("[TrashDetector] background image not found. Using fallback trash markers.");
            return createFallbackItems(targetWidth, targetHeight);
        }

        BufferedImage scaledBackground = scaleToGameSize(rawBackground, targetWidth, targetHeight);
        int[] pixels = scaledBackground.getRGB(0, 0, targetWidth, targetHeight, null, 0, targetWidth);
        List<Candidate> candidates = collectCandidates(pixels, targetWidth, targetHeight);
        List<TrashItem> items = chooseBestCandidates(candidates);

        if (items.size() < MIN_AUTO_MARKERS) {
            addFallbackItems(items, targetWidth, targetHeight);
        }

        System.out.println("[TrashDetector] trash markers ready: " + items.size());
        return items;
    }

    private static BufferedImage scaleToGameSize(BufferedImage image, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    private static List<Candidate> collectCandidates(int[] pixels, int width, int height) {
        List<Candidate> candidates = new ArrayList<>();
        int startY = Math.max(height / 3, 170);
        int endY = height - 28;

        for (int y = startY; y < endY; y += SAMPLE_STEP) {
            for (int x = 24; x < width - 24; x += SAMPLE_STEP) {
                ColorSample center = sampleColor(pixels, width, x, y);
                ColorSample smallAverage = averageColor(pixels, width, height, x, y, SMALL_RADIUS);
                ColorSample largeAverage = averageColor(pixels, width, height, x, y, LARGE_RADIUS);

                int contrast = colorDistance(smallAverage, largeAverage);
                int saturation = center.saturation();
                int brightness = center.brightness();

                if (contrast < 85) {
                    continue;
                }
                if (brightness > 235) {
                    continue;
                }
                if (isSkyLike(center) && y < height - 100) {
                    continue;
                }
                if (isGreenArea(center)) {
                    continue;
                }
                if (isUniformGround(center) && contrast < 120) {
                    continue;
                }

                double verticalBonus = (double) y / height * 20.0;
                double score = contrast + (saturation * 0.45) + verticalBonus;
                candidates.add(new Candidate(x, y, score));
            }
        }

        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed());
        return candidates;
    }

    private static List<TrashItem> chooseBestCandidates(List<Candidate> candidates) {
        List<TrashItem> items = new ArrayList<>();

        for (Candidate candidate : candidates) {
            if (isFarEnough(candidate.x(), candidate.y(), items)) {
                items.add(new TrashItem(candidate.x(), candidate.y()));
            }

            if (items.size() >= DESIRED_MARKERS) {
                break;
            }
        }

        return items;
    }

    private static boolean isFarEnough(int x, int y, List<TrashItem> items) {
        for (TrashItem item : items) {
            int dx = item.getCenterX() - x;
            int dy = item.getCenterY() - y;
            if ((dx * dx) + (dy * dy) < MIN_DISTANCE_BETWEEN_MARKERS * MIN_DISTANCE_BETWEEN_MARKERS) {
                return false;
            }
        }
        return true;
    }

    private static void addFallbackItems(List<TrashItem> items, int width, int height) {
        for (TrashItem fallback : createFallbackItems(width, height)) {
            if (isFarEnough(fallback.getCenterX(), fallback.getCenterY(), items)) {
                items.add(fallback);
            }

            if (items.size() >= DESIRED_MARKERS) {
                break;
            }
        }
    }

    private static List<TrashItem> createFallbackItems(int width, int height) {
        List<TrashItem> fallbackItems = new ArrayList<>();
        int[][] points = new int[][] {
            {width / 8, height - 90},
            {width / 4, height - 150},
            {width / 2 - 30, height - 210},
            {(width * 2) / 3, height - 280},
            {width / 3, height - 340},
            {(width * 3) / 4, height - 340}
        };

        for (int[] point : points) {
            fallbackItems.add(new TrashItem(point[0], point[1]));
        }

        return fallbackItems;
    }

    private static ColorSample averageColor(int[] pixels, int width, int height, int centerX, int centerY, int radius) {
        int startX = Math.max(0, centerX - radius);
        int endX = Math.min(width - 1, centerX + radius);
        int startY = Math.max(0, centerY - radius);
        int endY = Math.min(height - 1, centerY + radius);

        long red = 0;
        long green = 0;
        long blue = 0;
        int count = 0;

        for (int y = startY; y <= endY; y++) {
            int rowOffset = y * width;
            for (int x = startX; x <= endX; x++) {
                int rgb = pixels[rowOffset + x];
                red += (rgb >> 16) & 0xFF;
                green += (rgb >> 8) & 0xFF;
                blue += rgb & 0xFF;
                count++;
            }
        }

        return new ColorSample((int) (red / count), (int) (green / count), (int) (blue / count));
    }

    private static ColorSample sampleColor(int[] pixels, int width, int x, int y) {
        int rgb = pixels[(y * width) + x];
        return new ColorSample((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private static int colorDistance(ColorSample first, ColorSample second) {
        return Math.abs(first.red() - second.red())
            + Math.abs(first.green() - second.green())
            + Math.abs(first.blue() - second.blue());
    }

    private static boolean isSkyLike(ColorSample color) {
        return color.blue() > color.red() + 20 && color.blue() > color.green() + 8 && color.green() >= color.red();
    }

    private static boolean isGreenArea(ColorSample color) {
        return color.green() > color.red() + 15 && color.green() > color.blue() + 10;
    }

    private static boolean isUniformGround(ColorSample color) {
        return color.red() > 90
            && color.green() > 50
            && color.green() < color.red()
            && color.blue() < color.green() - 10;
    }

    private record Candidate(int x, int y, double score) {
    }

    private record ColorSample(int red, int green, int blue) {
        int brightness() {
            return (red + green + blue) / 3;
        }

        int saturation() {
            int max = Math.max(red, Math.max(green, blue));
            int min = Math.min(red, Math.min(green, blue));
            return max - min;
        }
    }
}
