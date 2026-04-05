package citycleaner.view;

import citycleaner.util.Constants;

import javax.swing.*;

/**
 * Janela principal do jogo City Cleaner
 */
public class MainWindow extends JFrame {
    
    public MainWindow() {
        setTitle("City Cleaner - Environmental Platformer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Adicionar painel do jogo
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        
        // Configurar tamanho e posição
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
