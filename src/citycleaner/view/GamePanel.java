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
    private final Player player;
    private final List<Platform> platforms;
    private final List<TrashItem> trashItems;
    private final KeyboardController keyboardController;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private final int ecoScore;
    private final int completedSteps;
    private final int totalSteps;
    private final int pollutionLevel;
    private boolean running = true;
    private int currentLevel = 1;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;

    public GamePanel() {
        this(60, 0, 0, 1);
    }

    public GamePanel(int initialPollutionLevel) {
        this(initialPollutionLevel, 0, 0, 1);
    }

    public GamePanel(int initialPollutionLevel, int initialEcoScore, int initialCompletedSteps, int initialTotalSteps) {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        pollutionLevel = Math.max(0, Math.min(100, initialPollutionLevel));
        ecoScore = Math.max(0, initialEcoScore);
        totalSteps = Math.max(1, initialTotalSteps);
        completedSteps = Math.max(0, Math.min(totalSteps, initialCompletedSteps));
        player = new Player(100, 300);
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

        // O unico lixo real da cena fica na sacolinha branca apontada no print 2.
        items.add(new TrashItem(246, 324, 18, Constants.POINTS_ITEM));

        return items;
    }

    private List<Platform> createLevel(int level) {
        List<Platform> levelPlatforms = new ArrayList<>();

        if (level == 1) {
            // Keep only an invisible floor for physics/collision.
            levelPlatforms.add(new Platform(0, Constants.GAME_HEIGHT - 64, Constants.GAME_WIDTH, 64));
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
        drawPollutionBar(g2d);
        drawPlayer(g2d);
        drawHUD(g2d);
    }

    private void drawPollutionBar(Graphics2D g) {
        int barWidth = 34;
        int barHeight = Constants.GAME_HEIGHT - 80;
        int barX = Constants.WINDOW_WIDTH - 52;
        int barY = 26;

        g.setColor(new Color(16, 18, 28, 210));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        int fillHeight = (int) (barHeight * (pollutionLevel / 100.0));
        int fillY = barY + barHeight - fillHeight;

        Color fillColor = pollutionLevel >= 80 ? new Color(208, 52, 52) : new Color(236, 196, 68);
        if (pollutionLevel <= 40) {
            fillColor = new Color(64, 175, 89);
        }

        g.setColor(fillColor);
        g.fillRoundRect(barX + 4, fillY + 4, barWidth - 8, Math.max(0, fillHeight - 8), 10, 10);

        g.setColor(new Color(235, 235, 240));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString("P", barX + 11, barY - 8);
        g.drawString(String.valueOf(pollutionLevel) + "%", barX - 14, barY + barHeight + 18);
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

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(Constants.COLOR_HUD));
        g.fillRect(0, Constants.GAME_HEIGHT, Constants.WINDOW_WIDTH, Constants.HUD_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        g.drawString("FASE 2 - COLETANDO O LIXO", 20, Constants.GAME_HEIGHT + 30);

        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.drawString("Progresso: 1/1", 20, Constants.GAME_HEIGHT + 55);
        g.drawString("Eco score: " + ecoScore, 280, Constants.GAME_HEIGHT + 55);
        g.drawString("Poluição: " + pollutionLevel + "%", 460, Constants.GAME_HEIGHT + 55);
    }
}
