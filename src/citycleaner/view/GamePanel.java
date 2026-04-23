package citycleaner.view;

import citycleaner.controller.KeyboardController;
import citycleaner.model.entity.Player;
import citycleaner.model.physics.PhysicsEngine;
import citycleaner.model.world.Platform;
import citycleaner.model.world.TrashItem;
import citycleaner.util.Constants;
import citycleaner.util.ResourceLoader;
import citycleaner.view.phase2.PhaseTwoController;
import citycleaner.view.renderer.BackgroundRenderer;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Fase 2: coleta de lixo com tempo limitado.
 */
public class GamePanel extends JPanel {
    private static final long PHASE_DURATION_MS = 20_000L;
    private static final long RESULT_OVERLAY_MS = 3_600L;
    private static final int GOOD_ENDING_POLLUTION_THRESHOLD = 60;
    private static final int BIN_X = 530;
    private static final int BIN_Y = 356;
    private static final int BIN_WIDTH = 90;
    private static final int BIN_HEIGHT = 126;

    private final Player player;
    private final List<Platform> platforms;
    private final KeyboardController keyboardController;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private final BufferedImage trashSprite;
    private final BufferedImage binSprite;
    private final BufferedImage goodEndingSceneSprite;
    private final BufferedImage badEndingSceneSprite;
    private final int phaseOneScore;
    private final int pollutionLevel;
    private final JButton startPhaseButton;
    private final PhaseTwoController phaseTwoController;

    private boolean running = true;
    private int currentLevel = 2;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;
    private boolean showInstructions = true;
    private long phaseFinishedAtMs = -1L;

    public GamePanel() {
        this(60, 0, 0, 1);
    }

    public GamePanel(int initialPollutionLevel) {
        this(initialPollutionLevel, 0, 0, 1);
    }

    public GamePanel(int initialPollutionLevel, int initialEcoScore, int initialCompletedSteps, int initialTotalSteps) {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);
        setLayout(null);

        pollutionLevel = Math.max(0, Math.min(100, initialPollutionLevel));
        phaseOneScore = Math.max(0, initialEcoScore);

        player = new Player(60, Constants.GAME_HEIGHT - Constants.PLAYER_HEIGHT - 64);
        playerSpriteOne = ResourceLoader.loadImage("sprites/Personagem1.png");
        playerSpriteTwo = ResourceLoader.loadImage("sprites/Personagem2.png");
        BufferedImage loadedTrashSprite = ResourceLoader.loadImage("sprites/newBanana.png");
        if (loadedTrashSprite == null) {
            loadedTrashSprite = ResourceLoader.loadImage("sprites/banana.png");
        }
        if (loadedTrashSprite == null) {
            loadedTrashSprite = ResourceLoader.loadImage("sprites/banana.png.png");
        }
        trashSprite = loadedTrashSprite;

        BufferedImage loadedBinSprite = ResourceLoader.loadImage("sprites/lata.png");
        if (loadedBinSprite == null) {
            loadedBinSprite = ResourceLoader.loadImage("sprites/latao.png");
        }
        binSprite = loadedBinSprite;
        goodEndingSceneSprite = ResourceLoader.loadImage("sprites/Cena1Gemini.png");
        badEndingSceneSprite = ResourceLoader.loadImage("sprites/CenaFinalRuim.jpg");

        platforms = createLevel(currentLevel);

        keyboardController = new KeyboardController(player);
        addKeyListener(keyboardController);
        addKeyListener(new PhaseTwoInputController());

        phaseTwoController = new PhaseTwoController(
            player,
            new Rectangle(BIN_X, BIN_Y, BIN_WIDTH, BIN_HEIGHT),
            PHASE_DURATION_MS
        );
        phaseTwoController.initializeTrash(6);

        startPhaseButton = new JButton("Iniciar Fase 2");
        startPhaseButton.setFont(new Font("Dialog", Font.BOLD, 20));
        startPhaseButton.setFocusable(false);
        startPhaseButton.addActionListener(e -> startPhaseTwo());
        add(startPhaseButton);

