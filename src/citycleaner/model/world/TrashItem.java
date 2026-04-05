package citycleaner.model.world;

import citycleaner.util.Constants;

import java.awt.Rectangle;

/**
 * Item de lixo marcado sobre o fundo e coletado por colisao com o jogador.
 */
public class TrashItem {
    private final int centerX;
    private final int centerY;
    private final int radius;
    private final int points;
    private boolean collected;

    public TrashItem(int centerX, int centerY) {
        this(centerX, centerY, 22, Constants.POINTS_ITEM);
    }

    public TrashItem(int centerX, int centerY, int radius, int points) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.points = points;
        this.collected = false;
    }

    public boolean intersects(Rectangle bounds) {
        return !collected && getBounds().intersects(bounds);
    }

    public boolean isNear(Rectangle bounds, int padding) {
        if (collected) {
            return false;
        }

        Rectangle proximityBounds = new Rectangle(bounds);
        proximityBounds.grow(padding, padding);
        return proximityBounds.intersects(getBounds());
    }

    public Rectangle getBounds() {
        int diameter = radius * 2;
        return new Rectangle(centerX - radius, centerY - radius, diameter, diameter);
    }

    public void collect() {
        collected = true;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getRadius() {
        return radius;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCollected() {
        return collected;
    }
}
