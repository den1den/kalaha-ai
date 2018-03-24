package talf.ai;

import talf.mechanics.Match;
import talf.mechanics.Move;

public interface Solver {
    Move solve(Match match);
}
