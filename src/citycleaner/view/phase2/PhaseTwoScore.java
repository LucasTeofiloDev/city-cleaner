package citycleaner.view.phase2;

/**
 * Isolated score tracker for Phase 2 trash deposit actions.
 */
public class PhaseTwoScore {
    private int totalPoints;

    public void addPoints(int points) {
        if (points <= 0) {
            return;
        }

        totalPoints += points;
    }

    public int getTotalPoints() {
        return totalPoints;
    }
}
