package citycleaner.model.entity;

import citycleaner.util.Constants;
import java.awt.Rectangle;

/**
 * Classe que representa o jogador do jogo
 */
public class Player {
    private float x;
    private float y;
    private final float spawnX;
    private final float spawnY;
    private float velX = 0;
    private float velY = 0;
    private int lives;
    private int score;
    private boolean onGround = false;
    private boolean jumping = false;
    
    // Direção: 1 = direita, -1 = esquerda, 0 = parado
    private int direction = 0;
    
    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.spawnX = startX;
        this.spawnY = startY;
        this.lives = Constants.MAX_LIVES;
        this.score = 0;
    }
    
    public void update() {
        // Aplicar movimento horizontal
        velX = direction * Constants.MOVE_SPEED;
        x += velX;
        
        // Aplicar gravidade
        velY += Constants.GRAVITY;
        if (velY > Constants.MAX_FALL_SPEED) {
            velY = Constants.MAX_FALL_SPEED;
        }
        
        // Aplicar movimento vertical
        y += velY;
        
        // Limite de tela horizontal
        if (x < 0) {
            x = 0;
        }
        if (x + Constants.PLAYER_WIDTH > Constants.GAME_WIDTH) {
            x = Constants.GAME_WIDTH - Constants.PLAYER_WIDTH;
        }
        
        // Se cair para fora da tela, perde uma vida
        if (y > Constants.GAME_HEIGHT) {
            takeDamage();
            respawn();
        }
    }
    
    public void jump() {
        if (onGround && !jumping) {
            velY = Constants.JUMP_FORCE;
            onGround = false;
            jumping = true;
        }
    }
    
    public void moveLeft() {
        direction = -1;
    }
    
    public void moveRight() {
        direction = 1;
    }
    
    public void stopMoving() {
        direction = 0;
    }
    
    public void collectItem(int points) {
        score += points;
    }
    
    public void takeDamage() {
        if (lives > 0) {
            lives--;
        }
    }
    
    public void respawn() {
        x = spawnX;
        y = spawnY;
        velX = 0;
        velY = 0;
        onGround = false;
        jumping = false;
    }

    public void teleportTo(double targetX, double targetY) {
        x = (float) targetX;
        y = (float) targetY;
        velX = 0;
        velY = 0;
        direction = 0;
        onGround = false;
        jumping = false;
    }
    
    // Getters e Setters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelX() { return velX; }
    public float getVelY() { return velY; }
    public int getWidth() { return Constants.PLAYER_WIDTH; }
    public int getHeight() { return Constants.PLAYER_HEIGHT; }
    
    public int getLives() { return lives; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean ground) { this.onGround = ground; jumping = false; }
    
    public void setVelY(float vy) { this.velY = vy; }
    
    public void setY(double y) { this.y = (float) y; }
    public void setX(double x) { this.x = (float) x; }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
    }
}
