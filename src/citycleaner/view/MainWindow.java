package citycleaner.view;

import citycleaner.util.AudioManager;

import javax.swing.*;

/**
 * Janela principal do jogo City Cleaner
 */
public class MainWindow extends JFrame {
    private int phaseOneFinalPollutionLevel = 60;
    private int phaseOneFinalScore = 0;
    private int phaseOneCompletedSteps = 0;
    private int phaseOneTotalSteps = 4;
    
    public MainWindow() {
        setTitle("City Cleaner - Environmental Platformer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        AudioManager.playBackgroundMusic("audio/music/GameMusic.wav");

        MenuPanel menuPanel = new MenuPanel(
            this::startCutscenes,
            this::exitGame,
            this::toggleSound,
            AudioManager::isMuted
        );
        add(menuPanel);
        
        // Configurar tamanho e posição
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startCutscenes() {
        IntroScenePanel introScenePanel = new IntroScenePanel(this::startPhaseOne);
        setContentPane(introScenePanel);
        revalidate();
        repaint();
        introScenePanel.requestFocusInWindow();
    }

    private void startPhaseOne() {
        PhaseOnePanel phaseOnePanel = new PhaseOnePanel(result -> startGame(result));
        setContentPane(phaseOnePanel);
        revalidate();
        repaint();
        phaseOnePanel.requestFocusInWindow();
    }

    private void startGame(PhaseOnePanel.PhaseOneResult result) {
        phaseOneFinalPollutionLevel = result.getPollutionLevel();
        phaseOneFinalScore = result.getEcoScore();
        phaseOneCompletedSteps = result.getCompletedSteps();
        phaseOneTotalSteps = result.getTotalSteps();

        AudioManager.playBackgroundMusic("audio/music/Tidal_Warning.mp3");

        GamePanel gamePanel = new GamePanel(
            phaseOneFinalPollutionLevel,
            phaseOneFinalScore,
            phaseOneCompletedSteps,
            phaseOneTotalSteps
        );
        setContentPane(gamePanel);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    private void exitGame() {
        AudioManager.stopBackgroundMusic();
        dispose();
        System.exit(0);
    }

    private void toggleSound() {
        AudioManager.toggleMuted();
    }
}
