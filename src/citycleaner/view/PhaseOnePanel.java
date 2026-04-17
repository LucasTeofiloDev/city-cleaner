package citycleaner.view;

import citycleaner.controller.KeyboardController;
import citycleaner.model.entity.Player;
import citycleaner.model.physics.PhysicsEngine;
import citycleaner.model.world.Platform;
import citycleaner.util.Constants;
import citycleaner.util.ResourceLoader;
import citycleaner.view.renderer.BackgroundRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Fase 1: jogador se movimenta e toma decisoes de sustentabilidade.
 */
public class PhaseOnePanel extends JPanel {
    private static final int MARKER_TRIGGER_DISTANCE = 36;
    private static final int TRANSPORT_TRIGGER_DISTANCE = 145;
    private static final int POLLUTION_LIMIT = 100;
    private static final int BIKE_X = 460;
    private static final int CAR_X = 930;
    private static final int BIKE_WIDTH = 220;
    private static final int BIKE_HEIGHT = 120;
    private static final int BIKE_BASELINE_OFFSET = -22;
    private static final int CAR_WIDTH = 360;
    private static final int CAR_HEIGHT = 196;
    private static final int TRANSPORT_TRANSITION_MS = 1_600;
    private static final int INTRO_SPEECH_TOTAL_MS = 24_000;
    private static final String[] INTRO_SPEECH_LINES = new String[] {
        "Se quero ver mudanca, preciso comecar por mim.",
        "Hora de sair e fazer a diferenca.",
        "Como vou me locomover?"
    };

    private final Runnable onPhaseCompleted;
    private final Player player;
    private final List<Platform> platforms;
    private final List<DecisionPoint> decisions;
    private final KeyboardController movementController;
    private final JButton continueButton;
    private final JButton tutorialButton;
    private final BufferedImage phaseBackgroundStart;
    private final BufferedImage phaseBackgroundAfterTransport;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private final BufferedImage bikeSprite;
    private final BufferedImage carSprite;

    private boolean running = true;
    private int currentDecisionIndex = 0;
    private DecisionPoint activeDecision;
    private TransportChoice nearbyTransport = TransportChoice.NONE;
    private TransportChoice activeTransportPrompt = TransportChoice.NONE;
    private TransportChoice transitionTransportChoice = TransportChoice.NONE;
    private boolean transportResolved = false;
    private boolean usingPostTransportScenario = false;
    private boolean transportTransitionActive = false;
    private boolean transportTransitionHalfReached = false;
    private long transportTransitionStartMs;
    private boolean gameOver = false;
    private int ecoScore = 0;
    private int pollutionLevel = 60;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;
    private boolean showTutorial = true;
    private boolean showIntroSpeech = true;
    private long introSpeechStartMs;

    public PhaseOnePanel(Runnable onPhaseCompleted) {
        this.onPhaseCompleted = onPhaseCompleted;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);
        setLayout(null);

        player = new Player(245, 300);
        phaseBackgroundStart = ResourceLoader.loadImage("sprites/Fase1.png");
        phaseBackgroundAfterTransport = ResourceLoader.loadImage("sprites/Fase2New.png");
        playerSpriteOne = ResourceLoader.loadImage("sprites/Personagem1.png");
        playerSpriteTwo = ResourceLoader.loadImage("sprites/Personagem2.png");
        bikeSprite = ResourceLoader.loadImage("sprites/Bike.png");
        carSprite = ResourceLoader.loadImage("sprites/Carro.png");
        platforms = createPlatforms();
        decisions = createDecisionPoints();
        introSpeechStartMs = System.currentTimeMillis();

        movementController = new KeyboardController(player);
        addKeyListener(movementController);
        addKeyListener(new DecisionInputController());

        continueButton = new JButton("Iniciar fase de coleta");
        continueButton.setFont(new Font("Dialog", Font.BOLD, 20));
        continueButton.setFocusable(false);
        continueButton.setVisible(false);
        continueButton.addActionListener(e -> this.onPhaseCompleted.run());
        add(continueButton);

        tutorialButton = new JButton("Entendi");
        tutorialButton.setFont(new Font("Dialog", Font.BOLD, 20));
        tutorialButton.setFocusable(false);
        tutorialButton.setVisible(true);
        tutorialButton.addActionListener(e -> closeTutorial());
        add(tutorialButton);