        startGameLoop();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int width = 220;
        int height = 46;
        startPhaseButton.setBounds(
            (getWidth() - width) / 2,
            Constants.GAME_HEIGHT - 74,
            width,
            height
        );
    }

    private List<Platform> createLevel(int level) {
        List<Platform> levelPlatforms = new ArrayList<>();

        if (level == 2) {
            levelPlatforms.add(new Platform(0, Constants.GAME_HEIGHT - 64, Constants.GAME_WIDTH, 64));
        }

        return levelPlatforms;
    }

    private void startPhaseTwo() {
        if (!showInstructions) {
            return;
        }

        showInstructions = false;
        startPhaseButton.setVisible(false);
        phaseTwoController.startPhase();
        requestFocusInWindow();
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
        startPhaseButton.setVisible(showInstructions);

        if (showInstructions) {
            player.stopMoving();
            updatePlayerAnimation(false);
            return;
        }

        if (phaseTwoController.isPhaseFinished()) {
            if (phaseFinishedAtMs < 0L) {
                phaseFinishedAtMs = System.currentTimeMillis();
            }
            player.stopMoving();
            updatePlayerAnimation(false);
            return;
        }

        PhysicsEngine.update(player, platforms);
        updatePlayerAnimation(Math.abs(player.getVelX()) > 0.01f);
        phaseTwoController.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BackgroundRenderer.draw(g2d, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        drawBinArea(g2d);
        drawTrashItems(g2d);
        drawCarriedTrashHint(g2d);
        drawPlayer(g2d);
        drawCountdown(g2d);
        drawPhaseScore(g2d);
        drawPollutionBar(g2d);
        drawHUD(g2d);

        if (phaseTwoController.isScoreFeedbackVisible()) {
            drawScoreFeedback(g2d);
        }

        if (showInstructions) {
            drawInstructionsOverlay(g2d);
        }

        if (phaseTwoController.isPhaseFinished()) {
            if (isEndingSceneVisible()) {
                if (hasGoodEnding()) {
                    drawGoodEndingScene(g2d);
                } else {
                    drawBadEndingScene(g2d);
                }
            } else {
                drawResultOverlay(g2d);
            }
        }
    }

    private boolean hasGoodEnding() {
        return pollutionLevel < GOOD_ENDING_POLLUTION_THRESHOLD;
    }

    private boolean isEndingSceneVisible() {
        if (!phaseTwoController.isPhaseFinished() || phaseFinishedAtMs < 0L) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - phaseFinishedAtMs;
        return elapsed >= RESULT_OVERLAY_MS;
    }

    private void drawBinArea(Graphics2D g) {
        Rectangle bin = phaseTwoController.getBinBounds();

        if (binSprite != null) {
            drawImageContain(g, binSprite, bin.x, bin.y, bin.width, bin.height);
        } else {
            g.setColor(new Color(42, 60, 86, 170));
            g.fillRoundRect(bin.x, bin.y, bin.width, bin.height, 20, 20);
            g.setColor(new Color(220, 230, 245, 220));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bin.x, bin.y, bin.width, bin.height, 20, 20);

            int lidY = bin.y - 12;
            g.setColor(new Color(178, 190, 205, 220));
            g.fillRoundRect(bin.x + 12, lidY, bin.width - 24, 14, 8, 8);
        }

        g.setColor(new Color(240, 245, 255));
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString("JOGUE O LIXO AQUI", bin.x - 12, bin.y - 18);

        if (!showInstructions && phaseTwoController.isPhaseStarted() && !phaseTwoController.isPhaseFinished()) {
            Rectangle playerBounds = player.getBounds();
            if (phaseTwoController.hasCarriedTrash() && phaseTwoController.isPlayerNearBin(playerBounds)) {
                drawInteractionBubble(g, bin.x + (bin.width / 2), bin.y - 52, "E: Depositar lixo");
            }
        }
    }

    private void drawBadEndingScene(Graphics2D g) {
        if (badEndingSceneSprite != null) {
            g.drawImage(badEndingSceneSprite, 0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT, null);
        } else {
            g.setColor(new Color(25, 12, 12));
            g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        }

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(new Color(255, 210, 210));
        g.setFont(new Font("Dialog", Font.BOLD, 42));
        String title = "FINAL RUIM";
        FontMetrics titleMetrics = g.getFontMetrics();
        g.drawString(title, (Constants.WINDOW_WIDTH - titleMetrics.stringWidth(title)) / 2, 120);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.PLAIN, 24));
        String lineOne = "A poluição permaneceu alta e a cidade sofreu as consequências.";
        String lineTwo = "Tente novamente com escolhas mais sustentáveis.";
        FontMetrics textMetrics = g.getFontMetrics();
        g.drawString(lineOne, (Constants.WINDOW_WIDTH - textMetrics.stringWidth(lineOne)) / 2, 170);
        g.drawString(lineTwo, (Constants.WINDOW_WIDTH - textMetrics.stringWidth(lineTwo)) / 2, 206);
    }

    private void drawTrashItems(Graphics2D g) {
        for (TrashItem item : phaseTwoController.getTrashItems()) {
            int r = item.getRadius();
            int x = item.getCenterX() - r;
            int y = item.getCenterY() - r;
            int d = r * 2;

            if (trashSprite != null) {
                drawImageContain(g, trashSprite, x, y, d, d);
            } else {
                g.setColor(new Color(232, 232, 232, 235));
                g.fillOval(x, y, d, d);
                g.setColor(new Color(80, 85, 90, 240));
                g.setStroke(new BasicStroke(2f));
                g.drawOval(x, y, d, d);

                g.setColor(new Color(90, 110, 120));
                g.setFont(new Font("Dialog", Font.BOLD, 11));
                g.drawString("LIXO", x - 2, y - 6);
            }
        }

        if (!showInstructions && !phaseTwoController.hasCarriedTrash() && isPlayerNearAnyTrash() && !phaseTwoController.isPhaseFinished()) {
            Rectangle playerBounds = player.getBounds();
            int hintX = playerBounds.x + (playerBounds.width / 2);
            int hintY = playerBounds.y - 52;
            drawInteractionBubble(g, hintX, hintY, "E: Coletar lixo");
        }
    }

    private boolean isPlayerNearAnyTrash() {
        Rectangle playerBounds = player.getBounds();
        for (TrashItem item : phaseTwoController.getTrashItems()) {
            if (item.isNear(playerBounds, 28)) {
                return true;
            }
        }
        return false;
    }

    private void drawInteractionBubble(Graphics2D g, int centerX, int y, String text) {
        Font oldFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(text) + 24;
        int height = 34;
        int x = centerX - (width / 2);

        g.setColor(new Color(19, 24, 36, 225));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(new Color(230, 235, 245));
        g.drawRoundRect(x, y, width, height, 12, 12);

        Polygon tail = new Polygon(
            new int[] {centerX - 8, centerX + 8, centerX},
            new int[] {y + height - 2, y + height - 2, y + height + 10},
            3
        );
        g.setColor(new Color(19, 24, 36, 225));
        g.fillPolygon(tail);
        g.setColor(new Color(230, 235, 245));
        g.drawPolygon(tail);

        g.setColor(Color.WHITE);
        g.drawString(text, x + 12, y + 22);
        g.setFont(oldFont);
    }

    private void drawCarriedTrashHint(Graphics2D g) {
        if (!phaseTwoController.hasCarriedTrash() || showInstructions || phaseTwoController.isPhaseFinished()) {
            return;
        }

        int x = (int) player.getX() + (Constants.PLAYER_WIDTH / 2) - 12;
        int y = (int) player.getY() - 24;

        if (trashSprite != null) {
            drawImageContain(g, trashSprite, x, y, 24, 24);
            return;
        }

        g.setColor(new Color(245, 245, 245, 230));
        g.fillOval(x, y, 24, 24);
        g.setColor(new Color(60, 60, 60, 220));
        g.drawOval(x, y, 24, 24);
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("L", x + 8, y + 16);
    }

    private void drawImageContain(Graphics2D g, BufferedImage image, int x, int y, int width, int height) {
        double scale = Math.min(width / (double) image.getWidth(), height / (double) image.getHeight());
        int drawW = (int) Math.round(image.getWidth() * scale);
        int drawH = (int) Math.round(image.getHeight() * scale);
        int drawX = x + ((width - drawW) / 2);
        int drawY = y + ((height - drawH) / 2);
        g.drawImage(image, drawX, drawY, drawW, drawH, null);
    }

    private void drawCountdown(Graphics2D g) {
        if (showInstructions) {
            return;
        }

        int remainingSeconds = phaseTwoController.getRemainingSeconds();

        g.setColor(new Color(18, 22, 35, 210));
        g.fillRoundRect((Constants.WINDOW_WIDTH / 2) - 90, 16, 180, 42, 14, 14);
        g.setColor(new Color(230, 235, 245));
        g.drawRoundRect((Constants.WINDOW_WIDTH / 2) - 90, 16, 180, 42, 14, 14);
        g.setFont(new Font("Dialog", Font.BOLD, 22));

        Color textColor = remainingSeconds <= 5 ? new Color(255, 120, 120) : Color.WHITE;
        g.setColor(textColor);
        g.drawString("Tempo: " + remainingSeconds + "s", (Constants.WINDOW_WIDTH / 2) - 70, 45);
    }

    private void drawPhaseScore(Graphics2D g) {
        if (showInstructions) {
            return;
        }

        g.setColor(new Color(18, 22, 35, 210));
        g.fillRoundRect(20, 16, 220, 42, 14, 14);
        g.setColor(new Color(230, 235, 245));
        g.drawRoundRect(20, 16, 220, 42, 14, 14);
        g.setFont(new Font("Dialog", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("Score Fase 2: " + phaseTwoController.getScore(), 32, 44);
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

    private void drawScoreFeedback(Graphics2D g) {
        int x = BIN_X + 8;
        int y = BIN_Y - 30;
        g.setColor(new Color(88, 222, 120));
        g.setFont(new Font("Dialog", Font.BOLD, 24));
        g.drawString("+" + phaseTwoController.getScoreFeedbackValue(), x, y);
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
        g.drawString("FASE 2 - COLETANDO O LIXO", 20, Constants.GAME_HEIGHT + 36);

        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.drawString("Progresso: 1/1", 20, Constants.GAME_HEIGHT + 66);
        g.drawString("Poluição: " + pollutionLevel + "%", 260, Constants.GAME_HEIGHT + 66);
    }

    private void drawInstructionsOverlay(Graphics2D g) {
        int boxX = 130;
        int boxY = 84;
        int boxW = Constants.WINDOW_WIDTH - 260;
        int boxH = Constants.GAME_HEIGHT - 170;

        g.setColor(new Color(10, 16, 28, 222));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 24, 24);
        g.setColor(new Color(233, 239, 250));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 24, 24);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 34));
        g.drawString("Instruções da Fase 2", boxX + 24, boxY + 52);

        g.setFont(new Font("Dialog", Font.PLAIN, 20));
        g.drawString("Esta fase é baseada em tempo: você terá 20 segundos.", boxX + 24, boxY + 106);
        g.drawString("Objetivo: coletar o máximo de lixo e jogar na lixeira.", boxX + 24, boxY + 144);
        g.drawString("Cada lixo depositado corretamente aumenta a pontuação.", boxX + 24, boxY + 182);
        g.drawString("Quanto mais lixo, mais pontos.", boxX + 24, boxY + 220);

        g.setFont(new Font("Dialog", Font.BOLD, 22));
        g.setColor(new Color(255, 224, 130));
        g.drawString("Controles:", boxX + 24, boxY + 274);

        g.setFont(new Font("Dialog", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("- A / D para andar", boxX + 40, boxY + 306);
        g.drawString("- W para pular", boxX + 40, boxY + 338);
        g.drawString("- E para coletar e depositar lixo", boxX + 40, boxY + 370);

        g.setFont(new Font("Dialog", Font.PLAIN, 17));
        g.setColor(new Color(210, 220, 240));
        g.drawString("Clique em Iniciar Fase 2 ou pressione ENTER para começar.", boxX + 24, boxY + boxH - 18);
    }

    private void drawResultOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 235));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 42));
        int centerX = Constants.WINDOW_WIDTH / 2;

        String title = "Fase 2 Finalizada!";
        FontMetrics titleMetrics = g.getFontMetrics();
        g.drawString(title, centerX - (titleMetrics.stringWidth(title) / 2), 180);

        g.setFont(new Font("Dialog", Font.PLAIN, 30));
        int phaseTwoScore = phaseTwoController.getScore();
        int finalScore = phaseOneScore + phaseTwoScore;
        String phaseOneScoreText = "Pontuação Fase 1: " + phaseOneScore;
        String phaseTwoScoreText = "Pontuação Fase 2: " + phaseTwoScore;
        String finalScoreText = "Pontuação final: " + finalScore;
        FontMetrics scoreMetrics = g.getFontMetrics();
        g.drawString(phaseOneScoreText, centerX - (scoreMetrics.stringWidth(phaseOneScoreText) / 2), 246);
        g.drawString(phaseTwoScoreText, centerX - (scoreMetrics.stringWidth(phaseTwoScoreText) / 2), 286);

        g.setFont(new Font("Dialog", Font.BOLD, 36));
        g.setColor(new Color(255, 225, 130));
        FontMetrics finalScoreMetrics = g.getFontMetrics();
        g.drawString(finalScoreText, centerX - (finalScoreMetrics.stringWidth(finalScoreText) / 2), 338);

        g.setFont(new Font("Dialog", Font.PLAIN, 23));
        g.setColor(new Color(220, 228, 245));
        String tipOne = "Você concluiu os 20 segundos de coleta!";
        String tipTwo = "Quanto mais lixo coletado corretamente, maior o impacto positivo.";
        FontMetrics tipsMetrics = g.getFontMetrics();
        g.drawString(tipOne, centerX - (tipsMetrics.stringWidth(tipOne) / 2), 394);
        g.drawString(tipTwo, centerX - (tipsMetrics.stringWidth(tipTwo) / 2), 430);

        if (hasGoodEnding()) {
            g.setFont(new Font("Dialog", Font.PLAIN, 19));
            g.setColor(new Color(190, 240, 190));
            String nextText = "Final bom desbloqueado! Cena final em instantes...";
            FontMetrics nextMetrics = g.getFontMetrics();
            g.drawString(nextText, centerX - (nextMetrics.stringWidth(nextText) / 2), 470);
        }
    }

    private void drawGoodEndingScene(Graphics2D g) {
        if (goodEndingSceneSprite != null) {
            g.drawImage(goodEndingSceneSprite, 0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT, null);
        } else {
            g.setColor(new Color(10, 20, 18));
            g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        }

        g.setColor(new Color(0, 0, 0, 145));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(new Color(214, 255, 214));
        g.setFont(new Font("Dialog", Font.BOLD, 42));
        String title = "FINAL BOM";
        FontMetrics titleMetrics = g.getFontMetrics();
        g.drawString(title, (Constants.WINDOW_WIDTH - titleMetrics.stringWidth(title)) / 2, 120);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.PLAIN, 24));
        String lineOne = "A cidade respondeu bem às suas decisões!";
        String lineTwo = "Com menor poluição, o futuro pode ser mais sustentável.";
        FontMetrics textMetrics = g.getFontMetrics();
        g.drawString(lineOne, (Constants.WINDOW_WIDTH - textMetrics.stringWidth(lineOne)) / 2, 170);
        g.drawString(lineTwo, (Constants.WINDOW_WIDTH - textMetrics.stringWidth(lineTwo)) / 2, 206);
    }

    private class PhaseTwoInputController extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (showInstructions) {
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_E) {
                    startPhaseTwo();
                }
                return;
            }

            if (phaseTwoController.isPhaseFinished()) {
                return;
            }

            if (key == KeyEvent.VK_E) {
                phaseTwoController.interact();
            }
        }
    }
}
