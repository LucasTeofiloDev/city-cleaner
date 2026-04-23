package citycleaner.view.phase2;

import citycleaner.model.entity.Player;
import citycleaner.model.world.TrashItem;
import citycleaner.util.Constants;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles Phase 2 gameplay rules: pickup, deposit, timer and score events.
 */
public class PhaseTwoController {
    private static final int INTERACTION_PADDING = 28;
    private static final int TRASH_RADIUS = 16;

    private final Player player;
    private final Rectangle binBounds;
    private final PhaseTwoTimer timer;
    private final PhaseTwoScore score;
    private final List<TrashItem> trashItems;
    private final int[] spawnXs;
    private final int spawnY;
    private final Random random;

    private boolean phaseStarted;
    private boolean phaseFinished;
    private int carryingPoints;
    private long scoreFeedbackStartMs;
    private int scoreFeedbackValue;

    public PhaseTwoController(Player player, Rectangle binBounds, long durationMs) {
        this.player = player;
        this.binBounds = new Rectangle(binBounds);
        this.timer = new PhaseTwoTimer(durationMs);
        this.score = new PhaseTwoScore();
        this.trashItems = new ArrayList<>();
        this.spawnXs = new int[] {100, 190, 280, 360, 440, 710, 790, 880, 980, 1060};
        this.spawnY = Constants.GAME_HEIGHT - 72;
        this.random = new Random();
    }

    public void initializeTrash(int amount) {
        trashItems.clear();
        for (int i = 0; i < amount; i++) {
            trashItems.add(createRandomTrashItem());
        }
    }

    public void startPhase() {
        if (phaseStarted) {
            return;
        }

        phaseStarted = true;
        timer.start();
    }

    public void update() {
        if (!phaseStarted || phaseFinished) {
            return;
        }

        if (timer.isFinished()) {
            phaseFinished = true;
        }
    }

    public InteractionResult interact() {
        if (!phaseStarted || phaseFinished) {
            return InteractionResult.NONE;
        }

        Rectangle playerBounds = player.getBounds();

        if (carryingPoints <= 0) {
            for (int i = 0; i < trashItems.size(); i++) {
                TrashItem item = trashItems.get(i);
                if (!item.isNear(playerBounds, INTERACTION_PADDING)) {
                    continue;
                }

                carryingPoints = item.getPoints();
                trashItems.remove(i);
                return InteractionResult.PICKED_TRASH;
            }
            return InteractionResult.NONE;
        }

        if (!isPlayerNearBin(playerBounds)) {
            return InteractionResult.NONE;
        }

        score.addPoints(carryingPoints);
        scoreFeedbackValue = carryingPoints;
        scoreFeedbackStartMs = System.currentTimeMillis();
        carryingPoints = 0;
        trashItems.add(createRandomTrashItem());
        return InteractionResult.DEPOSITED_TRASH;
    }

    public boolean isPlayerNearBin(Rectangle playerBounds) {
        Rectangle proximity = new Rectangle(binBounds);
        proximity.grow(INTERACTION_PADDING, INTERACTION_PADDING);
        return proximity.intersects(playerBounds);
    }

    public List<TrashItem> getTrashItems() {
        return trashItems;
    }

    public Rectangle getBinBounds() {
        return new Rectangle(binBounds);
    }

    public boolean hasCarriedTrash() {
        return carryingPoints > 0;
    }

    public boolean isPhaseStarted() {
        return phaseStarted;
    }

    public boolean isPhaseFinished() {
        return phaseFinished;
    }

    public int getRemainingSeconds() {
        return timer.getRemainingSecondsCeil();
    }

    public int getScore() {
        return score.getTotalPoints();
    }

    public int getScoreFeedbackValue() {
        return scoreFeedbackValue;
    }

    public boolean isScoreFeedbackVisible() {
        if (scoreFeedbackValue <= 0 || scoreFeedbackStartMs <= 0) {
            return false;
        }

        return (System.currentTimeMillis() - scoreFeedbackStartMs) <= 850;
    }

    private TrashItem createRandomTrashItem() {
        int attempts = 0;
        while (attempts < 40) {
            int x = spawnXs[random.nextInt(spawnXs.length)];
            Rectangle candidateBounds = new Rectangle(x - TRASH_RADIUS, spawnY - TRASH_RADIUS, TRASH_RADIUS * 2, TRASH_RADIUS * 2);
            if (!candidateBounds.intersects(binBounds)) {
                return new TrashItem(x, spawnY, TRASH_RADIUS, Constants.POINTS_ITEM);
            }
            attempts++;
        }

        return new TrashItem(120, spawnY, TRASH_RADIUS, Constants.POINTS_ITEM);
    }

    public enum InteractionResult {
        NONE,
        PICKED_TRASH,
        DEPOSITED_TRASH
    }
}
