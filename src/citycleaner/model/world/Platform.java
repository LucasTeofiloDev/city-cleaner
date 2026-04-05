package citycleaner.model.world;

import java.awt.Rectangle;

/**
 * Classe que representa uma plataforma no jogo
 */
public class Platform {
    private int x;
    private int y;
    private int width;
    private int height;
    
    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public Platform(int x, int y) {
        this(x, y, 64, 32);
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
