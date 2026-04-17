package citycleaner.view.renderer;

import citycleaner.util.ResourceLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Background renderer that draws a PNG background image using Java2D.
 * Loads the image via ResourceLoader and draws it scaled. If the image
 * is not found, a light gradient fallback is drawn.
 */
public class BackgroundRenderer {
    // Load lazily so updated files show without restarting the JVM and to
    // avoid issues with working directory when the class is initialized.
    private static BufferedImage backgroundImage = null;


    /**
     * Draw the background. This should be called from `paintComponent` before
     * rendering game entities so the image stays behind everything.
     */
    public static void draw(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // If image not yet loaded, attempt to load now and log result.
        if (backgroundImage == null) {
            backgroundImage = ResourceLoader.loadBackgroundImage();
            if (backgroundImage != null) {
                System.out.println("[BackgroundRenderer] background image loaded on draw().");
            } else {
                System.out.println("[BackgroundRenderer] background image still not found on draw().");
            }
        }

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, width, height, null);
        } else {
            // Fallback gradient
            Paint gradient = new GradientPaint(0, 0, new Color(135, 206, 235), 0, height / 2, new Color(176, 224, 230));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, width, height);

            // Draw a visible debug text so user notices fallback
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("BACKGROUND IMAGE MISSING", 20, 30);
        }
    }
}
