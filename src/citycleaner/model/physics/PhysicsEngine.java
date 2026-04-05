package citycleaner.model.physics;

import citycleaner.model.entity.Player;
import citycleaner.model.world.Platform;
import java.util.List;

/**
 * Motor de física do jogo
 */
public class PhysicsEngine {
    
    public static void update(Player player, List<Platform> platforms) {
        // Atualizar posição e velocidade do jogador
        player.update();
        
        // Verificar colisões com plataformas
        CollisionManager.checkPlayerPlatformCollision(player, platforms);
        
        // Verificar limites do jogo
        CollisionManager.checkPlatformBounds(player);
    }
}