        startGameLoop();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int width = 280;
        int height = 48;
        continueButton.setBounds((getWidth() - width) / 2, Constants.GAME_HEIGHT - 60, width, height);

        int tutorialButtonWidth = 190;
        int tutorialButtonHeight = 46;
        tutorialButton.setBounds(
            (getWidth() - tutorialButtonWidth) / 2,
            (Constants.GAME_HEIGHT / 2) + 144,
            tutorialButtonWidth,
            tutorialButtonHeight
        );
    }

    private List<Platform> createPlatforms() {
        List<Platform> levelPlatforms = new ArrayList<>();

        // Keep only an invisible floor so movement physics still works.
        levelPlatforms.add(new Platform(0, Constants.GAME_HEIGHT - 64, Constants.GAME_WIDTH, 64));
        return levelPlatforms;
    }

    private List<DecisionPoint> createDecisionPoints() {
        List<DecisionPoint> points = new ArrayList<>();

        // Decision 2, 3 and 4 are disabled for now.
        // Keep only decision 5 after transport interaction.
        points.add(new DecisionPoint(
            930,
            "Espaco publico",
            "Intervir na degradacao",
            "Ignorar degradacao",
            true,
            5
        ));

        return points;
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
        updateIntroSpeechState();
        updateTransportTransition();

        tutorialButton.setVisible(showTutorial);

        if (showTutorial) {
            player.stopMoving();
            updatePlayerAnimation(false);
            continueButton.setVisible(false);
            return;
        }

        if (transportTransitionActive) {
            player.stopMoving();
            updatePlayerAnimation(false);
            continueButton.setVisible(false);
            return;
        }

        if (!isPhaseComplete() && !gameOver) {
            if (activeDecision == null && activeTransportPrompt == TransportChoice.NONE) {
                PhysicsEngine.update(player, platforms);
                updatePlayerAnimation(Math.abs(player.getVelX()) > 0.01f);

                if (!transportResolved) {
                    updateNearbyTransport();
                } else {
                    checkDecisionTrigger();
                }
            } else {
                player.stopMoving();
                updatePlayerAnimation(false);
            }
        }

        continueButton.setVisible(isPhaseComplete());
    }

    private void updateTransportTransition() {
        if (!transportTransitionActive) {
            return;
        }

        long elapsed = System.currentTimeMillis() - transportTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        if (!transportTransitionHalfReached && progress >= 0.5) {
            transportTransitionHalfReached = true;
            applyTransportChoice(transitionTransportChoice);
            transitionTransportChoice = TransportChoice.NONE;
            usingPostTransportScenario = true;
            transportResolved = true;
            nearbyTransport = TransportChoice.NONE;
            showIntroSpeech = false;

            // Enter the second scenario from the left side of the map.
            double spawnX = 28;
            double spawnY = Constants.GAME_HEIGHT - 64 - Constants.PLAYER_HEIGHT;
            player.teleportTo(spawnX, spawnY);
        }

        if (progress >= 1.0) {
            transportTransitionActive = false;
        }
    }

    private void updateIntroSpeechState() {
        if (!showIntroSpeech) {
            return;
        }

        long elapsed = System.currentTimeMillis() - introSpeechStartMs;
        if (elapsed >= INTRO_SPEECH_TOTAL_MS) {
            showIntroSpeech = false;
        }
    }

    private void closeTutorial() {
        showTutorial = false;
        tutorialButton.setVisible(false);
        requestFocusInWindow();
    }

    private void updateNearbyTransport() {
        if (transportResolved) {
            nearbyTransport = TransportChoice.NONE;
            return;
        }

        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);
        int bikeDistance = Math.abs(playerCenterX - BIKE_X);
        int carDistance = Math.abs(playerCenterX - CAR_X);

        if (bikeDistance > TRANSPORT_TRIGGER_DISTANCE && carDistance > TRANSPORT_TRIGGER_DISTANCE) {
            nearbyTransport = TransportChoice.NONE;
            return;
        }

        nearbyTransport = bikeDistance <= carDistance ? TransportChoice.BIKE : TransportChoice.CAR;
    }

    private void openTransportPromptIfNearby() {
        if (transportResolved || activeTransportPrompt != TransportChoice.NONE || nearbyTransport == TransportChoice.NONE) {
            return;
        }

        activeTransportPrompt = nearbyTransport;
        player.stopMoving();
    }

    private void checkDecisionTrigger() {
        if (currentDecisionIndex >= decisions.size()) {
            return;
        }

        DecisionPoint point = decisions.get(currentDecisionIndex);
        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);

        if (Math.abs(playerCenterX - point.markerX) <= MARKER_TRIGGER_DISTANCE) {
            activeDecision = point;
            player.stopMoving();
        }
    }

    private void handleTransportSelection(boolean choose) {
        if (activeTransportPrompt == TransportChoice.NONE) {
            return;
        }

        if (!choose) {
            activeTransportPrompt = TransportChoice.NONE;
            return;
        }

        transitionTransportChoice = activeTransportPrompt;
        activeTransportPrompt = TransportChoice.NONE;
        startTransportTransition();
    }

    private void startTransportTransition() {
        transportTransitionActive = true;
        transportTransitionHalfReached = false;
        transportTransitionStartMs = System.currentTimeMillis();
    }

    private void applyTransportChoice(TransportChoice choice) {
        if (choice == TransportChoice.NONE) {
            return;
        }

        boolean sustainableChoice = choice == TransportChoice.BIKE;
        if (sustainableChoice) {
            ecoScore += 20;
            pollutionLevel = Math.max(0, pollutionLevel - 10);
        } else {
            ecoScore = Math.max(0, ecoScore - 5);
            pollutionLevel = Math.min(100, pollutionLevel + 10);
        }

        checkGameOver();
        if (gameOver) {
            activeDecision = null;
            return;
        }
    }

    private void answerDecision(int optionNumber) {
        if (activeDecision == null || activeDecision.answered) {
            return;
        }

        activeDecision.answered = true;
        activeDecision.chosenOption = optionNumber;

        boolean sustainableChoice = optionNumber == 1 ? activeDecision.optionOneSustainable : !activeDecision.optionOneSustainable;
        if (sustainableChoice) {
            ecoScore += 20;
            pollutionLevel = Math.max(0, pollutionLevel - 10);
        } else {
            ecoScore = Math.max(0, ecoScore - 5);
            pollutionLevel = Math.min(100, pollutionLevel + 10);
        }

        checkGameOver();
        if (gameOver) {
            activeDecision = null;
            return;
        }

        currentDecisionIndex++;
        activeDecision = null;
    }

    private void checkGameOver() {
        gameOver = pollutionLevel >= POLLUTION_LIMIT;
    }

    private boolean isPhaseComplete() {
        return transportResolved && currentDecisionIndex >= decisions.size();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawPhaseBackground(g2d);
        drawTransportObjects(g2d);
        drawTransportKeyHint(g2d);
        drawDecisionMarkers(g2d);
        drawPlayer(g2d);
        drawIntroSpeechBubble(g2d);
        drawPollutionBar(g2d);
        drawHud(g2d);

        if (activeDecision != null) {
            drawDecisionOverlay(g2d, activeDecision);
        }

        if (activeTransportPrompt != TransportChoice.NONE) {
            drawTransportOverlay(g2d);
        }

        if (isPhaseComplete()) {
            drawPhaseCompleteOverlay(g2d);
        }

        if (gameOver) {
            drawGameOverOverlay(g2d);
        }

        if (transportTransitionActive) {
            drawTransportTransition(g2d);
        }

        if (showTutorial) {
            drawTutorialOverlay(g2d);
        }
    }

    private void drawTransportTransition(Graphics2D g) {
        long elapsed = System.currentTimeMillis() - transportTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        float alphaFactor = progress < 0.5
            ? (float) (progress * 2.0)
            : (float) ((1.0 - progress) * 2.0);

        int alpha = Math.min(220, Math.max(0, (int) (255 * alphaFactor)));
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
    }

    private void drawPhaseBackground(Graphics2D g) {
        BufferedImage currentBackground = usingPostTransportScenario ? phaseBackgroundAfterTransport : phaseBackgroundStart;
        if (currentBackground != null) {
            g.drawImage(currentBackground, 0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT, null);
        } else {
            BackgroundRenderer.draw(g, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        }
    }

    private void drawTransportObjects(Graphics2D g) {
        if (transportResolved || transportTransitionActive) {
            return;
        }

        int bikeX = BIKE_X - (BIKE_WIDTH / 2);
        int carX = CAR_X - (CAR_WIDTH / 2);
        int groundY = Constants.GAME_HEIGHT - 64;
        int bikeY = groundY - BIKE_HEIGHT + BIKE_BASELINE_OFFSET;
        int carY = groundY - CAR_HEIGHT;

        if (bikeSprite != null) {
            g.drawImage(bikeSprite, bikeX, bikeY, BIKE_WIDTH, BIKE_HEIGHT, null);
        } else {
            g.setColor(new Color(70, 170, 95));
            g.fillRoundRect(bikeX, bikeY, BIKE_WIDTH, BIKE_HEIGHT, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString("BIKE", bikeX + 22, bikeY + 43);
        }

        if (carSprite != null) {
            g.drawImage(carSprite, carX, carY, CAR_WIDTH, CAR_HEIGHT, null);
        } else {
            g.setColor(new Color(180, 70, 70));
            g.fillRoundRect(carX, carY, CAR_WIDTH, CAR_HEIGHT, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString("CARRO", carX + 22, carY + 48);
        }
    }

    private void drawTransportKeyHint(Graphics2D g) {
        if (transportResolved || activeTransportPrompt != TransportChoice.NONE || nearbyTransport == TransportChoice.NONE) {
            return;
        }

        int targetX = nearbyTransport == TransportChoice.BIKE ? BIKE_X : CAR_X;
        int groundY = Constants.GAME_HEIGHT - 64;
        int targetHeight = nearbyTransport == TransportChoice.BIKE ? BIKE_HEIGHT : CAR_HEIGHT;
        int hintX = targetX - 18;
        int hintY = groundY - targetHeight - 52;

        g.setColor(new Color(15, 20, 32, 220));
        g.fillRoundRect(hintX, hintY, 36, 36, 10, 10);
        g.setColor(new Color(245, 245, 250));
        g.drawRoundRect(hintX, hintY, 36, 36, 10, 10);
        g.setFont(new Font("Dialog", Font.BOLD, 24));
        g.drawString("E", hintX + 11, hintY + 26);
    }

    private void drawDecisionMarkers(Graphics2D g) {
        if (gameOver) {
            return;
        }

        for (int i = 0; i < decisions.size(); i++) {
            DecisionPoint point = decisions.get(i);
            int x = point.markerX - 18;
            int y = Constants.GAME_HEIGHT - 110;

            if (point.answered) {
                boolean sustainable = point.chosenOption == 1 ? point.optionOneSustainable : !point.optionOneSustainable;
                g.setColor(sustainable ? new Color(45, 166, 80, 210) : new Color(180, 60, 60, 210));
            } else if (i == currentDecisionIndex && transportResolved) {
                g.setColor(new Color(250, 220, 70, 210));
            } else {
                g.setColor(new Color(245, 245, 245, 190));
            }

            g.fillOval(x, y, 36, 36);
            g.setColor(new Color(30, 30, 30, 230));
            g.drawOval(x, y, 36, 36);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.drawString(String.valueOf(point.displayOrder), point.markerX - 5, y + 23);
        }
    }

    private void drawIntroSpeechBubble(Graphics2D g) {
        if (!showIntroSpeech || INTRO_SPEECH_LINES.length == 0) {
            return;
        }

        Font previousFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();

        int maxTextWidth = 0;
        for (String line : INTRO_SPEECH_LINES) {
            maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(line));
        }

        int lineHeight = 24;
        int bubbleWidth = maxTextWidth + 34;
        int bubbleHeight = 26 + (INTRO_SPEECH_LINES.length * lineHeight);

        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);
        int bubbleX = playerCenterX - (bubbleWidth / 2);
        int bubbleY = (int) player.getY() - 130;

        int rightLimit = Constants.WINDOW_WIDTH - 68;
        if (bubbleX < 12) {
            bubbleX = 12;
        }
        if (bubbleX + bubbleWidth > rightLimit) {
            bubbleX = rightLimit - bubbleWidth;
        }
        if (bubbleY < 12) {
            bubbleY = 12;
        }

        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 16, 16);
        g.setColor(new Color(40, 45, 55, 235));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 16, 16);

        int tailX = Math.max(bubbleX + 14, Math.min(playerCenterX - 8, bubbleX + bubbleWidth - 26));
        Polygon tail = new Polygon(
            new int[] {tailX, tailX + 16, tailX + 8},
            new int[] {bubbleY + bubbleHeight - 2, bubbleY + bubbleHeight - 2, bubbleY + bubbleHeight + 14},
            3
        );
        g.setColor(new Color(255, 255, 255, 235));
        g.fillPolygon(tail);
        g.setColor(new Color(40, 45, 55, 235));
        g.drawPolygon(tail);

        g.setColor(new Color(20, 26, 36));
        int textY = bubbleY + 26;
        for (String line : INTRO_SPEECH_LINES) {
            g.drawString(line, bubbleX + 16, textY);
            textY += lineHeight;
        }
        g.setFont(previousFont);
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

        g.setColor(new Color(255, 128, 0));
        g.fillRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        g.setColor(new Color(180, 90, 0));
        g.drawRect((int) player.getX(), (int) player.getY(), Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
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

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(Constants.COLOR_HUD));
        g.fillRect(0, Constants.GAME_HEIGHT, Constants.WINDOW_WIDTH, Constants.HUD_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 22));
        g.drawString("FASE 1 - DECISOES URBANAS", 20, Constants.GAME_HEIGHT + 30);

        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        int totalSteps = decisions.size() + 1;
        int completedSteps = currentDecisionIndex + (transportResolved ? 1 : 0);
        g.drawString("Progresso: " + completedSteps + "/" + totalSteps, 20, Constants.GAME_HEIGHT + 55);
        g.drawString("Eco score: " + ecoScore, 280, Constants.GAME_HEIGHT + 55);
        g.drawString("Poluicao: " + pollutionLevel + "%", 460, Constants.GAME_HEIGHT + 55);
        g.drawString("Mova-se com A/D ou setas", 690, Constants.GAME_HEIGHT + 55);
    }

    private void drawDecisionOverlay(Graphics2D g, DecisionPoint decision) {
        int boxX = 120;
        int boxY = 140;
        int boxW = Constants.WINDOW_WIDTH - 240;
        int boxH = 230;

        g.setColor(new Color(20, 24, 38, 225));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 22, 22);
        g.setColor(new Color(235, 240, 255));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 22, 22);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 30));
        g.drawString(decision.theme, boxX + 24, boxY + 46);

        g.setFont(new Font("Dialog", Font.PLAIN, 23));
        g.drawString("1) " + decision.optionOne, boxX + 24, boxY + 104);
        g.drawString("2) " + decision.optionTwo, boxX + 24, boxY + 146);

        g.setFont(new Font("Dialog", Font.PLAIN, 19));
        g.setColor(new Color(220, 228, 255));
        g.drawString("Escolha com as teclas 1 ou 2.", boxX + 24, boxY + 192);
    }

    private void drawTransportOverlay(Graphics2D g) {
        int boxX = 120;
        int boxY = 140;
        int boxW = Constants.WINDOW_WIDTH - 240;
        int boxH = 230;

        g.setColor(new Color(20, 24, 38, 225));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 22, 22);
        g.setColor(new Color(235, 240, 255));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 22, 22);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 30));
        boolean bikePrompt = activeTransportPrompt == TransportChoice.BIKE;
        g.drawString(bikePrompt ? "Escolher andar de bicicleta?" : "Escolher ir de carro?", boxX + 24, boxY + 46);

        g.setFont(new Font("Dialog", Font.PLAIN, 23));
        g.drawString(bikePrompt ? "1) Escolher andar de bicicleta" : "1) Escolher ir de carro", boxX + 24, boxY + 104);
        g.drawString("2) Deixar pra la", boxX + 24, boxY + 146);

        g.setFont(new Font("Dialog", Font.PLAIN, 19));
        g.setColor(new Color(220, 228, 255));
        g.drawString("Escolha com 1 ou 2. Fora do menu, aproxime e aperte E.", boxX + 24, boxY + 192);
    }

    private void drawPhaseCompleteOverlay(Graphics2D g) {
        g.setColor(new Color(18, 24, 32, 140));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 34));
        g.drawString("Fase 1 concluida", 420, 200);

        g.setFont(new Font("Dialog", Font.PLAIN, 24));
        g.drawString("Eco score final: " + ecoScore, 430, 245);
        g.drawString("Poluicao final: " + pollutionLevel + "%", 430, 280);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(new Color(20, 10, 12, 180));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(new Color(255, 235, 235));
        g.setFont(new Font("Dialog", Font.BOLD, 48));
        g.drawString("GAME OVER", 410, 220);

        g.setFont(new Font("Dialog", Font.PLAIN, 24));
        g.drawString("A poluicao atingiu o limite de " + POLLUTION_LIMIT + "%.", 320, 270);
        g.drawString("Tente escolhas mais sustentaveis na proxima tentativa.", 260, 308);
    }

    private void drawTutorialOverlay(Graphics2D g) {
        int boxX = 110;
        int boxY = 80;
        int boxW = Constants.WINDOW_WIDTH - 220;
        int boxH = Constants.GAME_HEIGHT - 140;

        g.setColor(new Color(10, 16, 28, 220));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 24, 24);
        g.setColor(new Color(233, 239, 250));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 24, 24);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 32));
        g.drawString("Como jogar", boxX + 24, boxY + 46);

        g.setFont(new Font("Dialog", Font.PLAIN, 21));
        g.drawString("Movimento:", boxX + 24, boxY + 88);
        g.drawString("- A e D para andar", boxX + 40, boxY + 120);
        g.drawString("- W (ou ESPACO) para pular", boxX + 40, boxY + 150);

        g.drawString("Decisoes:", boxX + 24, boxY + 198);
        g.drawString("- Aproxime-se da bicicleta ou do carro", boxX + 40, boxY + 230);
        g.drawString("- Quando aparecer o botao E, aperte E para abrir as opcoes", boxX + 40, boxY + 260);
        g.drawString("- Pressione 1 para escolher e 2 para deixar pra la", boxX + 40, boxY + 290);

        g.drawString("Barra de poluicao (lado direito):", boxX + 24, boxY + 338);
        g.drawString("- Bicicleta reduz 10% da poluicao", boxX + 40, boxY + 370);
        g.drawString("- Carro aumenta 10% da poluicao", boxX + 40, boxY + 400);
        g.drawString("- Se chegar a 100%, e GAME OVER", boxX + 40, boxY + 430);

        g.setFont(new Font("Dialog", Font.PLAIN, 18));
        g.setColor(new Color(210, 220, 240));
        g.drawString("Clique em Entendi ou pressione ENTER para continuar.", boxX + 24, boxY + boxH - 20);
    }

    private class DecisionInputController extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (showTutorial) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_E) {
                    closeTutorial();
                }
                return;
            }

            if (transportTransitionActive) {
                return;
            }

            if (gameOver) {
                return;
            }

            if (activeTransportPrompt != TransportChoice.NONE) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
                    handleTransportSelection(true);
                }
                if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
                    handleTransportSelection(false);
                }
                return;
            }

            int key = e.getKeyCode();
            if (!transportResolved && (key == KeyEvent.VK_E)) {
                openTransportPromptIfNearby();
                return;
            }

            if (activeDecision == null) {
                return;
            }

            if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
                answerDecision(1);
            }
            if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
                answerDecision(2);
            }
        }
    }

    private static class DecisionPoint {
        private final int markerX;
        private final String theme;
        private final String optionOne;
        private final String optionTwo;
        private final boolean optionOneSustainable;
        private final int displayOrder;

        private boolean answered;
        private int chosenOption;

        private DecisionPoint(
            int markerX,
            String theme,
            String optionOne,
            String optionTwo,
            boolean optionOneSustainable,
            int displayOrder
        ) {
            this.markerX = markerX;
            this.theme = theme;
            this.optionOne = optionOne;
            this.optionTwo = optionTwo;
            this.optionOneSustainable = optionOneSustainable;
            this.displayOrder = displayOrder;
            this.answered = false;
            this.chosenOption = 0;
        }
    }

    private enum TransportChoice {
        NONE,
        BIKE,
        CAR
    }
}
