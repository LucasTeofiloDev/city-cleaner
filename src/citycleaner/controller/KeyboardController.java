package citycleaner.controller;

import citycleaner.model.entity.Player;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Controlador de teclado para entrada do jogador
 */
public class KeyboardController implements KeyListener {
    private final Player player;
    private final Runnable teleportAction;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    public KeyboardController(Player player) {
        this(player, null);
    }

    public KeyboardController(Player player, Runnable teleportAction) {
        this.player = player;
        this.teleportAction = teleportAction;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            leftPressed = true;
            player.moveLeft();
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            rightPressed = true;
            player.moveRight();
        }
        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            player.jump();
        }
        if ((key == KeyEvent.VK_T || key == KeyEvent.VK_E) && teleportAction != null) {
            teleportAction.run();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        
        // Parar movimento se nenhuma tecla estiver pressionada
        if (!leftPressed && !rightPressed) {
            player.stopMoving();
        }
        
        // Manter movimento se uma tecla ainda estiver pressionada
        if (leftPressed) {
            player.moveLeft();
        }
        if (rightPressed) {
            player.moveRight();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
}
