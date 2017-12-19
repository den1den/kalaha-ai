package marblegame.players;

import marblegame.gamemechanics.Match;
import marblegame.solvers.Solver;

public class AutomaticPlayer implements Player {
    final Solver solver;
    Match match;

    public AutomaticPlayer(Solver solver) {
        this(null, solver);
    }

    public AutomaticPlayer(Match match, Solver solver) {
        this.match = match;
        this.solver = solver;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    @Override
    public int getMove() {
        return solver.solve(match);
    }
}
