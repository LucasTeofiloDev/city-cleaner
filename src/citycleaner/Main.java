package citycleaner;

import citycleaner.view.MainWindow;

/**
 * Classe principal - ponto de entrada do jogo City Cleaner
 */
public class Main {
    public static void main(String[] args) {
        // Executar na thread de eventos da GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainWindow();
        });
    }
}
