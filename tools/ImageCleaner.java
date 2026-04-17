import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageCleaner {
    public static void main(String[] args) throws Exception {
        String input = null;
        String output = null;
        List<Rectangle> rects = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("--input") || a.equals("-i")) {
                input = args[++i];
            } else if (a.equals("--output") || a.equals("-o")) {
                output = args[++i];
            } else if (a.equals("--rect")) {
                String[] p = args[++i].split(",");
                if (p.length != 4) throw new IllegalArgumentException("Rect must be x1,y1,x2,y2");
                int x1 = Integer.parseInt(p[0]);
                int y1 = Integer.parseInt(p[1]);
                int x2 = Integer.parseInt(p[2]);
                int y2 = Integer.parseInt(p[3]);
                rects.add(new Rectangle(x1, y1, x2 - x1, y2 - y1));
            }
        }

        if (input == null || output == null || rects.isEmpty()) {
            System.err.println("Usage: java ImageCleaner --input in.png --output out.png --rect x1,y1,x2,y2 [--rect ...]");
            return;
        }

        BufferedImage img = ImageIO.read(new File(input));
        if (img == null) throw new RuntimeException("Failed to load image: " + input);

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        for (Rectangle r : rects) {
            int x = r.x, y = r.y, w = r.width, h = r.height;
            System.out.println("Filling rect: " + x + "," + y + "," + (x+w) + "," + (y+h));

            // Choose source sample box: right, left, or above.
            Rectangle src;
            if (x + w + w <= imgW) {
                src = new Rectangle(x + w, Math.max(0, y - h/4), w, Math.min(imgH - 1, y + h + h/4) - Math.max(0, y - h/4));
            } else if (x - w >= 0) {
                src = new Rectangle(x - w, Math.max(0, y - h/4), w, Math.min(imgH - 1, y + h + h/4) - Math.max(0, y - h/4));
            } else {
                src = new Rectangle(Math.max(0, x - w/2), Math.max(0, y - h), Math.min(imgW, x + w/2) - Math.max(0, x - w/2), Math.min(imgH, y) - Math.max(0, y - h));
            }

            // Clip src
            src.x = Math.max(0, Math.min(imgW - 1, src.x));
            src.y = Math.max(0, Math.min(imgH - 1, src.y));
            src.width = Math.max(1, Math.min(imgW - src.x, src.width));
            src.height = Math.max(1, Math.min(imgH - src.y, src.height));

            BufferedImage patch = img.getSubimage(src.x, src.y, src.width, src.height);

            // Scale patch to rect size using nearest neighbor for pixel art
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(patch, 0, 0, w, h, null);
            g.dispose();

            // Paste scaled patch into target rect
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(scaled, x, y, null);
            g2.dispose();
        }

        ImageIO.write(img, "png", new File(output));
        System.out.println("Saved cleaned image to: " + output);
    }
}
