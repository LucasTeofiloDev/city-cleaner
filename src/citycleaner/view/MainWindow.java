package citycleaner.view;

import citycleaner.util.AudioManager;

import javax.swing.*;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Janela principal do jogo City Cleaner
 */
public class MainWindow extends JFrame {
    private boolean fullscreen = false;
    private Rectangle windowedBounds = null;
    private final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private int phaseOneFinalPollutionLevel = 60;
    private int phaseOneFinalScore = 0;
    private int phaseOneCompletedSteps = 0;
    private int phaseOneTotalSteps = 4;
    
    public MainWindow() {
        setTitle("City Cleaner - Environmental Platformer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

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
        // Key bindings for fullscreen (F11) and exit fullscreen (ESC)
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "toggleFullscreen");
        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "exitFullscreen");
        getRootPane().getActionMap().put("exitFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fullscreen) toggleFullscreen();
            }
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void toggleFullscreen() {
        if (!fullscreen) {
            windowedBounds = getBounds();
            dispose();
            setUndecorated(true);
            setVisible(true);
            device.setFullScreenWindow(this);
            fullscreen = true;
        } else {
            device.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            if (windowedBounds != null) {
                setBounds(windowedBounds);
            }
            setVisible(true);
            fullscreen = false;
        }
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
