import citycleaner.view.GamePanel;
import citycleaner.util.Constants;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class RenderGamePanelPreview {
    public static void main(String[] args) throws Exception {
        GamePanel panel = new GamePanel();
        panel.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        Thread.sleep(150);
        BufferedImage image = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);
        g2.dispose();
        ImageIO.write(image, "png", new File("preview-gamepanel.png"));
        System.exit(0);
    }
}
