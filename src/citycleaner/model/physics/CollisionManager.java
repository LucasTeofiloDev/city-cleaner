package citycleaner.model.physics;

import citycleaner.model.entity.Player;
import citycleaner.model.world.Platform;
import citycleaner.util.Constants;
import java.awt.Rectangle;
import java.util.List;

/**
 * Gerenciador de colisões do jogo
 */
public class CollisionManager {
    
    public static void checkPlayerPlatformCollision(Player player, List<Platform> platforms) {
        Rectangle playerBounds = player.getBounds();
        
        for (Platform platform : platforms) {
            Rectangle platformBounds = platform.getBounds();
            
            if (playerBounds.intersects(platformBounds)) {
                // Detectar colisão por cima da plataforma (landing)
                if (player.getVelY() > 0 && 
                    playerBounds.getMaxY() - player.getVelY() <= platformBounds.getMinY() + 5) {
                    player.setOnGround(true);
                    player.setVelY(0);
                    player.setY(platformBounds.getMinY() - Constants.PLAYER_HEIGHT);
                }
                // Colisão por baixo
                else if (player.getVelY() < 0 && 
                         playerBounds.getMinY() - player.getVelY() >= platformBounds.getMaxY() - 5) {
                    player.setVelY(1);
                }
            } else {
                // Se não está tocando nenhuma plataforma, não está no chão
                if (player.isOnGround()) {
                    player.setOnGround(false);
                }
            }
        }
    }
    
    public static void checkPlatformBounds(Player player) {
        // Colisão com o chão (y = GAME_HEIGHT)
        if (player.getY() + Constants.PLAYER_HEIGHT >= Constants.GAME_HEIGHT) {
            player.setOnGround(true);
            player.setVelY(0);
            player.setY(Constants.GAME_HEIGHT - Constants.PLAYER_HEIGHT);
        }
    }
}
