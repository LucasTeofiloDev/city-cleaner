package citycleaner.view;

import citycleaner.util.Constants;
import citycleaner.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scene 1 cutscene shown before gameplay starts.
 */
public class IntroScenePanel extends JPanel {
    private static final int TEXT_SPEED_MS = 110;
    private static final int MARGIN = 28;
    private static final int DIALOG_HEIGHT = 170;

    private final Runnable onStartGame;
    private final List<SceneData> scenes;
    private final StringBuilder visibleText;
    private final Timer textTimer;
    private final JButton actionButton;

    private int currentSceneIndex;
    private List<String> currentWords;
    private int nextWordIndex;

    public IntroScenePanel(Runnable onStartGame) {
        this.onStartGame = onStartGame;
        this.scenes = buildScenes();
        this.visibleText = new StringBuilder();
        this.currentSceneIndex = 0;
        this.currentWords = new ArrayList<>();
        this.nextWordIndex = 0;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setLayout(null);

        actionButton = new JButton("Proxima cena");
        actionButton.setFont(new Font("Dialog", Font.BOLD, 20));
        actionButton.setFocusable(false);
        actionButton.setEnabled(false);
        actionButton.addActionListener(e -> onActionButtonClicked());
        add(actionButton);

        textTimer = new Timer(TEXT_SPEED_MS, e -> revealNextWord());
        textTimer.setInitialDelay(300);
        loadCurrentScene();
        textTimer.start();
    }

    private List<SceneData> buildScenes() {
        List<SceneData> builtScenes = new ArrayList<>();
        builtScenes.add(new SceneData(
            "Cena 1 - Visao da cidade",
            "sprites/Cena1Gemini.png",
            Arrays.asList(
                "A cidade ja nao e mais como antes...",
                "A poluicao tomou conta das ruas."
            )
        ));

        builtScenes.add(new SceneData(
            "Cena 2 - Problema ambiental",
            "sprites/Cena2Gemini.png",
            Arrays.asList(
                "O lixo e descartado de forma incorreta...",
                "E o ar esta cada vez mais dificil de respirar."
            )
        ));

        builtScenes.add(new SceneData(
            "Cena 3 - Personagem",
            "sprites/Cena3Gemini.png",
            Arrays.asList(
                "Mas alguem decidiu nao ignorar isso.",
                "Talvez pequenas atitudes possam fazer a diferenca..."
            )
        ));

        return builtScenes;
    }

    private void loadCurrentScene() {
        visibleText.setLength(0);
        nextWordIndex = 0;
        currentWords = buildWordList(scenes.get(currentSceneIndex).lines);
        actionButton.setEnabled(false);
        actionButton.setText(currentSceneIndex < scenes.size() - 1 ? "Proxima cena" : "Iniciar jogo");
        repaint();
    }

    private List<String> buildWordList(List<String> lines) {
        List<String> words = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String[] split = lines.get(lineIndex).trim().split("\\s+");
            for (String word : split) {
                words.add(word);
            }
            if (lineIndex < lines.size() - 1) {
                words.add("\\n");
            }
        }

        return words;
    }

    private void revealNextWord() {
        if (nextWordIndex >= currentWords.size()) {
            textTimer.stop();
            actionButton.setEnabled(true);
            return;
        }

        String nextWord = currentWords.get(nextWordIndex);
        if ("\\n".equals(nextWord)) {
            visibleText.append('\n');
        } else {
            if (visibleText.length() > 0 && visibleText.charAt(visibleText.length() - 1) != '\n') {
                visibleText.append(' ');
            }
            visibleText.append(nextWord);
        }

        nextWordIndex++;
        repaint();
    }

    private void onActionButtonClicked() {
        if (currentSceneIndex < scenes.size() - 1) {
            currentSceneIndex++;
            loadCurrentScene();
            textTimer.restart();
            return;
        }

        textTimer.stop();
        onStartGame.run();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int buttonWidth = 180;
        int buttonHeight = 46;
        int x = getWidth() - buttonWidth - MARGIN;
        int y = getHeight() - buttonHeight - 14;
        actionButton.setBounds(x, y, buttonWidth, buttonHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawSceneImage(g2d);
        drawDialogueBox(g2d);
    }

    private void drawSceneImage(Graphics2D g2d) {
        SceneData currentScene = scenes.get(currentSceneIndex);
        BufferedImage sceneImage = currentScene.image;

        if (sceneImage != null) {
            g2d.drawImage(sceneImage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        GradientPaint fallback = new GradientPaint(0, 0, new Color(85, 95, 110), 0, getHeight(), new Color(50, 55, 70));
        g2d.setPaint(fallback);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Dialog", Font.BOLD, 28));
        g2d.drawString("Imagem da cena nao encontrada", MARGIN, 50);
    }

    private void drawDialogueBox(Graphics2D g2d) {
        int boxX = MARGIN;
        int boxY = getHeight() - DIALOG_HEIGHT - MARGIN;
        int boxWidth = getWidth() - (MARGIN * 2);
        int boxHeight = DIALOG_HEIGHT;

        g2d.setColor(new Color(15, 20, 35, 220));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 24, 24);

        g2d.setColor(new Color(240, 240, 245));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 24, 24);

        SceneData currentScene = scenes.get(currentSceneIndex);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Dialog", Font.BOLD, 30));
        g2d.drawString(currentScene.title, boxX + 22, boxY + 40);

        g2d.setFont(new Font("Dialog", Font.PLAIN, 28));
        drawMultilineText(g2d, visibleText.toString(), boxX + 22, boxY + 84, 42);

        if (!actionButton.isEnabled()) {
            g2d.setFont(new Font("Dialog", Font.PLAIN, 18));
            g2d.setColor(new Color(214, 216, 231));
            g2d.drawString("Texto em andamento...", boxX + 22, boxY + boxHeight - 18);
        }
    }

    private void drawMultilineText(Graphics2D g2d, String text, int x, int startY, int lineHeight) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            g2d.drawString(lines[i], x, startY + (i * lineHeight));
        }
    }

    private static class SceneData {
        private final String title;
        private final BufferedImage image;
        private final List<String> lines;

        private SceneData(String title, String imagePath, List<String> lines) {
            this.title = title;
            this.image = ResourceLoader.loadImage(imagePath);
            this.lines = lines;
        }
    }
}