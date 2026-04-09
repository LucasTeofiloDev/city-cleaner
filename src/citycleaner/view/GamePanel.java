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
    private final List<Point> teleportMarkers;
    private final List<TrashItem> trashItems;
    private final KeyboardController keyboardController;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private boolean running = true;
    private int currentLevel = 1;
    private int nextTeleportIndex = 0;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        player = new Player(100, 300);
        playerSpriteOne = ResourceLoader.loadImage("sprites/Personagem1.png");
        playerSpriteTwo = ResourceLoader.loadImage("sprites/Personagem2.png");

        teleportMarkers = createTeleportMarkers();
        trashItems = createTrashItems();
        platforms = createLevel(currentLevel);

        keyboardController = new KeyboardController(player, this::teleportToNextMarker);
        addKeyListener(keyboardController);

        startGameLoop();
    }

    private List<Point> createTeleportMarkers() {
        List<Point> markers = new ArrayList<>();

        // Pontos definidos manualmente com base no print enviado pelo usuario.
        markers.add(new Point(246, 324)); // sacolinha branca ao lado da lixeira
        markers.add(new Point(348, 232)); // plataforma da casinha
        markers.add(new Point(864, 232)); // plataforma superior direita
        markers.add(new Point(764, 292)); // plataforma direita do meio
        markers.add(new Point(514, 362)); // plataforma central
        markers.add(new Point(264, 432)); // plataforma inferior esquerda

        return markers;
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
        updatePlayerAnimation(Math.abs(player.getVelX()) > 0.01f);
        collectTrashItems();
    }

    private void teleportToNextMarker() {
        if (teleportMarkers.isEmpty()) {
            return;
        }

        Point target = teleportMarkers.get(nextTeleportIndex);
        player.teleportTo(target.x - (Constants.PLAYER_WIDTH / 2), target.y - Constants.PLAYER_HEIGHT);
        nextTeleportIndex = (nextTeleportIndex + 1) % teleportMarkers.size();
        collectTrashItems();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BackgroundRenderer.draw(g2d, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        drawPlatforms(g2d);
        drawTeleportMarkers(g2d);
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

    private void drawTeleportMarkers(Graphics2D g) {
        Stroke previousStroke = g.getStroke();

        for (int i = 0; i < teleportMarkers.size(); i++) {
            Point marker = teleportMarkers.get(i);
            int radius = i == nextTeleportIndex ? 24 : 22;
            int diameter = radius * 2;
            int x = marker.x - radius;
            int y = marker.y - radius;

            g.setColor(new Color(255, 0, 0, i == nextTeleportIndex ? 90 : 60));
            g.fillOval(x, y, diameter, diameter);

            g.setColor(new Color(220, 20, 20, 220));
            g.setStroke(new BasicStroke(i == nextTeleportIndex ? 4f : 3f));
            g.drawOval(x, y, diameter, diameter);
            g.drawLine(marker.x - 6, marker.y - 6, marker.x + 6, marker.y + 6);
            g.drawLine(marker.x - 6, marker.y + 6, marker.x + 6, marker.y - 6);
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
        g.setFont(new Font("Arial", Font.BOLD, 24));

        String scoreText = "SCORE: " + player.getScore();
        g.drawString(scoreText, 20, Constants.GAME_HEIGHT + 45);

        String livesText = "LIVES: " + player.getLives();
        g.drawString(livesText, Constants.WINDOW_WIDTH / 2 - 50, Constants.GAME_HEIGHT + 45);

        String levelText = "LEVEL " + currentLevel;
        g.drawString(levelText, Constants.WINDOW_WIDTH - 200, Constants.GAME_HEIGHT + 45);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("T ou E = teleporta para o proximo ponto", 20, Constants.GAME_HEIGHT + 66);
    }
}
