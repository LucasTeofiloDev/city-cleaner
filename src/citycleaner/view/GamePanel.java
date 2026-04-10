package citycleaner.view;

import citycleaner.controller.KeyboardController;
import citycleaner.model.entity.Player;
import citycleaner.model.physics.PhysicsEngine;
import citycleaner.model.world.Platform;
import citycleaner.model.world.TrashItem;
import citycleaner.util.Constants;
import citycleaner.util.ResourceLoader;
import citycleaner.view.renderer.BackgroundRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel principal do jogo: renderizacao e game loop.
 */
public class GamePanel extends JPanel {
    private static final boolean SHOW_PLATFORM_OVERLAYS = false;
    private static final int PLAYER_ANIMATION_SPEED = 10;
    private static final Color PLAYER_BACKGROUND_KEY = new Color(247, 247, 247);
    private static final int PLAYER_BACKGROUND_TOLERANCE = 12;

    private final Player player;
    private final List<Platform> platforms;
    private final List<TrashItem> trashItems;
    private final KeyboardController keyboardController;
    private final BufferedImage[] playerFrames;
    private boolean running = true;
    private int currentLevel = 1;
    private int playerAnimationTick = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        player = new Player(100, 300);
        playerFrames = loadPlayerFrames();

        trashItems = createTrashItems();
        platforms = createLevel(currentLevel);

        keyboardController = new KeyboardController(player);
        addKeyListener(keyboardController);

