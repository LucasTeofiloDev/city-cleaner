package citycleaner.util;

/**
 * Classe com constantes globais do jogo City Cleaner
 */
public class Constants {
    // Dimensões da janela
    public static final int WINDOW_WIDTH = 1152;
    public static final int WINDOW_HEIGHT = 648;
    
    // Dimensões do jogo
    public static final int GAME_WIDTH = 1152;
    public static final int GAME_HEIGHT = 576;
    public static final int HUD_HEIGHT = 72;
    
    // Dimensões do jogador
    public static final int PLAYER_WIDTH = 165;
    public static final int PLAYER_HEIGHT = 240;
    
    // Física
    public static final float GRAVITY = 0.6f;
    public static final float JUMP_FORCE = -15f;
    public static final float MOVE_SPEED = 5f;
    public static final float MAX_FALL_SPEED = 20f;
    
    // Plataformas
    public static final int PLATFORM_WIDTH = 64;
    public static final int PLATFORM_HEIGHT = 32;
    
    // FPS e Game Loop
    public static final int FPS = 60;
    public static final int FRAME_TIME = 1000 / FPS; // ~16ms
    
    // Cores
    public static final int COLOR_BACKGROUND = 0x87CEEB; // Sky blue
    public static final int COLOR_HUD = 0x2C3E50;        // Dark gray
    
    // Pontuação
    public static final int POINTS_ITEM = 10;
    public static final int MAX_LIVES = 3;
    
    // Estados do jogo
    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }
}
