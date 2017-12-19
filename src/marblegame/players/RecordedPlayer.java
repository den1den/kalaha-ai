package marblegame.players;

import marblegame.gamemechanics.Match;
import marblegame.solvers.AiSolver;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dennis on 3-10-17.
 */
public class RecordedPlayer<P extends Player> implements Player {
    private final P player;
    private List<Integer> moves = new LinkedList<>();

    public RecordedPlayer(P player) {
        this.player = player;
    }

    @Override
    public int getMove() {
        int move = player.getMove();
        moves.add(move);
        return move;
    }

    public P get() {
        return player;
    }

    public int getLastMove() {
        if (moves.isEmpty()) {
            return -1;
        }
        return moves.get(moves.size() - 1);
    }

    @Override
    public String toString() {
        return player.toString();
    }

    public static RecordedPlayer<AutomaticPlayer> recordedAi(Match m, AiSolver ai) {
        return new RecordedPlayer<>(new AutomaticPlayer(m, ai));
    }

    public DeterministicPlayer getPredetPlayer() {
        return getPredetPlayer(moves);
    }

    public DeterministicPlayer getPredetPlayer(List<Integer> moves) {
        return new DeterministicPlayer(moves.stream().mapToInt(Integer::intValue).toArray());
    }
}
