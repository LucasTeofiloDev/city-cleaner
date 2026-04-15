package citycleaner.view;

import citycleaner.util.Constants;
import citycleaner.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

/**
 * Main menu shown before cutscenes.
 */
public class MenuPanel extends JPanel {
    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 56;
    private static final int BUTTON_GAP = 18;
    private static final int MENU_VERTICAL_OFFSET = 100;

    private final Runnable onStart;
    private final Runnable onExit;
    private final Runnable onToggleSound;
    private final BooleanSupplier isMutedSupplier;
    private final BufferedImage menuImage;

    private final JButton startButton;
    private final JButton exitButton;
    private final JButton soundButton;

    public MenuPanel(Runnable onStart, Runnable onExit, Runnable onToggleSound, BooleanSupplier isMutedSupplier) {
        this.onStart = onStart;
        this.onExit = onExit;
        this.onToggleSound = onToggleSound;
        this.isMutedSupplier = isMutedSupplier;
        this.menuImage = ResourceLoader.loadImage("sprites/MenuImage.png");

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setLayout(null);

        startButton = createButton("Iniciar jogo");
        startButton.addActionListener(e -> this.onStart.run());
        add(startButton);

        exitButton = createButton("Sair");
        exitButton.addActionListener(e -> this.onExit.run());
        add(exitButton);

        soundButton = createButton("");
        soundButton.addActionListener(e -> {
            this.onToggleSound.run();
            updateSoundButtonText();
        });
        add(soundButton);

        updateSoundButtonText();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);

        button.setFocusable(false);
        button.setFont(new Font("Dialog", Font.BOLD, 26));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int totalHeight = (BUTTON_HEIGHT * 3) + (BUTTON_GAP * 2);
        int startX = (getWidth() - BUTTON_WIDTH) / 2;
        int startY = ((getHeight() - totalHeight) / 2) + MENU_VERTICAL_OFFSET;
        int maxStartY = getHeight() - totalHeight - 28;
        if (startY > maxStartY) {
            startY = maxStartY;
        }

        startButton.setBounds(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
        exitButton.setBounds(startX, startY + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
        soundButton.setBounds(startX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private void updateSoundButtonText() {
        soundButton.setText(isMutedSupplier.getAsBoolean() ? "Som: desligado" : "Som: ligado");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (menuImage != null) {
            g2d.drawImage(menuImage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        GradientPaint fallback = new GradientPaint(0, 0, new Color(44, 62, 80), 0, getHeight(), new Color(30, 30, 46));
        g2d.setPaint(fallback);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Dialog", Font.BOLD, 28));
        g2d.drawString("MenuImage.png nao encontrada", 30, 50);
    }
}