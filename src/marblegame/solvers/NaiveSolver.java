package marblegame.solvers;

import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.PossibleMoveIterator;

/**
 * Created by dennis on 17-11-17.
 */
public class NaiveSolver implements Solver {

    @Override
    public int solve(Match match) {
        PossibleMoveIterator iterator = PossibleMoveIterator.from(match);
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return -1;
        }
    }
}
