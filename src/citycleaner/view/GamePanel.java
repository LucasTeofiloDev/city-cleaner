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
    private static final int PLAYER_START_X = 250;
    private static final int SIDEWALK_PLATFORM_Y = 456;
    private static final int SIDEWALK_PLATFORM_HEIGHT = Constants.GAME_HEIGHT - SIDEWALK_PLATFORM_Y;
    private static final int PLAYER_SPRITE_DRAW_OFFSET_Y = 0;
    private static final int POLLUTION_LEVEL = 70;
    private static final String HUD_PHASE_NAME = "FASE 2 - COLETA URBANA";

    private final Player player;
    private final List<Platform> platforms;
    private final List<TrashItem> trashItems;
    private final KeyboardController keyboardController;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private boolean running = true;
    private int currentLevel = 1;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        // Spawn the player directly on the sidewalk plane.
        player = new Player(PLAYER_START_X, SIDEWALK_PLATFORM_Y - Constants.PLAYER_HEIGHT);
        playerSpriteOne = ResourceLoader.loadImage("sprites/Personagem1.png");
        playerSpriteTwo = ResourceLoader.loadImage("sprites/Personagem2.png");

        trashItems = createTrashItems();
        platforms = createLevel(currentLevel);

        keyboardController = new KeyboardController(player);
        addKeyListener(keyboardController);

        startGameLoop();
    }

    private List<TrashItem> createTrashItems() {
        List<TrashItem> items = new ArrayList<>();

        // Keep collectible zones only on visible sidewalk clutter.
        items.add(new TrashItem(120, 436, 18, Constants.POINTS_ITEM));
        items.add(new TrashItem(270, 438, 20, Constants.POINTS_ITEM));
        items.add(new TrashItem(470, 430, 20, Constants.POINTS_ITEM));
        items.add(new TrashItem(628, 430, 20, Constants.POINTS_ITEM));
        items.add(new TrashItem(882, 434, 20, Constants.POINTS_ITEM));
        items.add(new TrashItem(1088, 432, 18, Constants.POINTS_ITEM));

        return items;
    }

    private List<Platform> createLevel(int level) {
        List<Platform> levelPlatforms = new ArrayList<>();

        if (level == 1) {
            // Only keep a single collision plane aligned with the visible sidewalk.
            levelPlatforms.add(new Platform(0, SIDEWALK_PLATFORM_Y, Constants.GAME_WIDTH, SIDEWALK_PLATFORM_HEIGHT));

            // Elevated platforms were removed from both gameplay and the scene art.
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
        updatePlayerAnimation(Math.abs(player.getVelX()) > 0.01f);
        collectTrashItems();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BackgroundRenderer.draw(g2d, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        drawPlayer(g2d);
        drawPollutionBar(g2d);
        drawHUD(g2d);
    }

    private void collectTrashItems() {
        Rectangle playerBounds = getCollectionBounds();

        for (TrashItem trashItem : trashItems) {
            if (trashItem.isNear(playerBounds, 12)) {
                trashItem.collect();
                player.collectItem(trashItem.getPoints());
            }
        }
    }

    private Rectangle getCollectionBounds() {
        int collectionX = (int) player.getX() + 48;
        int collectionY = (int) player.getY() + 110;
        int collectionWidth = 68;
        int collectionHeight = 110;
        return new Rectangle(collectionX, collectionY, collectionWidth, collectionHeight);
    }

    private void drawPlayer(Graphics2D g) {
        BufferedImage currentSprite = useFirstSprite ? playerSpriteOne : playerSpriteTwo;
        if (currentSprite != null) {
            g.drawImage(
                currentSprite,
                (int) player.getX(),
                (int) player.getY(),
                Constants.PLAYER_WIDTH,
                Constants.PLAYER_HEIGHT,
                null
            );
            return;
        }

        Stroke previousStroke = g.getStroke();
        g.setColor(new Color(255, 128, 0));
        g.fillRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        g.setColor(new Color(200, 100, 0));
        g.setStroke(new BasicStroke(2f));
        g.drawRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        g.setStroke(previousStroke);
    }

    private void updatePlayerAnimation(boolean moving) {
        if (!moving) {
            playerAnimationTick = 0;
            useFirstSprite = true;
            return;
        }

        playerAnimationTick++;
        if (playerAnimationTick >= 8) {
            playerAnimationTick = 0;
            useFirstSprite = !useFirstSprite;
        }
    }

    private void drawPollutionBar(Graphics2D g) {
        int barWidth = 44;
        int barHeight = Constants.GAME_HEIGHT - 86;
        int barX = Constants.WINDOW_WIDTH - 58;
        int barY = 26;

        g.setColor(new Color(16, 18, 28, 210));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        int fillHeight = (int) (barHeight * (POLLUTION_LEVEL / 100.0));
        int fillY = barY + barHeight - fillHeight;

        g.setColor(new Color(236, 196, 68));
        g.fillRoundRect(barX + 5, fillY + 5, barWidth - 10, Math.max(0, fillHeight - 10), 10, 10);

        g.setColor(new Color(235, 235, 240));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString("P", barX + 15, barY - 8);
        g.drawString(POLLUTION_LEVEL + "%", barX - 2, barY + barHeight + 18);
    }

    private int countCollectedTrashItems() {
        int collectedItems = 0;
        for (TrashItem trashItem : trashItems) {
            if (trashItem.isCollected()) {
                collectedItems++;
            }
        }
        return collectedItems;
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(Constants.COLOR_HUD));
        g.fillRect(0, Constants.GAME_HEIGHT, Constants.WINDOW_WIDTH, Constants.HUD_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        g.drawString(HUD_PHASE_NAME, 20, Constants.GAME_HEIGHT + 30);

        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.drawString("Progresso: " + countCollectedTrashItems() + "/" + trashItems.size(), 20, Constants.GAME_HEIGHT + 55);
        g.drawString("Eco score: " + player.getScore(), 280, Constants.GAME_HEIGHT + 55);
        g.drawString("Poluicao: " + POLLUTION_LEVEL + "%", 460, Constants.GAME_HEIGHT + 55);
        g.drawString("Mova-se com A/D ou setas", 690, Constants.GAME_HEIGHT + 55);
    }
}
