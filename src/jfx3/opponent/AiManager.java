package jfx3.opponent;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jfx3.util.SingleRunScheduledService;
import marblegame.gamemechanics.Match;
import marblegame.solvers.AiSolver;

public class AiManager extends SingleRunScheduledService<Result> {
    public final IntegerProperty depth = new SimpleIntegerProperty();
    AiSolver aiSolver = new AiSolver();
    private Match match = null;

    public void setMatch(Match match) {
        this.match = match;
    }

    @Override
    protected javafx.concurrent.Task<Result> createTask() {
        return new Task();
    }

    private class Task extends javafx.concurrent.Task<Result> {
        final AiSolver aiSolver = AiManager.this.aiSolver;
        private Match match = AiManager.this.match;
        private int depth = AiManager.this.depth.get();

        @Override
        protected Result call() {
            aiSolver.setDepth(depth);
            int r = aiSolver.solve(match);
            return new Result(r);
        }
    }
}