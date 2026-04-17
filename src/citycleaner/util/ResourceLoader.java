package citycleaner.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to load project resources (images, sounds).
 * Provides a robust method to load images from classpath or filesystem.
 */
public class ResourceLoader {

    /**
     * Loads an image given a relative path. Tries classpath first, then several
     * filesystem locations (project resources folder, working dir, etc.).
     *
     * @param relativePath path like "images/background.png" or "/images/background.png"
     * @return BufferedImage or null if not found or on error (exceptions are logged)
     */
    public static BufferedImage loadImage(String relativePath) {
        if (relativePath == null) return null;

        String normalized = relativePath.startsWith("/") ? relativePath : "/" + relativePath;

        // 1) Try classpath (works in IDE and when packaged as JAR)
        try (InputStream is = ResourceLoader.class.getResourceAsStream(normalized)) {
            if (is != null) {
                try {
                    return ImageIO.read(is);
                } catch (IOException e) {
                    System.err.println("ResourceLoader: failed to read image from classpath: " + normalized);
                    e.printStackTrace();
                }
            }
        } catch (IOException ignored) {
            // inputstream close exceptions ignored
        }

        // 2) Try common filesystem locations relative to working directory
        String userDir = System.getProperty("user.dir");
        String[] candidates = new String[] {
            // direct relative
            relativePath.replaceFirst("^/", ""),
            "resources/" + relativePath.replaceFirst("^/", ""),
            "./resources/" + relativePath.replaceFirst("^/", ""),
            "../resources/" + relativePath.replaceFirst("^/", ""),
            userDir + "/resources/" + relativePath.replaceFirst("^/", ""),
            userDir + "\\resources\\" + relativePath.replaceFirst("^/", ""),
            userDir + "/src/resources/" + relativePath.replaceFirst("^/", ""),
            userDir + "\\src\\resources\\" + relativePath.replaceFirst("^/", "")
        };

        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists() && f.isFile()) {
                try {
                    return ImageIO.read(f);
                } catch (IOException e) {
                    System.err.println("ResourceLoader: error reading file: " + f.getAbsolutePath());
                    e.printStackTrace();
                    return null;
                }
            }
        }

        System.out.println("ResourceLoader: image not found (checked candidates). Requested: " + relativePath);
        return null;
    }

    /**
     * Convenience method to specifically load the project's background image
     * located at /images/background.png (classpath) or resources/images/background.png (fs).
     */
    public static BufferedImage loadBackgroundImage() {
        String[] backgroundCandidates = new String[] {
            // Stage 2 should always prioritize the current scene art.
            "sprites/Fase2New.png",
            "resources/sprites/Fase2New.png",
            "images/Fase2New.png",
            "resources/images/Fase2New.png",

            // Previous phase 2 background kept as fallback.
            "sprites/Fase2.png",
            "resources/sprites/Fase2.png",
            "images/Fase2.png",
            "resources/images/Fase2.png",

            // Legacy background paths kept as fallback.
            "sprites/backgroundNew.png",
            "resources/sprites/backgroundNew.png",
            "images/backgroundNew.png",
            "resources/images/backgroundNew.png",

            // Older background paths kept as additional fallback.
            "images/background.png",
            "sprites/background.png",
            "resources/sprites/background.png"
        };

        for (String candidate : backgroundCandidates) {
            BufferedImage img = loadImage(candidate);
            if (img != null) {
                return img;
            }
        }

        // Not found
        return null;
    }
}