        startGameLoop();
    }

    private List<TrashItem> createTrashItems() {
        List<TrashItem> items = new ArrayList<>();

        // O unico lixo real da cena fica na sacolinha branca apontada no print 2.
        items.add(new TrashItem(246, 324, 18, Constants.POINTS_ITEM));

        return items;
    }

    private List<Platform> createLevel(int level) {
        List<Platform> levelPlatforms = new ArrayList<>();

        if (level == 1) {
            for (int i = 0; i < Constants.GAME_WIDTH; i += 64) {
                levelPlatforms.add(new Platform(i, Constants.GAME_HEIGHT - 64, 64, 64));
            }

            levelPlatforms.add(new Platform(200, 450, 128, 32));
            levelPlatforms.add(new Platform(450, 380, 128, 32));
            levelPlatforms.add(new Platform(700, 310, 128, 32));
            levelPlatforms.add(new Platform(300, 250, 96, 32));
            levelPlatforms.add(new Platform(800, 250, 128, 32));
        }

        return levelPlatforms;
    }

    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            final long frameTimeNanos = 1_000_000_000L / Constants.FPS;

            while (running) {
                long currentTime = System.nanoTime();
                long deltaTime = currentTime - lastTime;

                if (deltaTime >= frameTimeNanos) {
                    update();
                    repaint();
                    lastTime = currentTime;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void update() {
        PhysicsEngine.update(player, platforms);
        updatePlayerAnimation();
        collectTrashItems();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BackgroundRenderer.draw(g2d, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        if (SHOW_PLATFORM_OVERLAYS) {
            drawPlatforms(g2d);
        }
        drawPlayer(g2d);
        drawHUD(g2d);
    }

    private void drawPlatforms(Graphics2D g) {
        Stroke previousStroke = g.getStroke();
        g.setColor(new Color(139, 69, 19));

        for (Platform platform : platforms) {
            g.fillRect(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());

            g.setColor(new Color(101, 50, 15));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
            g.setColor(new Color(139, 69, 19));
        }

        g.setStroke(previousStroke);
    }
    private void collectTrashItems() {
        Rectangle playerBounds = player.getBounds();

        for (TrashItem trashItem : trashItems) {
            if (trashItem.isNear(playerBounds, 28)) {
                trashItem.collect();
                player.collectItem(trashItem.getPoints());
            }
        }
    }

    private void drawPlayer(Graphics2D g) {
        if (playerFrames.length == 0) {
            drawFallbackPlayer(g);
            return;
        }

        BufferedImage currentFrame = playerFrames[0];
        if (playerFrames.length > 1 && player.isMovingHorizontally()) {
            currentFrame = playerFrames[(playerAnimationTick / PLAYER_ANIMATION_SPEED) % playerFrames.length];
        }

        int spriteWidth = Constants.PLAYER_WIDTH;
        int spriteHeight = Constants.PLAYER_HEIGHT;
        int spriteX = Math.round(player.getX());
        int spriteY = Math.round(player.getY());

        Object previousInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (player.getFacingDirection() < 0) {
            g.drawImage(currentFrame, spriteX + spriteWidth, spriteY, -spriteWidth, spriteHeight, null);
        } else {
            g.drawImage(currentFrame, spriteX, spriteY, spriteWidth, spriteHeight, null);
        }

        if (previousInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previousInterpolation);
        }
    }

    private void drawFallbackPlayer(Graphics2D g) {
        Stroke previousStroke = g.getStroke();

        g.setColor(new Color(255, 128, 0));
        g.fillRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);

        g.setColor(new Color(200, 100, 0));
        g.setStroke(new BasicStroke(2f));
        g.drawRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);

        g.setStroke(previousStroke);
    }

    private void updatePlayerAnimation() {
        if (playerFrames.length <= 1 || !player.isMovingHorizontally()) {
            playerAnimationTick = 0;
            return;
        }

        playerAnimationTick = (playerAnimationTick + 1) % (playerFrames.length * PLAYER_ANIMATION_SPEED);
    }

    private BufferedImage[] loadPlayerFrames() {
        BufferedImage frame1 = preparePlayerSprite(ResourceLoader.loadImage("sprites/p1.png"));
        BufferedImage frame2 = preparePlayerSprite(ResourceLoader.loadImage("sprites/p2.png"));

        if (frame1 != null && frame2 != null) {
            return new BufferedImage[] { frame1, frame2 };
        }
        if (frame1 != null) {
            return new BufferedImage[] { frame1 };
        }
        if (frame2 != null) {
            return new BufferedImage[] { frame2 };
        }

        return new BufferedImage[0];
    }

    private BufferedImage preparePlayerSprite(BufferedImage source) {
        if (source == null) {
            return null;
        }

        BufferedImage transparent = applyTransparencyKey(
            source,
            PLAYER_BACKGROUND_KEY,
            PLAYER_BACKGROUND_TOLERANCE
        );
        return cropTransparentBorders(transparent);
    }

    private BufferedImage applyTransparencyKey(BufferedImage source, Color keyColor, int tolerance) {
        BufferedImage transparent = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                Color pixel = new Color(source.getRGB(x, y), true);
                if (isWithinTolerance(pixel, keyColor, tolerance)) {
                    transparent.setRGB(x, y, 0x00000000);
                } else {
                    transparent.setRGB(x, y, source.getRGB(x, y));
                }
            }
        }

        return transparent;
    }

    private boolean isWithinTolerance(Color pixel, Color keyColor, int tolerance) {
        return Math.abs(pixel.getRed() - keyColor.getRed()) <= tolerance
            && Math.abs(pixel.getGreen() - keyColor.getGreen()) <= tolerance
            && Math.abs(pixel.getBlue() - keyColor.getBlue()) <= tolerance;
    }

    private BufferedImage cropTransparentBorders(BufferedImage source) {
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int croppedWidth = maxX - minX + 1;
        int croppedHeight = maxY - minY + 1;
        BufferedImage cropped = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cropped.createGraphics();
        graphics.drawImage(
            source,
            0,
            0,
            croppedWidth,
            croppedHeight,
            minX,
            minY,
            maxX + 1,
            maxY + 1,
            null
        );
        graphics.dispose();
        return cropped;
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(Constants.COLOR_HUD));
        g.fillRect(0, Constants.GAME_HEIGHT, Constants.WINDOW_WIDTH, Constants.HUD_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        String scoreText = "SCORE: " + player.getScore();
        g.drawString(scoreText, 20, Constants.GAME_HEIGHT + 45);

        String livesText = "LIVES: " + player.getLives();
        g.drawString(livesText, Constants.WINDOW_WIDTH / 2 - 50, Constants.GAME_HEIGHT + 45);

        String levelText = "LEVEL " + currentLevel;
        g.drawString(levelText, Constants.WINDOW_WIDTH - 200, Constants.GAME_HEIGHT + 45);
    }
}
