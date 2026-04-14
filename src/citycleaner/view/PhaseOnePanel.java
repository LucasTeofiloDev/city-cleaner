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
    private static final int PHASE_ONE_GROUND_Y = Constants.GAME_HEIGHT - 56;
    private static final int COFFEE_DECISION_X = 655;
    private static final int COFFEE_TRIGGER_START_X = 560;
    private static final int COFFEE_TRIGGER_END_X = 760;
    private static final int COFFEE_HINT_Y = 270;
    private static final int TAP_DECISION_X = 110;
    private static final int TAP_TRIGGER_START_X = 24;
    private static final int TAP_TRIGGER_END_X = 220;
    private static final int TAP_HINT_Y = 300;
    private static final int CHILDREN_DECISION_X = 760;
    private static final int CHILDREN_TRIGGER_START_X = 640;
    private static final int CHILDREN_TRIGGER_END_X = 920;
    private static final int CHILDREN_HINT_Y = 295;
    private static final int TREE_MAN_X = 760;
    private static final int TREE_MAN_TRIGGER_START_X = 680;
    private static final int TREE_MAN_TRIGGER_END_X = 900;
    private static final int TREE_MAN_HINT_Y = 300;
    private static final int TREE_ALERT_MS = 3_000;
    private static final int TREE_PRIMARY_DECISION_TOTAL_MS = 5_000;
    private static final int TREE_INTERVENE_DETAIL_TOTAL_MS = 7_000;
    private static final int FINAL_SCREEN_TRANSITION_MS = 1_600;
    private static final int POLLUTION_LIMIT = 100;
    private static final int BIKE_X = 460;
    private static final int CAR_X = 930;
    private static final int BIKE_WIDTH = 220;
    private static final int BIKE_HEIGHT = 120;
    private static final int BIKE_BASELINE_OFFSET = -22;
    private static final int CAR_WIDTH = 360;
    private static final int CAR_HEIGHT = 196;
    private static final int CHILDREN_TARGET_HEIGHT = 225;
    private static final int CHILDREN_X = 620;
    private static final int CHILDREN_BOTTOM_OFFSET = 4;
    private static final String[] CHILDREN_BUBBLE_ONE = new String[] {
        "Joga no chao mesmo.",
        "Ninguem liga!"
    };
    private static final String[] CHILDREN_BUBBLE_TWO = new String[] {
        "Deixa ai no lixo.",
        "Depois a gente sai."
    };
    private static final int TRANSPORT_TRANSITION_MS = 1_600;
    private static final int INTRO_SPEECH_TOTAL_MS = 24_000;
    private static final int COFFEE_GUIDE_SPEECH_TOTAL_MS = 12_000;
    private static final String[] INTRO_SPEECH_LINES = new String[] {
        "Se quero ver mudanca, preciso comecar por mim.",
        "Hora de sair e fazer a diferenca.",
        "Como vou me locomover?"
    };
    private static final String[] COFFEE_GUIDE_LINES = new String[] {
        "Preciso me manter acordado, talvez um cafe me ajudaria neste momento.",
        "Qual copo vou escolher?"
    };
    private static final int TAP_REMINDER_SPEECH_TOTAL_MS = 7_500;
    private static final String[] TAP_REMINDER_LINES = new String[] {
        "Parece que tem alguma torneira ligada..."
    };
    private static final int SCENARIO_FOUR_GUIDE_SPEECH_TOTAL_MS = 9_000;
    private static final String[] SCENARIO_FOUR_GUIDE_LINES = new String[] {
        "Parece que tem alguem cortando algumas arvores!",
        "Precisamos chegar perto para checar melhor."
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
    private final BufferedImage phaseBackgroundThree;
    private final BufferedImage phaseBackgroundFour;
    private final BufferedImage playerSpriteOne;
    private final BufferedImage playerSpriteTwo;
    private final BufferedImage childrenSprite;
    private final BufferedImage bikeSprite;
    private final BufferedImage carSprite;

    private boolean running = true;
    private int currentDecisionIndex = 0;
    private DecisionPoint activeDecision;
    private DecisionPoint nearbyDecision;
    private TransportChoice nearbyTransport = TransportChoice.NONE;
    private TransportChoice activeTransportPrompt = TransportChoice.NONE;
    private TransportChoice transitionTransportChoice = TransportChoice.NONE;
    private boolean transportResolved = false;
    private boolean usingPostTransportScenario = false;
    private boolean usingThirdScenario = false;
    private boolean usingFourthScenario = false;
    private boolean transportTransitionActive = false;
    private boolean transportTransitionHalfReached = false;
    private long transportTransitionStartMs;
    private boolean scenarioThreeTransitionActive = false;
    private boolean scenarioThreeTransitionHalfReached = false;
    private long scenarioThreeTransitionStartMs;
    private boolean scenarioFourTransitionActive = false;
    private boolean scenarioFourTransitionHalfReached = false;
    private long scenarioFourTransitionStartMs;
    private boolean gameOver = false;
    private int ecoScore = 0;
    private int pollutionLevel = 60;
    private int playerAnimationTick = 0;
    private boolean useFirstSprite = true;
    private boolean showTutorial = true;
    private boolean showIntroSpeech = true;
    private boolean showCoffeeGuideSpeech = false;
    private boolean showTapReminderSpeech = false;
    private boolean showScenarioFourGuideSpeech = false;
    private boolean nearbyTreeDecision = false;
    private boolean treeDecisionResolved = false;
    private boolean usedConversationIntervention = false;
    private boolean showingFinalScreen = false;
    private boolean finalScreenTransitionActive = false;
    private boolean finalScreenTransitionHalfReached = false;
    private TreeDecisionStage treeDecisionStage = TreeDecisionStage.NONE;
    private long introSpeechStartMs;
    private long coffeeGuideSpeechStartMs;
    private long tapReminderSpeechStartMs;
    private long scenarioFourGuideSpeechStartMs;
    private long treeDecisionStageStartMs;
    private long treeDecisionDeadlineMs;
    private long finalScreenTransitionStartMs;

    public PhaseOnePanel(Runnable onPhaseCompleted) {
        this.onPhaseCompleted = onPhaseCompleted;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);
        setLayout(null);

        player = new Player(245, 300);
        phaseBackgroundStart = ResourceLoader.loadImage("sprites/Fase1.png");
        phaseBackgroundAfterTransport = ResourceLoader.loadImage("sprites/CenarioFase1_2.png");
        phaseBackgroundThree = ResourceLoader.loadImage("sprites/Fase1_3.png");
        phaseBackgroundFour = ResourceLoader.loadImage("sprites/Fase1_4.png");
        playerSpriteOne = ResourceLoader.loadImage("sprites/Personagem1.png");
        playerSpriteTwo = ResourceLoader.loadImage("sprites/Personagem2.png");
        childrenSprite = ResourceLoader.loadImage("sprites/criancas.png");
        bikeSprite = ResourceLoader.loadImage("sprites/Bike.png");
        carSprite = ResourceLoader.loadImage("sprites/Carro.png");
        platforms = createPlatforms();
        decisions = createDecisionPoints();
        introSpeechStartMs = System.currentTimeMillis();

        movementController = new KeyboardController(player);
        addKeyListener(movementController);
        addKeyListener(new DecisionInputController());

        continueButton = new JButton("Continuar para a Fase 2");
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
        levelPlatforms.add(new Platform(0, PHASE_ONE_GROUND_Y, Constants.GAME_WIDTH, 64));
        return levelPlatforms;
    }

    private List<DecisionPoint> createDecisionPoints() {
        List<DecisionPoint> points = new ArrayList<>();

        // Scenario 2: coffee shop decision.
        points.add(new DecisionPoint(
            COFFEE_DECISION_X,
            COFFEE_TRIGGER_START_X,
            COFFEE_TRIGGER_END_X,
            COFFEE_HINT_Y,
            2,
            "Loja Sao Joao (Takeaway Coffee)",
            "Pegar copo sustentavel",
            "Pegar copo descartavel",
            true,
            2,
            false
        ));

        // Scenario 3: leaking tap decision.
        points.add(new DecisionPoint(
            TAP_DECISION_X,
            TAP_TRIGGER_START_X,
            TAP_TRIGGER_END_X,
            TAP_HINT_Y,
            3,
            "Torneira da rua",
            "Fechar a torneira",
            "Deixar a torneira aberta",
            true,
            3,
            false
        ));

        // Scenario 3: children littering decision.
        points.add(new DecisionPoint(
            CHILDREN_DECISION_X,
            CHILDREN_TRIGGER_START_X,
            CHILDREN_TRIGGER_END_X,
            CHILDREN_HINT_Y,
            3,
            "Grupo de criancas",
            "Intervir e conscientizar",
            "Deixar pra la",
            true,
            4,
            false
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
        updateCoffeeGuideSpeechState();
        updateTapReminderSpeechState();
        updateScenarioFourGuideSpeechState();
        updateTreeDecisionState();
        updateTransportTransition();
        updateScenarioThreeTransition();
        updateScenarioFourTransition();
        updateFinalScreenTransition();

        tutorialButton.setVisible(showTutorial);

        if (showTutorial) {
            player.stopMoving();
            updatePlayerAnimation(false);
            continueButton.setVisible(false);
            return;
        }

        if (transportTransitionActive || scenarioThreeTransitionActive || scenarioFourTransitionActive || finalScreenTransitionActive) {
            player.stopMoving();
            updatePlayerAnimation(false);
            continueButton.setVisible(false);
            return;
        }

        if (!isPhaseComplete() && !gameOver) {
            if (activeDecision == null && activeTransportPrompt == TransportChoice.NONE && treeDecisionStage == TreeDecisionStage.NONE) {
                PhysicsEngine.update(player, platforms);
                updatePlayerAnimation(Math.abs(player.getVelX()) > 0.01f);

                if (!transportResolved) {
                    updateNearbyTransport();
                } else {
                    if (!usingThirdScenario && !usingFourthScenario) {
                        updateScenarioThreeEntry();
                    } else if (usingThirdScenario && !usingFourthScenario) {
                        updateScenarioFourEntry();
                    }

                    if (usingFourthScenario) {
                        nearbyDecision = null;
                        updateNearbyTreeDecision();
                    } else {
                        updateNearbyDecision();
                        nearbyTreeDecision = false;
                    }
                }
            } else {
                player.stopMoving();
                updatePlayerAnimation(false);
            }
        }

        continueButton.setVisible(showingFinalScreen && !finalScreenTransitionActive);
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
            showCoffeeGuideSpeech = true;
            coffeeGuideSpeechStartMs = System.currentTimeMillis();

            // Enter the second scenario from the left side of the map.
            double spawnX = 28;
            double spawnY = PHASE_ONE_GROUND_Y - Constants.PLAYER_HEIGHT;
            player.teleportTo(spawnX, spawnY);
        }

        if (progress >= 1.0) {
            transportTransitionActive = false;
        }
    }

    private void updateScenarioThreeEntry() {
        if (!usingPostTransportScenario || usingThirdScenario || usingFourthScenario || scenarioThreeTransitionActive) {
            return;
        }

        // Move to scenario 3 right after resolving the coffee decision.
        if (currentDecisionIndex < 1) {
            return;
        }

        startScenarioThreeTransition();
    }

    private void startScenarioThreeTransition() {
        scenarioThreeTransitionActive = true;
        scenarioThreeTransitionHalfReached = false;
        scenarioThreeTransitionStartMs = System.currentTimeMillis();
    }

    private void updateScenarioThreeTransition() {
        if (!scenarioThreeTransitionActive) {
            return;
        }

        long elapsed = System.currentTimeMillis() - scenarioThreeTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        if (!scenarioThreeTransitionHalfReached && progress >= 0.5) {
            scenarioThreeTransitionHalfReached = true;
            usingThirdScenario = true;
            showCoffeeGuideSpeech = false;

            double spawnX = 28;
            double spawnY = PHASE_ONE_GROUND_Y - Constants.PLAYER_HEIGHT;
            player.teleportTo(spawnX, spawnY);
        }

        if (progress >= 1.0) {
            scenarioThreeTransitionActive = false;
        }
    }

    private void updateScenarioFourEntry() {
        if (!usingThirdScenario || usingFourthScenario || scenarioFourTransitionActive) {
            return;
        }

        if (!areScenarioThreeDecisionsAnswered()) {
            return;
        }

        startScenarioFourTransition();
    }

    private void startScenarioFourTransition() {
        scenarioFourTransitionActive = true;
        scenarioFourTransitionHalfReached = false;
        scenarioFourTransitionStartMs = System.currentTimeMillis();
    }

    private void updateScenarioFourTransition() {
        if (!scenarioFourTransitionActive) {
            return;
        }

        long elapsed = System.currentTimeMillis() - scenarioFourTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        if (!scenarioFourTransitionHalfReached && progress >= 0.5) {
            scenarioFourTransitionHalfReached = true;
            usingThirdScenario = false;
            usingFourthScenario = true;
            showTapReminderSpeech = false;
            showScenarioFourGuideSpeech = true;
            scenarioFourGuideSpeechStartMs = System.currentTimeMillis();

            double spawnX = 28;
            double spawnY = PHASE_ONE_GROUND_Y - Constants.PLAYER_HEIGHT;
            player.teleportTo(spawnX, spawnY);
        }

        if (progress >= 1.0) {
            scenarioFourTransitionActive = false;
        }
    }

    private void startFinalScreenTransition() {
        if (showingFinalScreen || finalScreenTransitionActive) {
            return;
        }

        finalScreenTransitionActive = true;
        finalScreenTransitionHalfReached = false;
        finalScreenTransitionStartMs = System.currentTimeMillis();
    }

    private void updateFinalScreenTransition() {
        if (!finalScreenTransitionActive) {
            return;
        }

        long elapsed = System.currentTimeMillis() - finalScreenTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) FINAL_SCREEN_TRANSITION_MS);

        if (!finalScreenTransitionHalfReached && progress >= 0.5) {
            finalScreenTransitionHalfReached = true;
            showingFinalScreen = true;
        }

        if (progress >= 1.0) {
            finalScreenTransitionActive = false;
        }
    }

    private boolean areScenarioThreeDecisionsAnswered() {
        boolean hasScenarioThreeDecision = false;
        for (DecisionPoint point : decisions) {
            if (point.requiredScenario != 3) {
                continue;
            }

            hasScenarioThreeDecision = true;
            if (!point.answered) {
                return false;
            }
        }
        return hasScenarioThreeDecision;
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

    private void updateCoffeeGuideSpeechState() {
        if (!showCoffeeGuideSpeech) {
            return;
        }

        long elapsed = System.currentTimeMillis() - coffeeGuideSpeechStartMs;
        if (elapsed >= COFFEE_GUIDE_SPEECH_TOTAL_MS) {
            showCoffeeGuideSpeech = false;
        }
    }

    private void updateTapReminderSpeechState() {
        if (!showTapReminderSpeech) {
            return;
        }

        if (isTapDecisionAnswered()) {
            showTapReminderSpeech = false;
            return;
        }

        long elapsed = System.currentTimeMillis() - tapReminderSpeechStartMs;
        if (elapsed >= TAP_REMINDER_SPEECH_TOTAL_MS) {
            showTapReminderSpeech = false;
        }
    }

    private void updateScenarioFourGuideSpeechState() {
        if (!showScenarioFourGuideSpeech) {
            return;
        }

        long elapsed = System.currentTimeMillis() - scenarioFourGuideSpeechStartMs;
        if (elapsed >= SCENARIO_FOUR_GUIDE_SPEECH_TOTAL_MS) {
            showScenarioFourGuideSpeech = false;
        }
    }

    private void updateTreeDecisionState() {
        if (treeDecisionResolved || treeDecisionStage == TreeDecisionStage.NONE) {
            return;
        }

        long now = System.currentTimeMillis();

        if (treeDecisionStage == TreeDecisionStage.ALERT) {
            if (now - treeDecisionStageStartMs >= TREE_ALERT_MS) {
                treeDecisionStage = TreeDecisionStage.PRIMARY;
                treeDecisionStageStartMs = now;
                treeDecisionDeadlineMs = now + TREE_PRIMARY_DECISION_TOTAL_MS;
            }
            return;
        }

        if ((treeDecisionStage == TreeDecisionStage.PRIMARY || treeDecisionStage == TreeDecisionStage.INTERVENE_DETAIL)
            && now >= treeDecisionDeadlineMs) {
            resolveTreeDecision(false, false);
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

    private void updateNearbyDecision() {
        int currentScenario = usingThirdScenario ? 3 : 2;
        int playerLeftX = (int) player.getX();
        int playerRightX = playerLeftX + Constants.PLAYER_WIDTH;

        for (int i = currentDecisionIndex; i < decisions.size(); i++) {
            DecisionPoint point = decisions.get(i);
            if (point.answered || point.requiredScenario != currentScenario) {
                continue;
            }

            boolean isCrossingDecisionArea =
                playerRightX >= point.triggerStartX && playerLeftX <= point.triggerEndX;
            if (isCrossingDecisionArea) {
                nearbyDecision = point;
                return;
            }
        }

        nearbyDecision = null;
    }

    private void updateNearbyTreeDecision() {
        if (!usingFourthScenario || treeDecisionResolved || treeDecisionStage != TreeDecisionStage.NONE) {
            nearbyTreeDecision = false;
            return;
        }

        int playerLeftX = (int) player.getX();
        int playerRightX = playerLeftX + Constants.PLAYER_WIDTH;
        nearbyTreeDecision = playerRightX >= TREE_MAN_TRIGGER_START_X && playerLeftX <= TREE_MAN_TRIGGER_END_X;
    }

    private void openDecisionPromptIfNearby() {
        if (activeDecision != null || nearbyDecision == null || nearbyDecision.answered) {
            return;
        }

        activeDecision = nearbyDecision;
        player.stopMoving();
    }

    private void openTreeDecisionIfNearby() {
        if (!nearbyTreeDecision || treeDecisionResolved || treeDecisionStage != TreeDecisionStage.NONE) {
            return;
        }

        treeDecisionStage = TreeDecisionStage.ALERT;
        treeDecisionStageStartMs = System.currentTimeMillis();
        player.stopMoving();
    }

    private void handleTreeDecisionInput(int optionNumber) {
        if (treeDecisionStage == TreeDecisionStage.PRIMARY) {
            if (optionNumber == 1) {
                treeDecisionStage = TreeDecisionStage.INTERVENE_DETAIL;
                treeDecisionStageStartMs = System.currentTimeMillis();
                treeDecisionDeadlineMs = treeDecisionStageStartMs + TREE_INTERVENE_DETAIL_TOTAL_MS;
            } else {
                resolveTreeDecision(false, false);
            }
            return;
        }

        if (treeDecisionStage == TreeDecisionStage.INTERVENE_DETAIL) {
            if (optionNumber == 1) {
                resolveTreeDecision(true, true);
            } else {
                resolveTreeDecision(true, false);
            }
        }
    }

    private void resolveTreeDecision(boolean intervene, boolean reportedToAgency) {
        treeDecisionResolved = true;
        treeDecisionStage = TreeDecisionStage.NONE;
        nearbyTreeDecision = false;
        usedConversationIntervention = false;

        if (!intervene) {
            ecoScore = Math.max(0, ecoScore - 5);
            pollutionLevel = Math.min(100, pollutionLevel + 10);
        } else if (reportedToAgency) {
            ecoScore += 20;
            pollutionLevel = Math.max(0, pollutionLevel - 10);
        } else {
            ecoScore += 6;
            usedConversationIntervention = true;
        }

        checkGameOver();
        if (!gameOver) {
            startFinalScreenTransition();
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

        DecisionPoint answeredDecision = activeDecision;

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

        while (currentDecisionIndex < decisions.size() && decisions.get(currentDecisionIndex).answered) {
            currentDecisionIndex++;
        }

        if (usingThirdScenario && "Grupo de criancas".equals(answeredDecision.theme) && !isTapDecisionAnswered()) {
            showTapReminderSpeech = true;
            tapReminderSpeechStartMs = System.currentTimeMillis();
        }

        activeDecision = null;
    }

    private boolean isTapDecisionAnswered() {
        for (DecisionPoint point : decisions) {
            if ("Torneira da rua".equals(point.theme)) {
                return point.answered;
            }
        }
        return false;
    }

    private void checkGameOver() {
        gameOver = pollutionLevel >= POLLUTION_LIMIT;
    }

    private boolean isPhaseComplete() {
        return showingFinalScreen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawPhaseBackground(g2d);
        drawChildrenOnRight(g2d);
        drawChildrenTrashBubbles(g2d);
        drawTransportObjects(g2d);
        drawInteractionKeyHint(g2d);
        drawDecisionMarkers(g2d);
        drawPlayer(g2d);
        drawIntroSpeechBubble(g2d);
        drawCoffeeGuideSpeechBubble(g2d);
        drawTapReminderSpeechBubble(g2d);
        drawScenarioFourGuideSpeechBubble(g2d);
        drawTreeDecisionOverlay(g2d);
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

        if (scenarioThreeTransitionActive) {
            drawScenarioThreeTransition(g2d);
        }

        if (scenarioFourTransitionActive) {
            drawScenarioFourTransition(g2d);
        }

        if (finalScreenTransitionActive) {
            drawFinalScreenTransition(g2d);
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
        BufferedImage currentBackground = phaseBackgroundStart;
        if (usingFourthScenario) {
            currentBackground = phaseBackgroundFour;
        } else if (usingThirdScenario) {
            currentBackground = phaseBackgroundThree;
        } else if (usingPostTransportScenario) {
            currentBackground = phaseBackgroundAfterTransport;
        }

        if (currentBackground != null) {
            g.drawImage(currentBackground, 0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT, null);
        } else {
            BackgroundRenderer.draw(g, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
        }
    }

    private void drawScenarioThreeTransition(Graphics2D g) {
        long elapsed = System.currentTimeMillis() - scenarioThreeTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        float alphaFactor = progress < 0.5
            ? (float) (progress * 2.0)
            : (float) ((1.0 - progress) * 2.0);

        int alpha = Math.min(220, Math.max(0, (int) (255 * alphaFactor)));
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
    }

    private void drawScenarioFourTransition(Graphics2D g) {
        long elapsed = System.currentTimeMillis() - scenarioFourTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) TRANSPORT_TRANSITION_MS);

        float alphaFactor = progress < 0.5
            ? (float) (progress * 2.0)
            : (float) ((1.0 - progress) * 2.0);

        int alpha = Math.min(220, Math.max(0, (int) (255 * alphaFactor)));
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
    }

    private void drawFinalScreenTransition(Graphics2D g) {
        long elapsed = System.currentTimeMillis() - finalScreenTransitionStartMs;
        double progress = Math.min(1.0, elapsed / (double) FINAL_SCREEN_TRANSITION_MS);

        float alphaFactor = progress < 0.5
            ? (float) (progress * 2.0)
            : (float) ((1.0 - progress) * 2.0);

        int alpha = Math.min(255, Math.max(0, (int) (255 * alphaFactor)));
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);
    }

    private void drawChildrenOnRight(Graphics2D g) {
        if (!usingThirdScenario || childrenSprite == null) {
            return;
        }

        double aspectRatio = childrenSprite.getWidth() / (double) childrenSprite.getHeight();
        int renderHeight = CHILDREN_TARGET_HEIGHT;
        int renderWidth = (int) Math.round(renderHeight * aspectRatio);
        int x = CHILDREN_X;
        int y = PHASE_ONE_GROUND_Y - renderHeight - CHILDREN_BOTTOM_OFFSET;
        g.drawImage(childrenSprite, x, y, renderWidth, renderHeight, null);
    }

    private void drawChildrenTrashBubbles(Graphics2D g) {
        if (!usingThirdScenario || childrenSprite == null) {
            return;
        }

        double aspectRatio = childrenSprite.getWidth() / (double) childrenSprite.getHeight();
        int renderHeight = CHILDREN_TARGET_HEIGHT;
        int renderWidth = (int) Math.round(renderHeight * aspectRatio);
        int childrenX = CHILDREN_X;
        int childrenY = PHASE_ONE_GROUND_Y - renderHeight - CHILDREN_BOTTOM_OFFSET;

        int firstBubbleWidth = 220;
        int secondBubbleWidth = 220;
        int bubbleHeight = 68;
        int bubbleGroupCenterX = childrenX + (renderWidth / 2);
        int firstBubbleX = Math.max(12, bubbleGroupCenterX - firstBubbleWidth - 14);
        int secondBubbleX = Math.min(
            Constants.WINDOW_WIDTH - secondBubbleWidth - 12,
            bubbleGroupCenterX + 14
        );
        int bubbleY = Math.max(12, childrenY - 112);

        drawChildrenBubble(
            g,
            firstBubbleX,
            bubbleY,
            firstBubbleWidth,
            bubbleHeight,
            CHILDREN_BUBBLE_ONE,
            childrenX + 52,
            childrenY + 20
        );

        drawChildrenBubble(
            g,
            secondBubbleX,
            bubbleY,
            secondBubbleWidth,
            bubbleHeight,
            CHILDREN_BUBBLE_TWO,
            childrenX + renderWidth - 52,
            childrenY + 22
        );
    }

    private void drawChildrenBubble(
        Graphics2D g,
        int bubbleX,
        int bubbleY,
        int bubbleWidth,
        int bubbleHeight,
        String[] lines,
        int targetX,
        int targetY
    ) {
        Font previousFont = g.getFont();

        g.setColor(new Color(255, 255, 255, 238));
        g.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 14, 14);
        g.setColor(new Color(38, 42, 54, 235));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 14, 14);

        int clampedTailX = Math.max(bubbleX + 14, Math.min(targetX, bubbleX + bubbleWidth - 14));
        int tailBaseY = bubbleY + bubbleHeight - 2;
        int tailTipY = Math.max(tailBaseY + 8, targetY);
        Polygon tail = new Polygon(
            new int[] {clampedTailX - 8, clampedTailX + 8, targetX},
            new int[] {tailBaseY, tailBaseY, tailTipY},
            3
        );
        g.setColor(new Color(255, 255, 255, 238));
        g.fillPolygon(tail);
        g.setColor(new Color(38, 42, 54, 235));
        g.drawPolygon(tail);

        g.setColor(new Color(26, 30, 40));
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        int textY = bubbleY + 24;
        for (String line : lines) {
            g.drawString(line, bubbleX + 12, textY);
            textY += 20;
        }

        g.setFont(previousFont);
    }

    private void drawTransportObjects(Graphics2D g) {
        if (transportResolved || transportTransitionActive) {
            return;
        }

        int bikeX = BIKE_X - (BIKE_WIDTH / 2);
        int carX = CAR_X - (CAR_WIDTH / 2);
        int groundY = PHASE_ONE_GROUND_Y;
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

    private void drawInteractionKeyHint(Graphics2D g) {
        if (activeTransportPrompt != TransportChoice.NONE || activeDecision != null || treeDecisionStage != TreeDecisionStage.NONE) {
            return;
        }

        Integer targetX = null;
        Integer targetY = null;

        if (!transportResolved && nearbyTransport != TransportChoice.NONE) {
            int groundY = PHASE_ONE_GROUND_Y;
            int targetHeight = nearbyTransport == TransportChoice.BIKE ? BIKE_HEIGHT : CAR_HEIGHT;
            targetX = nearbyTransport == TransportChoice.BIKE ? BIKE_X : CAR_X;
            targetY = groundY - targetHeight - 52;
        } else if (usingFourthScenario && nearbyTreeDecision) {
            targetX = TREE_MAN_X;
            targetY = TREE_MAN_HINT_Y;
        } else if (transportResolved && nearbyDecision != null) {
            targetX = nearbyDecision.markerX;
            targetY = nearbyDecision.hintY;
        }

        if (targetX == null || targetY == null) {
            return;
        }

        int hintX = targetX - 18;
        int hintY = targetY;

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
            if (!point.showMarker) {
                continue;
            }

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

    private void drawCoffeeGuideSpeechBubble(Graphics2D g) {
        if (!showCoffeeGuideSpeech || COFFEE_GUIDE_LINES.length == 0) {
            return;
        }

        Font previousFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();

        int maxTextWidth = 0;
        for (String line : COFFEE_GUIDE_LINES) {
            maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(line));
        }

        int lineHeight = 24;
        int bubbleWidth = maxTextWidth + 34;
        int bubbleHeight = 26 + (COFFEE_GUIDE_LINES.length * lineHeight);

        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);
        int bubbleX = playerCenterX - (bubbleWidth / 2);
        int bubbleY = (int) player.getY() - 112;

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
        for (String line : COFFEE_GUIDE_LINES) {
            g.drawString(line, bubbleX + 16, textY);
            textY += lineHeight;
        }
        g.setFont(previousFont);
    }

    private void drawTapReminderSpeechBubble(Graphics2D g) {
        if (!showTapReminderSpeech || TAP_REMINDER_LINES.length == 0) {
            return;
        }

        Font previousFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();

        int maxTextWidth = 0;
        for (String line : TAP_REMINDER_LINES) {
            maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(line));
        }

        int lineHeight = 24;
        int bubbleWidth = maxTextWidth + 34;
        int bubbleHeight = 26 + (TAP_REMINDER_LINES.length * lineHeight);

        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);
        int bubbleX = playerCenterX - (bubbleWidth / 2);
        int bubbleY = (int) player.getY() - 146;

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
        for (String line : TAP_REMINDER_LINES) {
            g.drawString(line, bubbleX + 16, textY);
            textY += lineHeight;
        }
        g.setFont(previousFont);
    }

    private void drawScenarioFourGuideSpeechBubble(Graphics2D g) {
        if (!showScenarioFourGuideSpeech || SCENARIO_FOUR_GUIDE_LINES.length == 0) {
            return;
        }

        Font previousFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();

        int maxTextWidth = 0;
        for (String line : SCENARIO_FOUR_GUIDE_LINES) {
            maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(line));
        }

        int lineHeight = 24;
        int bubbleWidth = maxTextWidth + 34;
        int bubbleHeight = 26 + (SCENARIO_FOUR_GUIDE_LINES.length * lineHeight);

        int playerCenterX = (int) player.getX() + (Constants.PLAYER_WIDTH / 2);
        int bubbleX = playerCenterX - (bubbleWidth / 2);
        int bubbleY = (int) player.getY() - 146;

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
        for (String line : SCENARIO_FOUR_GUIDE_LINES) {
            g.drawString(line, bubbleX + 16, textY);
            textY += lineHeight;
        }
        g.setFont(previousFont);
    }

    private void drawTreeDecisionOverlay(Graphics2D g) {
        if (treeDecisionStage == TreeDecisionStage.NONE) {
            return;
        }

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
        g.setFont(new Font("Dialog", Font.BOLD, 34));

        if (treeDecisionStage == TreeDecisionStage.ALERT) {
            g.drawString("Aja rapido!", boxX + 24, boxY + 58);
            g.setFont(new Font("Dialog", Font.PLAIN, 24));
            g.drawString("Um homem esta cortando a arvore.", boxX + 24, boxY + 112);
            g.drawString("Prepare-se para decidir...", boxX + 24, boxY + 152);
            return;
        }

        int secondsLeft = Math.max(0, (int) Math.ceil((treeDecisionDeadlineMs - System.currentTimeMillis()) / 1000.0));
        g.drawString("Corte ilegal de arvore", boxX + 24, boxY + 52);

        g.setFont(new Font("Dialog", Font.BOLD, 22));
        g.setColor(new Color(255, 224, 130));
        g.drawString("Tempo: " + secondsLeft + "s", boxX + boxW - 165, boxY + 52);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.PLAIN, 23));
        if (treeDecisionStage == TreeDecisionStage.PRIMARY) {
            g.drawString("1) Intervir", boxX + 24, boxY + 112);
            g.drawString("2) Deixar pra la", boxX + 24, boxY + 152);
        } else {
            g.drawString("1) Denunciar para orgao ambiental", boxX + 24, boxY + 112);
            g.drawString("2) Intervir conversando com o homem", boxX + 24, boxY + 152);
        }

        g.setFont(new Font("Dialog", Font.PLAIN, 19));
        g.setColor(new Color(220, 228, 255));
        g.drawString("Use 1 ou 2 para escolher antes do tempo acabar.", boxX + 24, boxY + 198);
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
        g.setColor(new Color(0, 0, 0, 245));
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 42));
        g.drawString("Fase 1 Finalizada!", 360, 180);

        g.setFont(new Font("Dialog", Font.PLAIN, 30));
        g.drawString("Porcentagem de poluicao: " + pollutionLevel + "%", 285, 250);

        if (usedConversationIntervention) {
            g.setFont(new Font("Dialog", Font.PLAIN, 22));
            g.setColor(new Color(235, 222, 160));
            g.drawString("Conversar com o homem nao e tao efetivo...", 255, 320);
            g.drawString("Repensa e veja melhores maneiras como denunciar o caso.", 190, 355);
        }

        g.setColor(new Color(210, 220, 240));
        g.setFont(new Font("Dialog", Font.PLAIN, 20));
        g.drawString("Clique no botao para continuar.", 430, 430);
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

            if (transportTransitionActive || scenarioThreeTransitionActive || scenarioFourTransitionActive) {
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

            if (transportResolved && key == KeyEvent.VK_E) {
                openTreeDecisionIfNearby();
                if (treeDecisionStage != TreeDecisionStage.NONE) {
                    return;
                }

                openDecisionPromptIfNearby();
                return;
            }

            if (treeDecisionStage == TreeDecisionStage.ALERT) {
                return;
            }

            if (treeDecisionStage == TreeDecisionStage.PRIMARY || treeDecisionStage == TreeDecisionStage.INTERVENE_DETAIL) {
                if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
                    handleTreeDecisionInput(1);
                }
                if (key == KeyEvent.VK_2 || key == KeyEvent.VK_NUMPAD2) {
                    handleTreeDecisionInput(2);
                }
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
        private final int triggerStartX;
        private final int triggerEndX;
        private final int hintY;
        private final int requiredScenario;
        private final String theme;
        private final String optionOne;
        private final String optionTwo;
        private final boolean optionOneSustainable;
        private final int displayOrder;
        private final boolean showMarker;

        private boolean answered;
        private int chosenOption;

        private DecisionPoint(
            int markerX,
            int triggerStartX,
            int triggerEndX,
            int hintY,
            int requiredScenario,
            String theme,
            String optionOne,
            String optionTwo,
            boolean optionOneSustainable,
            int displayOrder,
            boolean showMarker
        ) {
            this.markerX = markerX;
            this.triggerStartX = triggerStartX;
            this.triggerEndX = triggerEndX;
            this.hintY = hintY;
            this.requiredScenario = requiredScenario;
            this.theme = theme;
            this.optionOne = optionOne;
            this.optionTwo = optionTwo;
            this.optionOneSustainable = optionOneSustainable;
            this.displayOrder = displayOrder;
            this.showMarker = showMarker;
            this.answered = false;
            this.chosenOption = 0;
        }
    }

    private enum TransportChoice {
        NONE,
        BIKE,
        CAR
    }

    private enum TreeDecisionStage {
        NONE,
        ALERT,
        PRIMARY,
        INTERVENE_DETAIL
    }
}
