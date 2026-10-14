package players.heuristics;

import core.AbstractGameState;
import core.CoreConstants;
import core.interfaces.IStateHeuristic;
import games.cantstop.CantStopGameState;
import games.cantstop.CantStopParameters;

public class CantStopHeuristic implements IStateHeuristic {

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        // 1. Terminal states: exact values
        CoreConstants.GameResult result = gs.getPlayerResults()[playerId];
        if (result == CoreConstants.GameResult.WIN_GAME)  return 1.0;
        if (result == CoreConstants.GameResult.LOSE_GAME) return -1.0;

        CantStopGameState cs = (CantStopGameState) gs;
        CantStopParameters params = (CantStopParameters) cs.getGameParameters();
        int opponent = 1 - playerId;  // 2-player game

        double score = 0.0;

        // 2. Claimed columns — the decisive feature (3 claims = win)
        for (int n = 2; n <= 12; n++) {
            if (cs.isTrackComplete(n)) {
                // the player whose marker reached the top owns the column
                if (cs.getMarkerPosition(n, playerId) >= params.maxValue(n))
                    score += 0.30;
                else
                    score -= 0.30;
            }
        }

        // 3. Permanent progress, normalized by column length
        for (int n = 2; n <= 12; n++) {
            if (!cs.isTrackComplete(n)) {
                double max = params.maxValue(n);
                score += 0.15 * (cs.getMarkerPosition(n, playerId) / max);
                score -= 0.15 * (cs.getMarkerPosition(n, opponent) / max);
            }
        }

        // 4. Temporary progress — heavily discounted: a bust wipes it
        for (int n : cs.getMarkersMoved()) {
            int temp = cs.getTemporaryMarkerPosition(n);
            int perm = cs.getMarkerPosition(n, cs.getCurrentPlayer());
            double gain = (temp - perm) / (double) params.maxValue(n);
            if (cs.getCurrentPlayer() == playerId)
                score += 0.05 * gain;
            else
                score -= 0.05 * gain;
        }

        // 5. Squash to (-1, 1)
        return Math.tanh(score);
    }
}